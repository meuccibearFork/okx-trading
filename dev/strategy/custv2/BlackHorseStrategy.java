package com.okx.trading.strategy.custv2;

import com.okx.trading.strategy.custv2.utils.Ta4jUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.num.Num;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Data
public class BlackHorseStrategy {

    // 策略参数
    private double triggerPoints = 10.0;      // 触发移动止盈的盈利阈值
    private double trailOffset = 10.0;        // 移动止盈偏移量
    private double trailPoints = 10.0;        // 移动止盈点数

    // 状态跟踪
    private BarSeries barSeries;
    private Position longPosition;
    private Position shortPosition;
    private Map<String, Object> positionInfo = new HashMap<>();

    public BlackHorseStrategy() {
        this.barSeries = Ta4jUtil.createBarSeries("OKX_4H");
    }

    /**
     * 处理新K线数据
     */
    public void processNewBar(Bar bar) {
        // 添加新K线
        barSeries.addBar(bar);

        if (barSeries.getBarCount() < 2) {
            return;
        }

        // 获取当前和前一根K线
        int currentIndex = barSeries.getEndIndex();
        Bar currentBar = barSeries.getBar(currentIndex);
        Bar previousBar = barSeries.getBar(currentIndex - 1);

        // 检查是否是新4小时K线
        boolean isNew4HBar = Ta4jUtil.isNewBar(currentBar, previousBar);

        if (isNew4HBar) {
            // 计算平均价
            Num avg4 = Ta4jUtil.calculateAveragePrice(currentBar);
            Num avg4Prev = Ta4jUtil.calculateAveragePrice(previousBar);

            // 生成信号
            boolean longSignal = avg4.isGreaterThan(avg4Prev);
            boolean shortSignal = avg4.isLessThan(avg4Prev);

            // 获取上一根K线的高低点
            Num prevHigh = previousBar.getHighPrice();
            Num prevLow = previousBar.getLowPrice();

            // 检查并执行交易
            checkAndExecuteTrades(longSignal, shortSignal, prevHigh, prevLow);
        }

        // 检查移动止盈条件
        checkTrailingStop();

        // 记录信号
        logSignals(currentIndex);
    }

    /**
     * 检查并执行交易
     */
    private void checkAndExecuteTrades(boolean longSignal, boolean shortSignal,
                                       Num prevHigh, Num prevLow) {
        // 无持仓时开仓
        if (!hasPosition()) {
            if (longSignal) {
                openLongPosition(prevLow);
                log.info("开多仓，初始止损: {}", prevLow);
            } else if (shortSignal) {
                openShortPosition(prevHigh);
                log.info("开空仓，初始止损: {}", prevHigh);
            }
        }
    }

    /**
     * 开多仓
     */
    private void openLongPosition(Num stopLoss) {
        Bar currentBar = barSeries.getLastBar();
        longPosition = new Position(currentBar.getClosePrice(), stopLoss, null , true);

        positionInfo.put("positionType", "LONG");
        positionInfo.put("entryPrice", currentBar.getClosePrice().doubleValue());
        positionInfo.put("stopLoss", stopLoss.doubleValue());
        positionInfo.put("trailingActivated", false);
    }

    /**
     * 开空仓
     */
    private void openShortPosition(Num stopLoss) {
        Bar currentBar = barSeries.getLastBar();
        shortPosition = new Position(currentBar.getClosePrice(), stopLoss, null , true);

        positionInfo.put("positionType", "SHORT");
        positionInfo.put("entryPrice", currentBar.getClosePrice().doubleValue());
        positionInfo.put("stopLoss", stopLoss.doubleValue());
        positionInfo.put("trailingActivated", false);
    }

    /**
     * 检查移动止盈条件
     */
    private void checkTrailingStop() {
        if (!hasPosition()) return;

        Bar currentBar = barSeries.getLastBar();
        double profitPoints = calculateProfitPoints(currentBar);

        if (profitPoints >= triggerPoints && !(boolean) positionInfo.get("trailingActivated")) {
            activateTrailingStop(currentBar);
            log.info("移动止盈激活，当前盈利点数: {}", profitPoints);
        }

        if ((boolean) positionInfo.get("trailingActivated")) {
            updateTrailingStop(currentBar);
        }
    }

    /**
     * 计算盈利点数
     */
    private double calculateProfitPoints(Bar currentBar) {
        double entryPrice = (double) positionInfo.get("entryPrice");
        double currentPrice = currentBar.getClosePrice().doubleValue();
        String positionType = (String) positionInfo.get("positionType");

        if ("LONG".equals(positionType)) {
            return currentPrice - entryPrice;
        } else if ("SHORT".equals(positionType)) {
            return entryPrice - currentPrice;
        }
        return 0.0;
    }

    /**
     * 激活移动止盈
     */
    private void activateTrailingStop(Bar currentBar) {
        positionInfo.put("trailingActivated", true);
        positionInfo.put("highestPrice", currentBar.getHighPrice().doubleValue());
        positionInfo.put("lowestPrice", currentBar.getLowPrice().doubleValue());
    }

    /**
     * 更新移动止盈
     */
    private void updateTrailingStop(Bar currentBar) {
        double currentHigh = currentBar.getHighPrice().doubleValue();
        double currentLow = currentBar.getLowPrice().doubleValue();
        String positionType = (String) positionInfo.get("positionType");

        if ("LONG".equals(positionType)) {
            double highestPrice = (double) positionInfo.get("highestPrice");
            if (currentHigh > highestPrice) {
                positionInfo.put("highestPrice", currentHigh);
                double newStop = currentHigh - trailOffset;
                positionInfo.put("stopLoss", newStop);
                log.debug("更新多仓移动止盈: {}", newStop);
            }
        } else if ("SHORT".equals(positionType)) {
            double lowestPrice = (double) positionInfo.get("lowestPrice");
            if (currentLow < lowestPrice) {
                positionInfo.put("lowestPrice", currentLow);
                double newStop = currentLow + trailOffset;
                positionInfo.put("stopLoss", newStop);
                log.debug("更新空仓移动止盈: {}", newStop);
            }
        }

        // 检查止损是否触发
        checkStopLoss(currentBar);
    }

    /**
     * 检查止损
     */
    private void checkStopLoss(Bar currentBar) {
        double currentPrice = currentBar.getClosePrice().doubleValue();
        double stopLoss = (double) positionInfo.get("stopLoss");
        String positionType = (String) positionInfo.get("positionType");

        if ("LONG".equals(positionType) && currentPrice <= stopLoss) {
            closePosition("止损触发");
        } else if ("SHORT".equals(positionType) && currentPrice >= stopLoss) {
            closePosition("止损触发");
        }
    }

    /**
     * 平仓
     */
    private void closePosition(String reason) {
        log.info("平仓: {}", reason);
        longPosition = null;
        shortPosition = null;
        positionInfo.clear();
    }

    /**
     * 检查是否有持仓
     */
    private boolean hasPosition() {
        return longPosition != null || shortPosition != null;
    }

    /**
     * 记录信号
     */
    private void logSignals(int barIndex) {
        // 这里可以添加信号记录逻辑
        // 比如保存到数据库或发送通知
    }

    /**
     * 内部持仓类
     */
    @Data
    @lombok.Builder
    private static class Position {
        private Num entry;
        private Num stopLoss;
        private Num trailingStop;
        private boolean trailingActivated;
    }
}
