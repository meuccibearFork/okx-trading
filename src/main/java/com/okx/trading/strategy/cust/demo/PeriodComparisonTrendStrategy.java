//package com.okx.trading.strategy.cust.demo;
//
//import com.okx.trading.model.entity.RealTimeStrategyEntity;
//import com.okx.trading.model.market.Candlestick;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//
///**
// * 周期比较趋势跟踪策略
// *
// * 策略逻辑：
// * 1. 比较当前周期和上一个周期的价格特征
// * 2. 基于价格趋势进行开仓/平仓决策
// * 3. 动态止损管理：初始止损2%，移动止损保护利润
// */
//@Slf4j
//@Data
//public class PeriodComparisonTrendStrategy extends BaseRealTimeStrategy {
//
//    // ========== 策略参数 ==========
//    private static final BigDecimal STOP_LOSS_PERCENT = new BigDecimal("0.02"); // 2%止损
//    private static final BigDecimal PROFIT_LOCK_PERCENT = new BigDecimal("0.02"); // 2%利润锁定
//    private static final BigDecimal TREND_THRESHOLD = new BigDecimal("0.001"); // 趋势阈值 0.1%
//
//    // ========== 策略状态 ==========
//    private Candlestick previousCandle;
//    private BigDecimal entryPrice;          // 入场价格
//    private BigDecimal initialStopLoss;     // 初始止损价
//    private BigDecimal currentStopLoss;     // 当前止损价
//    private BigDecimal highestProfitPrice;  // 最高盈利价格
//    private BigDecimal lowestProfitPrice;   // 最低盈利价格（空头）
//    private BigDecimal breakevenPrice;      // 保本价格
//
//    // ========== 统计数据 ==========
//    private int consecutiveSameSignals;     // 连续同向信号计数
//    private BigDecimal totalProfit;         // 累计盈利
//    private BigDecimal totalLoss;           // 累计亏损
//    private int winCount;                   // 胜率计数
//    private int lossCount;                  // 败率计数
//
//    // ========== 构造函数 ==========
//    public PeriodComparisonTrendStrategy(RealTimeStrategyEntity strategyEntity) {
//        super(strategyEntity);
//        initialize();
//    }
//
//    private void initialize() {
//        this.previousCandle = null;
//        this.entryPrice = BigDecimal.ZERO;
//        this.initialStopLoss = BigDecimal.ZERO;
//        this.currentStopLoss = BigDecimal.ZERO;
//        this.highestProfitPrice = BigDecimal.ZERO;
//        this.lowestProfitPrice = BigDecimal.ZERO;
//        this.breakevenPrice = BigDecimal.ZERO;
//        this.consecutiveSameSignals = 0;
//        this.totalProfit = BigDecimal.ZERO;
//        this.totalLoss = BigDecimal.ZERO;
//        this.winCount = 0;
//        this.lossCount = 0;
//    }
//
//    // ========== 策略核心方法 ==========
//
//    /**
//     * 处理新的K线数据
//     */
//    @Override
//    public Map<String, Object> processCandle(Candlestick currentCandle) {
//        Map<String, Object> result = new HashMap<>();
//
//        if (previousCandle == null) {
//            previousCandle = currentCandle;
//            result.put("signal", "WAIT");
//            result.put("message", "等待第一个周期完成");
//            return result;
//        }
//
//        // 计算价格特征
//        PriceFeatures currentFeatures = calculatePriceFeatures(currentCandle);
//        PriceFeatures previousFeatures = calculatePriceFeatures(previousCandle);
//
//        // 分析趋势信号
//        TrendSignal trendSignal = analyzeTrend(currentFeatures, previousFeatures);
//
//        // 如果没有持仓，检查开仓信号
//        if (!getStrategyEntity().getIsInPosition()) {
//            SignalResult openSignal = checkOpenSignal(currentCandle, trendSignal);
//            if (openSignal.shouldOpen()) {
//                result.put("signal", openSignal.getAction());
//                result.put("price", currentCandle.getClose());
//                result.put("stopLoss", openSignal.getStopLoss());
//                result.put("message", openSignal.getMessage());
//
//                // 更新入场信息
//                entryPrice = currentCandle.getClose();
//                initialStopLoss = openSignal.getStopLoss();
//                currentStopLoss = initialStopLoss;
//                updateBreakevenPrice();
//
//                log.info("开仓信号: 方向={}, 入场价={}, 初始止损={}",
//                        openSignal.getAction(), entryPrice, initialStopLoss);
//            } else {
//                result.put("signal", "WAIT");
//                result.put("message", "等待开仓信号");
//            }
//        } else {
//            // 如果有持仓，检查止损和移动止损
//            SignalResult manageSignal = managePosition(currentCandle, trendSignal);
//            if (manageSignal.shouldClose()) {
//                result.put("signal", manageSignal.getAction());
//                result.put("price", currentCandle.getClose());
//                result.put("stopLoss", currentStopLoss);
//                result.put("message", manageSignal.getMessage());
//
//                // 计算盈亏
//                BigDecimal exitPrice = currentCandle.getClose();
//                BigDecimal profit = calculateProfit(exitPrice);
//
//                log.info("平仓信号: 方向={}, 出场价={}, 盈亏={}, 累计盈亏={}",
//                        manageSignal.getAction(), exitPrice, profit, totalProfit);
//
//                // 重置持仓状态
//                resetPosition();
//            } else {
//                result.put("signal", "HOLD");
//                result.put("stopLoss", currentStopLoss);
//                result.put("message", manageSignal.getMessage());
//            }
//        }
//
//        // 更新前一根K线
//        previousCandle = currentCandle;
//        return result;
//    }
//
//    // ========== 价格特征计算 ==========
//
//    /**
//     * 计算价格特征
//     */
//    private PriceFeatures calculatePriceFeatures(Candlestick candle) {
//        PriceFeatures features = new PriceFeatures();
//
//        // 计算典型价格
//        features.typicalPrice = candle.getOpen()
//                .add(candle.getHigh())
//                .add(candle.getLow())
//                .add(candle.getClose())
//                .divide(new BigDecimal("4"), 8, RoundingMode.HALF_UP);
//
//        // 计算价格动量
//        if (previousCandle != null && candle != previousCandle) {
//            features.priceMomentum = candle.getClose()
//                    .subtract(previousCandle.getClose())
//                    .divide(previousCandle.getClose(), 8, RoundingMode.HALF_UP);
//        }
//
//        // 计算价格范围
//        features.priceRange = candle.getHigh().subtract(candle.getLow());
//        features.rangePercentage = features.priceRange.divide(candle.getOpen(), 8, RoundingMode.HALF_UP);
//
//        // 计算买卖压力
//        BigDecimal bodySize = candle.getClose().subtract(candle.getOpen()).abs();
//        BigDecimal upperShadow = candle.getHigh().subtract(candle.getClose().max(candle.getOpen()));
//        BigDecimal lowerShadow = candle.getOpen().min(candle.getClose()).subtract(candle.getLow());
//
//        features.buyPressure = lowerShadow.divide(bodySize.max(BigDecimal.ONE), 8, RoundingMode.HALF_UP);
//        features.sellPressure = upperShadow.divide(bodySize.max(BigDecimal.ONE), 8, RoundingMode.HALF_UP);
//
//        // 计算收盘位置
//        features.closePosition = candle.getClose().subtract(candle.getLow())
//                .divide(features.priceRange.max(BigDecimal.ONE), 8, RoundingMode.HALF_UP);
//
//        return features;
//    }
//
//    /**
//     * 价格特征类
//     */
//    @Data
//    private static class PriceFeatures {
//        private BigDecimal typicalPrice;      // 典型价格
//        private BigDecimal priceMomentum;     // 价格动量
//        private BigDecimal priceRange;        // 价格范围
//        private BigDecimal rangePercentage;   // 价格范围百分比
//        private BigDecimal buyPressure;       // 买压
//        private BigDecimal sellPressure;      // 卖压
//        private BigDecimal closePosition;     // 收盘位置（0-1）
//    }
//
//    // ========== 趋势分析 ==========
//
//    /**
//     * 分析趋势信号
//     */
//    private TrendSignal analyzeTrend(PriceFeatures current, PriceFeatures previous) {
//        TrendSignal signal = new TrendSignal();
//
//        // 比较典型价格
//        BigDecimal typicalDiff = current.getTypicalPrice().subtract(previous.getTypicalPrice());
//        BigDecimal typicalDiffPercent = typicalDiff.divide(previous.getTypicalPrice(), 8, RoundingMode.HALF_UP);
//
//        signal.typicalTrend = typicalDiffPercent.compareTo(TREND_THRESHOLD) > 0 ? "UP" :
//                              typicalDiffPercent.compareTo(TREND_THRESHOLD.negate()) < 0 ? "DOWN" : "NEUTRAL";
//
//        // 分析价格动量
//        if (current.getPriceMomentum() != null) {
//            signal.momentumStrength = current.getPriceMomentum().abs();
//            signal.momentumDirection = current.getPriceMomentum().compareTo(BigDecimal.ZERO) > 0 ? "UP" : "DOWN";
//        }
//
//        // 分析买卖压力
//        BigDecimal pressureDiff = current.getBuyPressure().subtract(current.getSellPressure());
//        signal.pressureTrend = pressureDiff.compareTo(BigDecimal.ZERO) > 0 ? "UP" : "DOWN";
//
//        // 分析价格范围
//        signal.rangeExpansion = current.getRangePercentage().compareTo(previous.getRangePercentage()) > 0;
//
//        // 综合判断
//        signal.combinedSignal = calculateCombinedSignal(signal);
//        signal.confidence = calculateConfidence(current, previous, signal);
//
//        return signal;
//    }
//
//    /**
//     * 计算综合信号
//     */
//    private String calculateCombinedSignal(TrendSignal signal) {
//        int upCount = 0;
//        int downCount = 0;
//
//        if ("UP".equals(signal.getTypicalTrend())) upCount++;
//        if ("DOWN".equals(signal.getTypicalTrend())) downCount++;
//
//        if ("UP".equals(signal.getMomentumDirection())) upCount++;
//        if ("DOWN".equals(signal.getMomentumDirection())) downCount++;
//
//        if ("UP".equals(signal.getPressureTrend())) upCount++;
//        if ("DOWN".equals(signal.getPressureTrend())) downCount++;
//
//        if (upCount > downCount && upCount >= 2) return "BULLISH";
//        if (downCount > upCount && downCount >= 2) return "BEARISH";
//        return "NEUTRAL";
//    }
//
//    /**
//     * 计算信号置信度
//     */
//    private BigDecimal calculateConfidence(PriceFeatures current, PriceFeatures previous, TrendSignal signal) {
//        BigDecimal confidence = BigDecimal.ZERO;
//
//        // 典型价格变化幅度
//        BigDecimal typicalChange = current.getTypicalPrice().subtract(previous.getTypicalPrice()).abs()
//                .divide(previous.getTypicalPrice(), 8, RoundingMode.HALF_UP);
//        confidence = confidence.add(typicalChange.multiply(new BigDecimal("0.3")));
//
//        // 动量强度
//        if (signal.getMomentumStrength() != null) {
//            confidence = confidence.add(signal.getMomentumStrength().multiply(new BigDecimal("0.3")));
//        }
//
//        // 买卖压力差
//        BigDecimal pressureDiff = current.getBuyPressure().subtract(current.getSellPressure()).abs();
//        confidence = confidence.add(pressureDiff.multiply(new BigDecimal("0.2")));
//
//        // 价格范围扩张
//        if (signal.isRangeExpansion()) {
//            confidence = confidence.add(new BigDecimal("0.2"));
//        }
//
//        return confidence.min(new BigDecimal("1.0"));
//    }
//
//    /**
//     * 趋势信号类
//     */
//    @Data
//    private static class TrendSignal {
//        private String typicalTrend;      // 典型价格趋势
//        private String momentumDirection; // 动量方向
//        private BigDecimal momentumStrength; // 动量强度
//        private String pressureTrend;     // 买卖压力趋势
//        private boolean rangeExpansion;   // 价格范围是否扩张
//        private String combinedSignal;    // 综合信号
//        private BigDecimal confidence;    // 信号置信度
//    }
//
//    // ========== 开仓逻辑 ==========
//
//    /**
//     * 检查开仓信号
//     */
//    private SignalResult checkOpenSignal(Candlestick currentCandle, TrendSignal trendSignal) {
//        SignalResult result = new SignalResult();
//
//        // 需要置信度达到阈值
//        if (trendSignal.getConfidence().compareTo(new BigDecimal("0.6")) < 0) {
//            result.setMessage("置信度过低: " + trendSignal.getConfidence());
//            return result;
//        }
//
//        // 检查连续信号
//        if (!isConsecutiveSignal(trendSignal.getCombinedSignal())) {
//            result.setMessage("连续信号不足");
//            return result;
//        }
//
//        // 确定开仓方向和价格
//        BigDecimal currentPrice = currentCandle.getClose();
//
//        if ("BULLISH".equals(trendSignal.getCombinedSignal())) {
//            result.setAction("BUY");
//            result.setStopLoss(calculateStopLoss(currentPrice, "BUY"));
//            result.setMessage(String.format("看多信号: 置信度=%.2f%%, 连续信号=%d",
//                    trendSignal.getConfidence().multiply(new BigDecimal("100")),
//                    consecutiveSameSignals));
//        } else if ("BEARISH".equals(trendSignal.getCombinedSignal())) {
//            result.setAction("SELL");
//            result.setStopLoss(calculateStopLoss(currentPrice, "SELL"));
//            result.setMessage(String.format("看空信号: 置信度=%.2f%%, 连续信号=%d",
//                    trendSignal.getConfidence().multiply(new BigDecimal("100")),
//                    consecutiveSameSignals));
//        } else {
//            result.setMessage("中性信号");
//        }
//
//        return result;
//    }
//
//    /**
//     * 检查是否为连续信号
//     */
//    private boolean isConsecutiveSignal(String currentSignal) {
//        // 这里简化处理，实际中应该记录历史信号
//        // 假设连续2个同向信号为有效
//        if (consecutiveSameSignals >= 2) {
//            consecutiveSameSignals = 0; // 重置计数
//            return true;
//        }
//
//        // 更新计数
//        if ("BULLISH".equals(currentSignal) || "BEARISH".equals(currentSignal)) {
//            consecutiveSameSignals++;
//        } else {
//            consecutiveSameSignals = 0;
//        }
//
//        return false;
//    }
//
//    /**
//     * 计算止损价格
//     */
//    private BigDecimal calculateStopLoss(BigDecimal entryPrice, String direction) {
//        BigDecimal stopLossDistance = entryPrice.multiply(STOP_LOSS_PERCENT);
//
//        if ("BUY".equals(direction)) {
//            return entryPrice.subtract(stopLossDistance);
//        } else { // SELL
//            return entryPrice.add(stopLossDistance);
//        }
//    }
//
//    // ========== 持仓管理 ==========
//
//    /**
//     * 管理持仓
//     */
//    private SignalResult managePosition(Candlestick currentCandle, TrendSignal trendSignal) {
//        SignalResult result = new SignalResult();
//        BigDecimal currentPrice = currentCandle.getClose();
//
//        // 检查是否触发止损
//        if (isStopLossTriggered(currentPrice)) {
//            result.setAction(getStrategyEntity().getIsLongPosition() ? "SELL" : "BUY");
//            result.setMessage(String.format("止损触发: 当前价=%s, 止损价=%s",
//                    currentPrice, currentStopLoss));
//            return result;
//        }
//
//        // 更新最高/最低盈利价格
//        updateExtremePrices(currentPrice);
//
//        // 检查是否应该移动止损
//        if (shouldMoveStopLoss(currentPrice)) {
//            moveStopLoss(currentPrice);
//            result.setMessage(String.format("移动止损: 新止损价=%s, 盈利=%s%%",
//                    currentStopLoss,
//                    calculateProfitPercentage(currentPrice).multiply(new BigDecimal("100"))));
//        } else {
//            result.setMessage(String.format("持有中: 当前价=%s, 止损=%s, 盈利=%s%%",
//                    currentPrice, currentStopLoss,
//                    calculateProfitPercentage(currentPrice).multiply(new BigDecimal("100"))));
//        }
//
//        // 检查趋势反转（可选平仓条件）
//        if (isTrendReversed(trendSignal)) {
//            result.setAction(getStrategyEntity().getIsLongPosition() ? "SELL" : "BUY");
//            result.setMessage("趋势反转，平仓离场");
//        }
//
//        return result;
//    }
//
//    /**
//     * 检查止损是否触发
//     */
//    private boolean isStopLossTriggered(BigDecimal currentPrice) {
//        if (getStrategyEntity().getIsLongPosition()) {
//            // 多头：价格低于止损价
//            return currentPrice.compareTo(currentStopLoss) <= 0;
//        } else {
//            // 空头：价格高于止损价
//            return currentPrice.compareTo(currentStopLoss) >= 0;
//        }
//    }
//
//    /**
//     * 更新极端价格
//     */
//    private void updateExtremePrices(BigDecimal currentPrice) {
//        if (getStrategyEntity().getIsLongPosition()) {
//            // 多头：更新最高价
//            if (highestProfitPrice.compareTo(BigDecimal.ZERO) == 0 ||
//                currentPrice.compareTo(highestProfitPrice) > 0) {
//                highestProfitPrice = currentPrice;
//            }
//        } else {
//            // 空头：更新最低价
//            if (lowestProfitPrice.compareTo(BigDecimal.ZERO) == 0 ||
//                currentPrice.compareTo(lowestProfitPrice) < 0) {
//                lowestProfitPrice = currentPrice;
//            }
//        }
//    }
//
//    /**
//     * 检查是否应该移动止损
//     */
//    private boolean shouldMoveStopLoss(BigDecimal currentPrice) {
//        if (getStrategyEntity().getIsLongPosition()) {
//            // 多头逻辑
//            if (breakevenPrice.compareTo(BigDecimal.ZERO) == 0) {
//                // 尚未保本，检查是否达到2%盈利
//                BigDecimal profitPercent = calculateProfitPercentage(currentPrice);
//                return profitPercent.compareTo(PROFIT_LOCK_PERCENT) >= 0;
//            } else {
//                // 已保本，检查是否再有2%盈利提升
//                BigDecimal profitFromBreakeven = currentPrice.subtract(breakevenPrice)
//                        .divide(breakevenPrice, 8, RoundingMode.HALF_UP);
//                return profitFromBreakeven.compareTo(PROFIT_LOCK_PERCENT) >= 0;
//            }
//        } else {
//            // 空头逻辑
//            if (breakevenPrice.compareTo(BigDecimal.ZERO) == 0) {
//                BigDecimal profitPercent = calculateProfitPercentage(currentPrice);
//                return profitPercent.compareTo(PROFIT_LOCK_PERCENT) >= 0;
//            } else {
//                BigDecimal profitFromBreakeven = breakevenPrice.subtract(currentPrice)
//                        .divide(breakevenPrice, 8, RoundingMode.HALF_UP);
//                return profitFromBreakeven.compareTo(PROFIT_LOCK_PERCENT) >= 0;
//            }
//        }
//    }
//
//    /**
//     * 移动止损
//     */
//    private void moveStopLoss(BigDecimal currentPrice) {
//        if (getStrategyEntity().getIsLongPosition()) {
//            // 多头：将止损移动到新的保本价格
//            if (breakevenPrice.compareTo(BigDecimal.ZERO) == 0) {
//                // 第一次移动：移动到入场价
//                currentStopLoss = entryPrice;
//                breakevenPrice = entryPrice;
//                log.info("首次移动止损到保本价: {}", entryPrice);
//            } else {
//                // 后续移动：每次再上移2%
//                BigDecimal newStopLoss = breakevenPrice.multiply(BigDecimal.ONE.add(PROFIT_LOCK_PERCENT));
//                if (newStopLoss.compareTo(currentStopLoss) > 0) {
//                    currentStopLoss = newStopLoss;
//                    breakevenPrice = newStopLoss;
//                    log.info("移动止损到新位置: {}", newStopLoss);
//                }
//            }
//        } else {
//            // 空头：将止损移动到新的保本价格
//            if (breakevenPrice.compareTo(BigDecimal.ZERO) == 0) {
//                currentStopLoss = entryPrice;
//                breakevenPrice = entryPrice;
//                log.info("首次移动止损到保本价: {}", entryPrice);
//            } else {
//                BigDecimal newStopLoss = breakevenPrice.multiply(BigDecimal.ONE.subtract(PROFIT_LOCK_PERCENT));
//                if (newStopLoss.compareTo(currentStopLoss) < 0) {
//                    currentStopLoss = newStopLoss;
//                    breakevenPrice = newStopLoss;
//                    log.info("移动止损到新位置: {}", newStopLoss);
//                }
//            }
//        }
//    }
//
//    /**
//     * 检查趋势是否反转
//     */
//    private boolean isTrendReversed(TrendSignal trendSignal) {
//        if (getStrategyEntity().getIsLongPosition()) {
//            // 多头持仓，检查是否出现看空信号
//            return "BEARISH".equals(trendSignal.getCombinedSignal()) &&
//                   trendSignal.getConfidence().compareTo(new BigDecimal("0.7")) > 0;
//        } else {
//            // 空头持仓，检查是否出现看多信号
//            return "BULLISH".equals(trendSignal.getCombinedSignal()) &&
//                   trendSignal.getConfidence().compareTo(new BigDecimal("0.7")) > 0;
//        }
//    }
//
//    // ========== 盈亏计算 ==========
//
//    /**
//     * 计算盈亏
//     */
//    private BigDecimal calculateProfit(BigDecimal exitPrice) {
//        BigDecimal profit;
//
//        if (getStrategyEntity().getIsLongPosition()) {
//            profit = exitPrice.subtract(entryPrice);
//        } else {
//            profit = entryPrice.subtract(exitPrice);
//        }
//
//        // 更新统计数据
//        if (profit.compareTo(BigDecimal.ZERO) > 0) {
//            totalProfit = totalProfit.add(profit);
//            winCount++;
//        } else {
//            totalLoss = totalLoss.add(profit.abs());
//            lossCount++;
//        }
//
//        return profit;
//    }
//
//    /**
//     * 计算盈利百分比
//     */
//    private BigDecimal calculateProfitPercentage(BigDecimal currentPrice) {
//        if (getStrategyEntity().getIsLongPosition()) {
//            return currentPrice.subtract(entryPrice).divide(entryPrice, 8, RoundingMode.HALF_UP);
//        } else {
//            return entryPrice.subtract(currentPrice).divide(entryPrice, 8, RoundingMode.HALF_UP);
//        }
//    }
//
//    /**
//     * 更新保本价格
//     */
//    private void updateBreakevenPrice() {
//        if (getStrategyEntity().getIsLongPosition()) {
//            breakevenPrice = entryPrice; // 初始保本价为入场价
//        } else {
//            breakevenPrice = entryPrice;
//        }
//    }
//
//    // ========== 辅助方法 ==========
//
//    /**
//     * 重置持仓状态
//     */
//    private void resetPosition() {
//        entryPrice = BigDecimal.ZERO;
//        initialStopLoss = BigDecimal.ZERO;
//        currentStopLoss = BigDecimal.ZERO;
//        highestProfitPrice = BigDecimal.ZERO;
//        lowestProfitPrice = BigDecimal.ZERO;
//        breakevenPrice = BigDecimal.ZERO;
//        consecutiveSameSignals = 0;
//    }
//
//    /**
//     * 获取策略统计数据
//     */
//    public Map<String, Object> getStatistics() {
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("totalProfit", totalProfit);
//        stats.put("totalLoss", totalLoss);
//        stats.put("netProfit", totalProfit.subtract(totalLoss));
//        stats.put("winCount", winCount);
//        stats.put("lossCount", lossCount);
//        stats.put("winRate", winCount + lossCount > 0 ?
//                new BigDecimal(winCount).divide(new BigDecimal(winCount + lossCount), 4, RoundingMode.HALF_UP) :
//                BigDecimal.ZERO);
//        stats.put("profitFactor", totalLoss.compareTo(BigDecimal.ZERO) > 0 ?
//                totalProfit.divide(totalLoss, 4, RoundingMode.HALF_UP) :
//                BigDecimal.ZERO);
//
//        return stats;
//    }
//
//    // ========== 信号结果类 ==========
//
//    @Data
//    private static class SignalResult {
//        private String action;        // 动作: BUY, SELL, HOLD, WAIT
//        private BigDecimal stopLoss;  // 止损价
//        private String message;       // 消息
//
//        public boolean shouldOpen() {
//            return "BUY".equals(action) || "SELL".equals(action);
//        }
//
//        public boolean shouldClose() {
//            return "BUY".equals(action) || "SELL".equals(action);
//        }
//    }
//
//    // ========== 基类方法实现 ==========
//
//    @Override
//    public String getStrategyCode() {
//        return "PERIOD_COMPARISON_TREND";
//    }
//
//    @Override
//    public String getStrategyName() {
//        return "周期比较趋势跟踪策略";
//    }
//
//    @Override
//    public String getStrategyDescription() {
//        return "基于当前周期和上一个周期的价格特征比较，进行趋势跟踪交易，采用2%动态止损管理";
//    }
//
//    @Override
//    public Map<String, Object> getStrategyParameters() {
//        Map<String, Object> params = new HashMap<>();
//        params.put("stopLossPercent", STOP_LOSS_PERCENT);
//        params.put("profitLockPercent", PROFIT_LOCK_PERCENT);
//        params.put("trendThreshold", TREND_THRESHOLD);
//        params.put("minConfidence", new BigDecimal("0.6"));
//        params.put("consecutiveSignals", 2);
//        return params;
//    }
//}
