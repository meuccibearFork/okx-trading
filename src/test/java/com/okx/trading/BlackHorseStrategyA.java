//package com.okx.trading;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * K线数据类
// */
//class Bar {
//    public double open, high, low, close;
//    public long time; // 时间戳（毫秒）
//
//    public Bar(double open, double high, double low, double close, long time) {
//        this.open = open;
//        this.high = high;
//        this.low = low;
//        this.close = close;
//        this.time = time;
//    }
//}
//
///**
// * 黑马策略A - Java实现
// */
//public class BlackHorseStrategyA {
//    // 策略参数
//    private final int htfMinutes = 240;      // 4小时
//    private final double triggerPoints;
//    private final double trailOffset;
//    private final double trailPoints;
//
//    // 状态变量
//    private int position = 0;                // 0:空仓, 1:多, -1:空
//    private double entryPrice = 0.0;
//    private double stopLossPrice = 0.0;      // 当前止损价
//    private double highestSinceEntry = 0.0;  // 多头持仓期间最高价
//    private double lowestSinceEntry = 0.0;   // 空头持仓期间最低价
//    private double currentProfitPoints = 0.0;
//
//    // 4H数据缓存
//    private Bar currentHtfBar = null;
//    private Bar previousHtfBar = null;
//    private boolean isNewHtfBar = false;
//    private long lastHtfTime = 0;
//
//    // 上一根4H的最高/最低（用于初始止损）
//    private double prevHtfHigh = 0.0;
//    private double prevHtfLow = 0.0;
//
//    public BlackHorseStrategyA(double triggerPoints, double trailOffset, double trailPoints) {
//        this.triggerPoints = triggerPoints;
//        this.trailOffset = trailOffset;
//        this.trailPoints = trailPoints;
//    }
//
//    /**
//     * 处理每根K线（可以是任意时间框架，但需要识别4H新K线）
//     * @param bar 当前K线
//     */
//    public void onBar(Bar bar) {
//        // 1. 更新4H数据
//        updateHtfData(bar);
//
//        // 2. 如果无持仓，检查开仓信号
//        if (position == 0) {
//            if (isNewHtfBar && currentHtfBar != null && previousHtfBar != null) {
//                double avgCurrent = (currentHtfBar.open + currentHtfBar.high + currentHtfBar.low + currentHtfBar.close) / 4.0;
//                double avgPrev = (previousHtfBar.open + previousHtfBar.high + previousHtfBar.low + previousHtfBar.close) / 4.0;
//                if (avgCurrent > avgPrev) {
//                    openLong(bar.close);
//                } else if (avgCurrent < avgPrev) {
//                    openShort(bar.close);
//                }
//            }
//        } else {
//            // 3. 持仓时，更新浮动盈亏
//            updateProfit(bar.close);
//
//            // 4. 检查是否应切换为移动止盈
//            if (currentProfitPoints >= triggerPoints) {
//                // 若当前仍使用固定止损，则切换为移动止损
//                // （实际上，每次onBar都会检查并可能重新设置移动止损）
//                enableTrailingStop(bar);
//            }
//
//            // 5. 检查止损是否触发
//            checkStopLoss(bar);
//        }
//
//        // 保存上一根4H用于下一轮
//        if (isNewHtfBar && currentHtfBar != null) {
//            previousHtfBar = currentHtfBar;
//            // 保存前一根的最高/最低用于开仓止损
//            prevHtfHigh = previousHtfBar.high;
//            prevHtfLow = previousHtfBar.low;
//        }
//    }
//
//    /**
//     * 更新4H数据，判断是否出现新的4H K线
//     */
//    private void updateHtfData(Bar bar) {
//        long barTime = bar.time;
//        // 计算当前时间所属4H周期的起始时间（对齐到整点）
//        long periodStart = (barTime / (htfMinutes * 60 * 1000)) * (htfMinutes * 60 * 1000);
//        if (periodStart != lastHtfTime) {
//            // 新的4H周期开始
//            if (currentHtfBar != null) {
//                previousHtfBar = currentHtfBar; // 旧的变为前一根
//            }
//            // 创建新的4H K线（当前周期的第一根）
//            currentHtfBar = new Bar(bar.open, bar.high, bar.low, bar.close, periodStart);
//            lastHtfTime = periodStart;
//            isNewHtfBar = true;
//        } else {
//            // 仍在同一4H周期内，更新OHLC
//            if (currentHtfBar != null) {
//                currentHtfBar.high = Math.max(currentHtfBar.high, bar.high);
//                currentHtfBar.low = Math.min(currentHtfBar.low, bar.low);
//                currentHtfBar.close = bar.close;
//                // open保持不变
//            }
//            isNewHtfBar = false;
//        }
//    }
//
//    /**
//     * 开多单
//     */
//    private void openLong(double price) {
//        position = 1;
//        entryPrice = price;
//        stopLossPrice = prevHtfLow; // 初始止损为前一根4H最低价
//        highestSinceEntry = price;
//        lowestSinceEntry = price;
//        currentProfitPoints = 0.0;
//        System.out.printf("开多 @ %.2f, 初始止损 %.2f%n", price, stopLossPrice);
//        // 实际交易执行（如发送订单）
//    }
//
//    /**
//     * 开空单
//     */
//    private void openShort(double price) {
//        position = -1;
//        entryPrice = price;
//        stopLossPrice = prevHtfHigh; // 初始止损为前一根4H最高价
//        highestSinceEntry = price;
//        lowestSinceEntry = price;
//        currentProfitPoints = 0.0;
//        System.out.printf("开空 @ %.2f, 初始止损 %.2f%n", price, stopLossPrice);
//    }
//
//    /**
//     * 更新浮动盈亏（点数）
//     */
//    private void updateProfit(double currentPrice) {
//        if (position == 1) {
//            currentProfitPoints = currentPrice - entryPrice;
//            highestSinceEntry = Math.max(highestSinceEntry, currentPrice);
//        } else if (position == -1) {
//            currentProfitPoints = entryPrice - currentPrice;
//            lowestSinceEntry = Math.min(lowestSinceEntry, currentPrice);
//        } else {
//            currentProfitPoints = 0.0;
//        }
//    }
//
//    /**
//     * 启用移动止盈（将固定止损替换为动态跟踪止损）
//     */
//    private void enableTrailingStop(Bar bar) {
//        if (position == 1) {
//            // 多头：止损价为最高价 - trailOffset（但只有当盈利达到trailPoints时才激活）
//            // 我们这里直接启用，因为currentProfitPoints >= triggerPoints（假设triggerPoints即为激活阈值）
//            // 但为了符合Pine行为，我们检查是否满足激活条件（盈利 >= trailPoints）
//            // 如果triggerPoints >= trailPoints，则已满足；否则需等待。
//            if (currentProfitPoints >= trailPoints) {
//                double newStop = highestSinceEntry - trailOffset;
//                if (newStop > stopLossPrice) { // 只向上移动止损
//                    stopLossPrice = newStop;
//                    System.out.printf("多头移动止损更新: 最高价 %.2f, 止损 %.2f%n", highestSinceEntry, stopLossPrice);
//                }
//            }
//        } else if (position == -1) {
//            if (currentProfitPoints >= trailPoints) {
//                double newStop = lowestSinceEntry + trailOffset;
//                if (newStop < stopLossPrice) { // 只向下移动止损
//                    stopLossPrice = newStop;
//                    System.out.printf("空头移动止损更新: 最低价 %.2f, 止损 %.2f%n", lowestSinceEntry, stopLossPrice);
//                }
//            }
//        }
//    }
//
//    /**
//     * 检查是否触发止损
//     */
//    private void checkStopLoss(Bar bar) {
//        if (position == 1 && bar.low <= stopLossPrice) {
//            closePosition(stopLossPrice, "多头止损");
//        } else if (position == -1 && bar.high >= stopLossPrice) {
//            closePosition(stopLossPrice, "空头止损");
//        }
//    }
//
//    /**
//     * 平仓
//     */
//    private void closePosition(double price, String reason) {
//        System.out.printf("平仓 %s @ %.2f, 盈亏 %.2f 点%n", reason, price, currentProfitPoints);
//        position = 0;
//        entryPrice = 0.0;
//        stopLossPrice = 0.0;
//        currentProfitPoints = 0.0;
//        // 实际执行平仓订单
//    }
//
//    // ----- 示例回测 -----
//    public static void main(String[] args) {
//        // 模拟4H K线数据（仅示例，实际应加载真实数据）
//        List<Bar> bars = generateSampleBars();
//        BlackHorseStrategyA strategy = new BlackHorseStrategyA(10.0, 10.0, 10.0);
//
//        for (Bar bar : bars) {
//            strategy.onBar(bar);
//        }
//    }
//
//    private static List<Bar> generateSampleBars() {
//        // 生成一些虚拟数据用于演示
//        List<Bar> bars = new ArrayList<>();
//        long baseTime = System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L; // 10天前
//        for (int i = 0; i < 100; i++) {
//            long time = baseTime + i * 60 * 60 * 1000L; // 每小时一根
//            double open = 100 + Math.random() * 10;
//            double high = open + Math.random() * 5;
//            double low = open - Math.random() * 5;
//            double close = low + Math.random() * (high - low);
//            bars.add(new Bar(open, high, low, close, time));
//        }
//        return bars;
//    }
//}
