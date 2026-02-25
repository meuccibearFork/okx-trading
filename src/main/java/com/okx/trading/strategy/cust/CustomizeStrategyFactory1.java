package com.okx.trading.strategy.cust;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.okex.open.api.bean.account.result.PositionDetail;
import com.okex.open.api.bean.account.result.tracker.DynamicStopLossTracker;
import com.okex.open.api.bean.account.result.tracker.TradeStatistics;
import com.okex.open.api.bean.calculator.PositionCalculationResult;
import com.okex.open.api.calculator.OKXProfitCalculator;
import com.okex.open.api.service.trade.TradingService;
import com.okx.trading.constant.TradingSignal;
import com.okx.trading.constant.log.LoggerName;
import com.okx.trading.model.entity.RealTimeStrategyEntity;
import com.okx.trading.model.market.Candlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义-策略工厂 - 高级策略集合
 */
@Service
public class CustomizeStrategyFactory1 {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.WSS_MSG);

    private final TradingService tradingService;

    PositionDetail positionDetail;

    @Value("${strategy.yjw.tp:}")
    private double triggerPoints = 2.0;

    private final DecimalNum trailOffset = DecimalNum.valueOf(2.0);
    private final int leverage = 3;

    // 核心数据
    private Position currentPosition;
    private final Map<String, Object> metrics = new HashMap<>();

    public CustomizeStrategyFactory1(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    public Strategy yjwStrategy(BarSeries barSeries, RealTimeStrategyEntity state, Candlestick candlestick) {
        try {
            // 获取最新的Bar
            int endIndex = barSeries.getEndIndex();
            Bar currentBar = barSeries.getBar(endIndex);
            Bar previousBar = barSeries.getBar(endIndex - 1);

            // 检查是否是新K线
            boolean isNewBar = !currentBar.getEndTime().equals(previousBar.getEndTime());
            log.info("<TEST>Current Bar: isNewBar:{} endIndex:{} currentBar:{} previousBar:{}", isNewBar, endIndex, currentBar, previousBar);


            if (isNewBar) {
                // 计算信号
                Signal signal = calculateSignal(currentBar, previousBar);

                // 无持仓时开仓
                if (currentPosition == null) {
                    if (signal.isLongSignal()) {
                        openLongPosition(currentBar, previousBar, state, candlestick);
                    } else if (signal.isShortSignal()) {
                        openShortPosition(currentBar, previousBar, state, candlestick);
                    }
                }
            }

            // 更新持仓状态
            updatePosition(currentBar, state, candlestick);

            // 记录指标
            recordMetrics(currentBar);

        } catch (Exception e) {
            log.error("处理K线数据失败", e);
        }

        return null;
    }

    DynamicStopLossTracker tracker;

    public void stopLoss(BarSeries barSeries, RealTimeStrategyEntity state, Candlestick candlestick) {
        // 获取最新的Bar
        int endIndex = barSeries.getEndIndex();
        Bar currentBar = barSeries.getBar(endIndex);
        Bar previousBar = barSeries.getBar(endIndex - 1);
        if (positionDetail == null) {
            final var positions = tradingService.getPositions(state.getSymbolSwap());
            positionDetail = positions.getPositionDetailOne(state.getSymbolSwap());
            if(ObjectUtil.isEmpty(positionDetail)) {
                return;
            }
            // 使用Builder模式创建追踪器
            tracker = DynamicStopLossTracker.builder()
                    .entryPrice(BigDecimal.valueOf(100))
                    .initialStopLossPercent(BigDecimal.valueOf(-triggerPoints))
                    .incrementPercent(BigDecimal.valueOf(triggerPoints))
                    .build();
        } else {
            positionDetail.setMarkPx(currentBar.getClosePrice().bigDecimalValue());
        }

        PositionCalculationResult positionCalculationResult = OKXProfitCalculator.calculateAll(tracker, positionDetail);

        // 每次更新后显示状态
        tracker.logStatus();

        if (tracker.isStopLossTriggered()) {
            log.info("\n⚠️ 止损已被触发！交易结束。");
            positionCalculationResult.printSummary();

            // 显示调整历史
            tracker.logAdjustmentHistory();

            closePosition("止损触发", currentBar.getClosePrice(), state, candlestick);

            // 显示最终统计
            TradeStatistics stats = tracker.getStatistics();
            log.info("""
                            
                            ┌─────────────────────────────────────────────────────┐
                            │                   最终统计                          │
                            ├─────────────────────────────────────────────────────┤
                            │ 最终状态: {}
                            │ 入场价格: {}
                            │ 最终价格: {}
                            │ 最终收益率: {}%
                            │ 最高收益率: {}%
                            │ 总调整次数: {}
                            │ 总运行时间: {}秒
                            │ 最终盈亏比: {}
                            └─────────────────────────────────────────────────────┘""",
                    stats.getStatus().getDescription(),
                    stats.getEntryPrice().setScale(2, RoundingMode.HALF_UP),
                    stats.getCurrentPrice().setScale(2, RoundingMode.HALF_UP),
                    stats.getCurrentReturn().setScale(2, RoundingMode.HALF_UP),
                    stats.getMaxReturnAchieved().setScale(2, RoundingMode.HALF_UP),
                    stats.getAdjustmentCount(),
                    java.time.Duration.between(stats.getStartTime(), stats.getLastUpdateTime()).getSeconds(),
                    stats.getRiskRewardRatio().setScale(2, RoundingMode.HALF_UP)
            );
        }

        //profitPercentage
        log.info("<TEST>进:{} 现:{} 益:{}  positionCalculationResult:{}", positionCalculationResult.getEntryPrice(), positionCalculationResult.getCurrentPrice(), positionCalculationResult.getProfitPercentage(), JSON.toJSONString(positionCalculationResult));

        if (ObjectUtil.isNotEmpty(metrics)) {
            log.info("  <TEST>currentPosition: {} metrics: {} ", JSON.toJSONString(currentPosition), JSON.toJSONString(metrics));
        }
    }

    /**
     * 计算交易信号
     */
    private Signal calculateSignal(Bar currentBar, Bar previousBar) {
        // 计算平均价
        Num avg4 = calculateAveragePrice(currentBar);
        Num avg4Prev = calculateAveragePrice(previousBar);

        // 生成信号
        boolean longSignal = avg4.isGreaterThan(avg4Prev);
        boolean shortSignal = avg4.isLessThan(avg4Prev);

        return Signal.builder()
                .longSignal(longSignal)
                .shortSignal(shortSignal)
                .avgPrice(avg4)
                .avgPricePrev(avg4Prev)
                .build();
    }

    /**
     * 计算平均价
     */
    private Num calculateAveragePrice(Bar bar) {
        if (bar == null) return DecimalNum.valueOf(0);

        return bar.getOpenPrice()
                .plus(bar.getHighPrice())
                .plus(bar.getLowPrice())
                .plus(bar.getClosePrice())
                .dividedBy(DecimalNum.valueOf(4));
    }

    /**
     * 开多仓
     */
    private void openLongPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        Num entryPrice = currentBar.getClosePrice();

        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 + triggerPoints / 100)));

        currentPosition = new Position(TradingSignal.LONG, entryPrice, stopLoss);
        log.info("<TEST>【开多仓】 入场价: {}, 止损价: {}, 时间: {} symbol:{}", entryPrice, stopLoss, currentBar.getEndTime(), state.getSymbolSwap());
        //tradingService.tradeByUsdtValue(state.getSymbol(), BUY_OPEN_LONG_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);

        metrics.put("lastAction", "OPEN_LONG");
        metrics.put("entryPrice", entryPrice.doubleValue());
        metrics.put("stopLoss", stopLoss.doubleValue());
    }

    /**
     * 开空仓
     */
    private void openShortPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        Num entryPrice = currentBar.getClosePrice();
        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 - triggerPoints / 100)));

        currentPosition = new Position(TradingSignal.SHORT, entryPrice, stopLoss);
        log.info("<TEST>【开空仓】 入场价: {}, 止损价: {}, 时间: {} symbol:{}",
                entryPrice, stopLoss, currentBar.getEndTime(), state.getSymbolSwap());
        //tradingService.tradeByUsdtValue(state.getSymbolSwap(), SELL_OPEN_SHORT_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);

        metrics.put("lastAction", "OPEN_SHORT");
        metrics.put("entryPrice", entryPrice.doubleValue());
        metrics.put("stopLoss", stopLoss.doubleValue());
    }

    /**
     * 更新持仓状态
     */
    private void updatePosition(Bar currentBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        if (currentPosition == null) return;

        Num currentPrice = currentBar.getClosePrice();
        Num profit = currentPosition.calculateProfit(currentPrice);

        // 检查移动止盈
        if (!currentPosition.isTrailingActivated()) {
            // 检查是否达到触发阈值
            if (profit.doubleValue() >= triggerPoints) {
                activateTrailingStop(currentBar);
            }
        } else {
            // 更新移动止盈
            updateTrailingStop(currentBar);
        }

        // 更新最高/最低价
        updateExtremePrices(currentBar);
    }

    /**
     * 检查止损
     */
    private boolean checkStopLoss(Num currentPrice) {
        if (currentPosition == null) return false;

        log.info("<TEST>stopLoss:{} currentPrice:{}", currentPosition.getStopLoss(), currentPrice);
        if (currentPosition.getType() == TradingSignal.LONG) {
            return currentPrice.isLessThanOrEqual(currentPosition.getStopLoss());
        } else {
            return currentPrice.isGreaterThanOrEqual(currentPosition.getStopLoss());
        }
    }

    /**
     * 激活移动止盈
     */
    private void activateTrailingStop(Bar currentBar) {
        currentPosition.setTrailingActivated(true);
        log.info("<TEST>【移动止盈激活】 当前价格: {}, 持仓类型: {}", currentBar.getClosePrice(), currentPosition.getType());
        metrics.put("trailingActivated", true);
    }

    /**
     * 更新移动止盈
     */
    private void updateTrailingStop(Bar currentBar) {
        if (!currentPosition.isTrailingActivated()) return;

        Num currentPrice = currentBar.getClosePrice();
        if (currentPosition.getType() == TradingSignal.LONG) {
            // 更新多仓移动止盈
            if (currentPrice.isGreaterThan(currentPosition.getHighestPrice())) {
                currentPosition.setHighestPrice(currentPrice);
                Num newStop = currentPrice.minus(DecimalNum.valueOf(trailOffset));
                currentPosition.setStopLoss(newStop);

                log.info("<TEST>更新多仓移动止盈: {}", newStop);
            }
        } else {
            // 更新空仓移动止盈
            if (currentPrice.isLessThan(currentPosition.getLowestPrice())) {
                currentPosition.setLowestPrice(currentPrice);
                Num newStop = currentPrice.plus(DecimalNum.valueOf(trailOffset));
                currentPosition.setStopLoss(newStop);

                log.info("<TEST>更新空仓移动止盈: {}", newStop);
            }
        }
    }

    /**
     * 更新最高/最低价
     */
    private void updateExtremePrices(Bar currentBar) {
        if (currentPosition == null) return;

        Num currentHigh = currentBar.getHighPrice();
        Num currentLow = currentBar.getLowPrice();

        if (currentPosition.getType() == TradingSignal.LONG) {
            if (currentHigh.isGreaterThan(currentPosition.getHighestPrice())) {
                log.info("<TEST>更新最高价: {}", currentHigh);
                currentPosition.setHighestPrice(currentHigh);
            }
        } else {
            if (currentLow.isLessThan(currentPosition.getLowestPrice())) {
                log.info("<TEST>更新最低价: {}", currentLow);
                currentPosition.setLowestPrice(currentLow);
            }
        }
    }

    /**
     * 平仓
     */
    private void closePosition(String reason, Num exitPrice, RealTimeStrategyEntity state, Candlestick candlestick) {
        if (currentPosition == null) return;

        Num profit = currentPosition.calculateProfit(exitPrice);

        log.info("<TEST>【平仓】 原因: {}, 入场价: {}, 止损价: {}, 出场价: {}, 盈亏: {}, 持仓时间: {}",
                reason, currentPosition.getEntryPrice(), currentPosition.getStopLoss(), exitPrice, profit,
                java.time.Duration.between(
                        currentPosition.getEntryTime(),
                        ZonedDateTime.now()
                ).toMinutes() + "分钟");
        //tradingService.closePosition(state.getSymbolSwap(), "OPEN_SHORT".equals(metrics.get("lastAction")) ? BUY_CLOSE_SHORT_ISOLATED : SELL_CLOSE_LONG_ISOLATED);

        metrics.put("lastAction", "CLOSE_POSITION");
        metrics.put("exitPrice", exitPrice.doubleValue());
        metrics.put("profit", profit.doubleValue());
        metrics.put("positionDuration",
                java.time.Duration.between(
                        currentPosition.getEntryTime(),
                        ZonedDateTime.now()
                ).toMinutes());

        currentPosition = null;
    }

    /**
     * 记录指标
     */
    private void recordMetrics(Bar currentBar) {
        metrics.put("currentPrice", currentBar.getClosePrice().doubleValue());
        metrics.put("timestamp", System.currentTimeMillis());

        if (currentPosition != null) {
            Num profit = currentPosition.calculateProfit(currentBar.getClosePrice());
            metrics.put("floatingProfit", profit.doubleValue());
            metrics.put("stopLoss", currentPosition.getStopLoss().doubleValue());
            metrics.put("positionType", currentPosition.getType().name());
        }
        log.info("metrics:{}", JSON.toJSONString(metrics));
    }

    /**
     * 获取策略指标
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

}
