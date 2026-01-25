package com.okx.trading.cust.service.impl;// 在原有的HeimaStrategyAServiceImpl中添加WebSocket集成

import com.okx.trading.cust.manager.BarSeriesManager;
import com.okx.trading.cust.service.HeimaStrategyService;
import com.okx.trading.strategy.cust.HeimaStrategyBuilder;
import com.okx.trading.strategy.cust.StrategyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;



import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.okx.trading.cust.entity.TradeOrder;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeimaStrategyAServiceImpl implements HeimaStrategyService {

    private final StrategyConfig config;
    private final HeimaStrategyBuilder strategyBuilder;
    private final BarSeriesManager barSeriesManager; // 注入BarSeries管理器
    private final OKXWebSocketService okxWebSocketService; // 注入WebSocket服务

    private Strategy strategy;
    private TradingRecord tradingRecord;
    private boolean strategyRunning = false;

    @Override
    public void initialize(String symbol) {
        log.info("初始化黑马策略A，交易对: {}", symbol);

        // 1. 从BarSeries管理器获取实时数据系列
        BarSeries higherSeries = barSeriesManager.getHigherSeries(); // 4小时系列

        if (higherSeries.getBarCount() < 10) {
            log.warn("4小时K线数据不足({}根)，等待更多数据...", higherSeries.getBarCount());
            // 可以等待WebSocket数据积累或使用REST API补充历史数据
            BarSeries history = okxWebSocketService.fetchKlines(symbol, "4H", 50);
            // 将历史数据合并到系列中...
        }

        // 2. 构建策略
        strategy = strategyBuilder.buildHeimaStrategy(
            barSeriesManager.getBaseSeries(),
            higherSeries
        );

        // 3. 初始化交易记录
        tradingRecord = new BaseTradingRecord();

        log.info("策略初始化完成，4小时K线数据: {}根", higherSeries.getBarCount());
    }

    /**
     * 定时检查信号（由新的K线触发）
     */
    @Scheduled(fixedDelay = 1000) // 每秒检查一次
    public void autoCheckSignal() {
        if (!strategyRunning || strategy == null) {
            return;
        }

        BarSeries higherSeries = barSeriesManager.getHigherSeries();
        if (higherSeries.getBarCount() == 0) {
            return;
        }

        int endIndex = higherSeries.getEndIndex();

        try {
            // 检查入场信号
            if (strategy.shouldEnter(endIndex, tradingRecord)) {
                executeEntrySignal(endIndex);
            }

            // 检查退出信号
            if (strategy.shouldExit(endIndex, tradingRecord)) {
                executeExitSignal(endIndex);
            }

        } catch (Exception e) {
            log.error("自动检查信号异常", e);
        }
    }

    @Override
    public void startStrategy() {
        if (strategy == null) {
            log.error("策略未初始化，请先调用initialize()");
            return;
        }

        strategyRunning = true;
        log.info("策略已启动，开始自动交易");

        // 确保WebSocket连接正常
        if (!okxWebSocketService.isWebSocketConnected()) {
            log.warn("WebSocket未连接，策略可能无法获取实时数据");
        }
    }

    @Override
    public void stopStrategy() {
        strategyRunning = false;
        log.info("策略已停止");
    }


    /**
     * 执行入场信号
     */
    private JSONObject executeEntrySignal(int index) {
        JSONObject result = new JSONObject();

        try {
            String symbol = barSeries.getName();
            Num currentPrice = barSeries.getBar(index).getClosePrice();

            // 获取账户余额
            JSONObject balanceInfo = dataService.getAccountBalance("USDT");
            Num usdtBalance = dataService.extractAvailableBalance(balanceInfo, "USDT");

            if (usdtBalance.isZero() || usdtBalance.isLessThan(DecimalNum.valueOf(10))) {
                result.put("success", false);
                result.put("message", "账户余额不足");
                return result;
            }

            // 计算仓位大小
            Num positionSize = tradeService.calculatePositionSize(
                    usdtBalance, currentPrice,
                    strategyConfig.getHeimaA().getMaxPositionSize());

            // 确定方向（这里简化为总是开多，实际应根据信号判断）
            boolean isLongSignal = true; // 这里需要根据实际信号判断
            String side = isLongSignal ? "buy" : "sell";

            // 下单
            TradeOrder order = tradeService.placeMarketOrder(symbol, side, positionSize);

            if (order != null) {
                // 记录交易
                tradingRecord.enter(index, currentPrice, positionSize);

                // 创建仓位记录
                Position position = new Position();
                position.setSymbol(symbol);
                position.setSide(isLongSignal ? "long" : "short");
                position.setQuantity(positionSize.getDelegate());
                position.setEntryPrice(currentPrice.getDelegate());
                position.setCurrentPrice(currentPrice.getDelegate());
                position.setEntryOrderId(order.getOrderId());
                position.setStrategyName("HeimaA");
                position.calculateUnrealizedPnl();
                positionRepository.save(position);

                // 设置止损
                setStopLoss(position, currentPrice, isLongSignal);

                result.put("success", true);
                result.put("orderId", order.getOrderId());
                result.put("side", side);
                result.put("quantity", positionSize.toString());
                result.put("price", currentPrice.toString());

                log.info("入场信号执行成功: {} {} {} @ {}",
                        side, positionSize, symbol, currentPrice);
            } else {
                result.put("success", false);
                result.put("message", "下单失败");
            }

        } catch (Exception e) {
            log.error("执行入场信号失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 执行退出信号
     */
    private JSONObject executeExitSignal(int index) {
        JSONObject result = new JSONObject();

        try {
            if (!tradingRecord.getCurrentPosition().isOpened()) {
                result.put("success", false);
                result.put("message", "没有持仓需要平仓");
                return result;
            }

            String symbol = barSeries.getName();
            Num currentPrice = barSeries.getBar(index).getClosePrice();
            Position position = tradingRecord.getCurrentPosition();

            // 查找数据库中的仓位记录
            Optional<Position> positionOpt = positionRepository.findByEntryOrderId(
                    position.getEntry().getNetPrice().toString()); // 这里简化

            if (positionOpt.isPresent()) {
                Position dbPosition = positionOpt.get();

                // 平仓
                String side = "long".equals(dbPosition.getSide()) ? "sell" : "buy";
                Num quantity = DecimalNum.valueOf(dbPosition.getQuantity());

                TradeOrder exitOrder = tradeService.placeMarketOrder(symbol, side, quantity);

                if (exitOrder != null) {
                    // 记录交易退出
                    tradingRecord.exit(index, currentPrice, quantity);

                    // 更新仓位记录
                    dbPosition.setExitOrderId(exitOrder.getOrderId());
                    dbPosition.setExitPrice(currentPrice.getDelegate());
                    dbPosition.setStatus("closed");
                    dbPosition.setClosedAt(LocalDateTime.now());

                    // 计算已实现盈亏
                    calculateRealizedPnl(dbPosition);
                    positionRepository.save(dbPosition);

                    result.put("success", true);
                    result.put("orderId", exitOrder.getOrderId());
                    result.put("side", side);
                    result.put("quantity", quantity.toString());
                    result.put("price", currentPrice.toString());
                    result.put("realizedPnl", dbPosition.getRealizedPnl());

                    log.info("退出信号执行成功: 平仓 {} @ {}", quantity, currentPrice);
                } else {
                    result.put("success", false);
                    result.put("message", "平仓下单失败");
                }
            } else {
                result.put("success", false);
                result.put("message", "未找到对应的仓位记录");
            }

        } catch (Exception e) {
            log.error("执行退出信号失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 设置止损
     */
    private void setStopLoss(Position position, Num entryPrice, boolean isLong) {
        try {
            double stopLossPercent = strategyConfig.getHeimaA().getStopLossPercent();
            Num stopLossPrice;

            if (isLong) {
                stopLossPrice = entryPrice.multipliedBy(
                        DecimalNum.valueOf(1 - stopLossPercent / 100.0));
            } else {
                stopLossPrice = entryPrice.multipliedBy(
                        DecimalNum.valueOf(1 + stopLossPercent / 100.0));
            }

            String side = isLong ? "sell" : "buy";
            TradeOrder stopOrder = tradeService.placeStopOrder(
                    position.getSymbol(), side,
                    DecimalNum.valueOf(position.getQuantity()),
                    stopLossPrice
            );

            if (stopOrder != null) {
                position.setStopLossPrice(stopLossPrice.getDelegate());
                positionRepository.save(position);

                log.info("止损单设置成功: {} @ {}", side, stopLossPrice);
            }

        } catch (Exception e) {
            log.error("设置止损失败", e);
        }
    }

    /**
     * 计算已实现盈亏
     */
    private void calculateRealizedPnl(Position position) {
        if (position.getEntryPrice() != null && position.getExitPrice() != null
                && position.getQuantity() != null) {

            BigDecimal entryValue = position.getEntryPrice().multiply(position.getQuantity());
            BigDecimal exitValue = position.getExitPrice().multiply(position.getQuantity());

            if ("long".equals(position.getSide())) {
                position.setRealizedPnl(exitValue.subtract(entryValue));
            } else {
                position.setRealizedPnl(entryValue.subtract(exitValue));
            }

            if (entryValue.compareTo(BigDecimal.ZERO) > 0) {
                position.setRealizedPnlPercent(
                        position.getRealizedPnl().divide(entryValue, 8, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                );
            }
        }
    }

    /**
     * 聚合到高时间框架
     */
    private BarSeries aggregateToHigherTimeframe(BarSeries baseSeries, Duration timeframe) {
        try {
            BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder()
                    .withName(baseSeries.getName() + "_higher");

            List<Bar> aggregatedBars = new ArrayList<>();
            Bar currentBar = null;
            ZonedDateTime nextBarTime = null;

            for (int i = 0; i < baseSeries.getBarCount(); i++) {
                Bar bar = baseSeries.getBar(i);
                ZonedDateTime barTime = bar.getEndTime().atZone(ZoneId.systemDefault());

                if (currentBar == null || !barTime.isBefore(nextBarTime)) {
                    // 创建新Bar
                    if (currentBar != null) {
                        aggregatedBars.add(currentBar);
                    }

                    currentBar = BaseBar.builder()
                            .timePeriod(timeframe)
                            .endTime(barTime.plus(timeframe).toInstant())
                            .openPrice(bar.getOpenPrice())
                            .highPrice(bar.getHighPrice())
                            .lowPrice(bar.getLowPrice())
                            .closePrice(bar.getClosePrice())
                            .volume(bar.getVolume())
                            .amount(bar.getAmount())
                            .build();

                    nextBarTime = barTime.plus(timeframe);
                } else {
                    // 更新当前Bar
                    Num high = currentBar.getHighPrice().max(bar.getHighPrice());
                    Num low = currentBar.getLowPrice().min(bar.getLowPrice());
                    Num volume = currentBar.getVolume().plus(bar.getVolume());
                    Num amount = currentBar.getAmount().plus(bar.getAmount());

                    currentBar = BaseBar.builder()
                            .timePeriod(timeframe)
                            .endTime(nextBarTime.toInstant())
                            .openPrice(currentBar.getOpenPrice())
                            .highPrice(high)
                            .lowPrice(low)
                            .closePrice(bar.getClosePrice())
                            .volume(volume)
                            .amount(amount)
                            .build();
                }
            }

            // 添加最后一个Bar
            if (currentBar != null) {
                aggregatedBars.add(currentBar);
            }

            // 添加到builder
            for (Bar bar : aggregatedBars) {
                builder.addBar(bar);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("聚合时间框架失败", e);
            // 如果聚合失败，返回基础序列
            return baseSeries;
        }
    }

    /**
     * 自定义退出规则
     */
    private static class HeimaExitRule extends AbstractRule {
        private final BarSeries series;
        private final Num triggerPoints;
        private final Num trailOffset;
        private final Num trailPoints;
        private final BarSeries higherSeries;

        public HeimaExitRule(BarSeries series, Num triggerPoints, Num trailOffset,
                             Num trailPoints, BarSeries higherSeries) {
            this.series = series;
            this.triggerPoints = triggerPoints;
            this.trailOffset = trailOffset;
            this.trailPoints = trailPoints;
            this.higherSeries = higherSeries;
        }

        @Override
        public boolean isSatisfied(int index, TradingRecord tradingRecord) {
            if (!tradingRecord.getCurrentPosition().isOpened()) {
                return false;
            }

            Position position = tradingRecord.getCurrentPosition();
            Num entryPrice = position.getEntry().getNetPrice();
            Num currentPrice = series.getBar(index).getClosePrice();

            // 计算盈利点数
            Num profitPoints;
            if (position.getEntry().getType().isBuy()) {
                profitPoints = currentPrice.minus(entryPrice);
            } else {
                profitPoints = entryPrice.minus(currentPrice);
            }

            // 检查是否触发移动止盈
            if (profitPoints.isGreaterThanOrEqual(triggerPoints)) {
                // 计算移动止损价格
                Num trailingStop;
                if (position.getEntry().getType().isBuy()) {
                    // 多单：最高价 - trailOffset
                    Num highestSinceEntry = calculateHighestSinceEntry(index, position);
                    trailingStop = highestSinceEntry.minus(trailOffset);
                } else {
                    // 空单：最低价 + trailOffset
                    Num lowestSinceEntry = calculateLowestSinceEntry(index, position);
                    trailingStop = lowestSinceEntry.plus(trailOffset);
                }

                // 检查是否触发止损
                if (position.getEntry().getType().isBuy()) {
                    return currentPrice.isLessThanOrEqual(trailingStop);
                } else {
                    return currentPrice.isGreaterThanOrEqual(trailingStop);
                }
            }

            // 初始止损：使用上一根高时间框架的高低点
            int htIndex = findHigherTimeframeIndex(index);
            if (htIndex > 0) {
                Bar prevHigherBar = higherSeries.getBar(htIndex - 1);

                if (position.getEntry().getType().isBuy()) {
                    // 多单：跌破上一根4小时低点止损
                    return currentPrice.isLessThanOrEqual(prevHigherBar.getLowPrice());
                } else {
                    // 空单：突破上一根4小时高点止损
                    return currentPrice.isGreaterThanOrEqual(prevHigherBar.getHighPrice());
                }
            }

            return false;
        }

        private Num calculateHighestSinceEntry(int index, Position position) {
            int entryIndex = position.getEntry().getIndex();
            Num highest = series.getBar(entryIndex).getHighPrice();

            for (int i = entryIndex + 1; i <= index; i++) {
                Num currentHigh = series.getBar(i).getHighPrice();
                if (currentHigh.isGreaterThan(highest)) {
                    highest = currentHigh;
                }
            }

            return highest;
        }

        private Num calculateLowestSinceEntry(int index, Position position) {
            int entryIndex = position.getEntry().getIndex();
            Num lowest = series.getBar(entryIndex).getLowPrice();

            for (int i = entryIndex + 1; i <= index; i++) {
                Num currentLow = series.getBar(i).getLowPrice();
                if (currentLow.isLessThan(lowest)) {
                    lowest = currentLow;
                }
            }

            return lowest;
        }

        private int findHigherTimeframeIndex(int baseIndex) {
            ZonedDateTime baseTime = series.getBar(baseIndex).getEndTime()
                    .atZone(ZoneId.systemDefault());

            for (int i = 0; i < higherSeries.getBarCount(); i++) {
                ZonedDateTime higherTime = higherSeries.getBar(i).getEndTime()
                        .atZone(ZoneId.systemDefault());

                if (higherTime.isAfter(baseTime)) {
                    return Math.max(0, i - 1);
                }
            }

            return higherSeries.getBarCount() - 1;
        }
    }

    /**
     * 仓位跟踪器
     */
    private static class PositionTracker {
        private final String positionId;
        private boolean trailingStopEnabled = false;
        private Num extremePrice;
        private Num currentStop;

        public PositionTracker(String positionId) {
            this.positionId = positionId;
            this.extremePrice = DecimalNum.valueOf(0);
            this.currentStop = DecimalNum.valueOf(0);
        }

        // getters and setters
        public String getPositionId() { return positionId; }
        public boolean isTrailingStopEnabled() { return trailingStopEnabled; }
        public void setTrailingStopEnabled(boolean enabled) { this.trailingStopEnabled = enabled; }
        public Num getExtremePrice() { return extremePrice; }
        public void setExtremePrice(Num extremePrice) { this.extremePrice = extremePrice; }
        public Num getCurrentStop() { return currentStop; }
        public void setCurrentStop(Num currentStop) { this.currentStop = currentStop; }
    }

    /**
     * 获取策略状态
     */
    public JSONObject getStatus() {
        JSONObject status = new JSONObject();
        status.put("initialized", barSeries != null);
        status.put("barCount", barSeries != null ? barSeries.getBarCount() : 0);
        status.put("tradingRecord", tradingRecord != null ? tradingRecord.getTradeCount() : 0);
        status.put("activeTrackers", trackers.size());
        status.put("config", new JSONObject()
                .fluentPut("enabled", strategyConfig.getHeimaA().isEnabled())
                .fluentPut("triggerPoints", strategyConfig.getHeimaA().getTriggerPoints())
                .fluentPut("trailOffset", strategyConfig.getHeimaA().getTrailOffset())
                .fluentPut("trailPoints", strategyConfig.getHeimaA().getTrailPoints())
                .fluentPut("maxPositionSize", strategyConfig.getHeimaA().getMaxPositionSize())
        );

        // 添加当前持仓信息
        try {
            var openPositions = positionRepository.findByStatus("open");
            status.put("openPositions", openPositions.size());

            JSONArray positionsJson = new JSONArray();
            for (Position pos : openPositions) {
                JSONObject posJson = new JSONObject();
                posJson.put("symbol", pos.getSymbol());
                posJson.put("side", pos.getSide());
                posJson.put("quantity", pos.getQuantity());
                posJson.put("entryPrice", pos.getEntryPrice());
                posJson.put("unrealizedPnl", pos.getUnrealizedPnl());
                positionsJson.add(posJson);
            }
            status.put("positions", positionsJson);
        } catch (Exception e) {
            log.error("获取持仓信息失败", e);
        }

        return status;
    }

    /**
     * 更新数据
     */
    public JSONObject updateData(String symbol, String timeframe, int limit) {
        JSONObject result = new JSONObject();

        try {
            BarSeries newSeries = dataService.fetchKlines(symbol, timeframe, limit);

            if (newSeries != null && newSeries.getBarCount() > 0) {
                barSeries = newSeries;

                // 重新聚合高时间框架
                higherTimeframeSeries = aggregateToHigherTimeframe(
                        barSeries, Duration.ofHours(4));

                // 重新构建策略
                strategy = buildTa4jStrategy();

                result.put("success", true);
                result.put("barCount", barSeries.getBarCount());
                result.put("message", "数据更新成功");

                log.info("数据更新成功: {}根K线", barSeries.getBarCount());
            } else {
                result.put("success", false);
                result.put("message", "获取数据为空");
            }

        } catch (Exception e) {
            log.error("更新数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
