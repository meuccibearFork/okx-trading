package com.okx.trading.strategy.cust;

import com.okx.trading.constant.TradingSignal;
import com.okx.trading.model.trade.OrderRequest;
import com.okx.trading.service.OkxApiService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义-策略工厂 - 高级策略集合
 */
@Slf4j
@Component
public class CustomizeStrategyFactory {

    // 策略参数
    private double triggerPoints = 10.0;
    private double trailOffset = 10.0;
    private double trailPoints = 10.0;

    // 核心数据
    private BarSeries barSeries;
    private Position currentPosition;
    private final Map<String, Object> metrics = new HashMap<>();

    @Resource
    OkxApiService okxApiService;

    public  Strategy yjwStrategy(BarSeries barSeries) {
        try {
            // 获取最新的Bar
            int endIndex = barSeries.getEndIndex();
            Bar currentBar = barSeries.getBar(endIndex);
            Bar previousBar = barSeries.getBar(endIndex - 1);

            // 检查是否是新K线
            boolean isNewBar = !currentBar.getEndTime().equals(previousBar.getEndTime());

            if (isNewBar) {
                // 计算信号
                Signal signal = calculateSignal(currentBar, previousBar);

                // 执行策略
                executeStrategy(signal, currentBar, previousBar);
            }

            // 更新持仓状态
            updatePosition(currentBar);

            // 记录指标
            recordMetrics(currentBar);

        } catch (Exception e) {
            log.error("处理K线数据失败", e);
        }

        return null;
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
     * 执行策略
     */
    private void executeStrategy(Signal signal, Bar currentBar, Bar previousBar) {
        // 无持仓时开仓
        if (currentPosition == null) {
            if (signal.isLongSignal()) {
                openLongPosition(currentBar, previousBar);
            } else if (signal.isShortSignal()) {
                openShortPosition(currentBar, previousBar);
            }
        }
    }

    /**
     * 开多仓
     */
    private void openLongPosition(Bar currentBar, Bar previousBar) {
        Num entryPrice = currentBar.getClosePrice();
        Num stopLoss = previousBar.getLowPrice();

        currentPosition = new Position(TradingSignal.LONG, entryPrice, stopLoss);

        log.info("【开多仓】 入场价: {}, 止损价: {}, 时间: {}",
                entryPrice, stopLoss, currentBar.getEndTime());

        metrics.put("lastAction", "OPEN_LONG");
        metrics.put("entryPrice", entryPrice.doubleValue());
        metrics.put("stopLoss", stopLoss.doubleValue());

        okxApiService.createSpotOrder(new OrderRequest());
    }

    /**
     * 开空仓
     */
    private void openShortPosition(Bar currentBar, Bar previousBar) {
        Num entryPrice = currentBar.getClosePrice();
        Num stopLoss = previousBar.getHighPrice();

        currentPosition = new Position(TradingSignal.SHORT, entryPrice, stopLoss);

        log.info("【开空仓】 入场价: {}, 止损价: {}, 时间: {}",
                entryPrice, stopLoss, currentBar.getEndTime());

        metrics.put("lastAction", "OPEN_SHORT");
        metrics.put("entryPrice", entryPrice.doubleValue());
        metrics.put("stopLoss", stopLoss.doubleValue());
    }

    /**
     * 更新持仓状态
     */
    private void updatePosition(Bar currentBar) {
        if (currentPosition == null) return;

        Num currentPrice = currentBar.getClosePrice();
        Num profit = currentPosition.calculateProfit(currentPrice);

        // 检查止损
        if (checkStopLoss(currentPrice)) {
            closePosition("止损触发", currentPrice);
            return;
        }

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

        log.info("【移动止盈激活】 当前价格: {}, 持仓类型: {}",
                currentBar.getClosePrice(), currentPosition.getType());

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

                log.info("更新多仓移动止盈: {}", newStop);
            }
        } else {
            // 更新空仓移动止盈
            if (currentPrice.isLessThan(currentPosition.getLowestPrice())) {
                currentPosition.setLowestPrice(currentPrice);
                Num newStop = currentPrice.plus(DecimalNum.valueOf(trailOffset));
                currentPosition.setStopLoss(newStop);

                log.info("更新空仓移动止盈: {}", newStop);
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
                currentPosition.setHighestPrice(currentHigh);
            }
        } else {
            if (currentLow.isLessThan(currentPosition.getLowestPrice())) {
                currentPosition.setLowestPrice(currentLow);
            }
        }
    }

    /**
     * 平仓
     */
    private void closePosition(String reason, Num exitPrice) {
        if (currentPosition == null) return;

        Num profit = currentPosition.calculateProfit(exitPrice);

        log.info("【平仓】 原因: {}, 出场价: {}, 盈亏: {}, 持仓时间: {}",
                reason, exitPrice, profit,
                java.time.Duration.between(
                        currentPosition.getEntryTime(),
                        ZonedDateTime.now()
                ).toMinutes() + "分钟");

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
    }

    /**
     * 获取策略指标
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

}
