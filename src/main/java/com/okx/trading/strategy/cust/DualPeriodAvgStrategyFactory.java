package com.okx.trading.strategy.cust;

import lombok.Getter;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * 双周期平均价格比较策略工厂
 * 返回BaseStrategy对象，与现有系统兼容
 */
public class DualPeriodAvgStrategyFactory {

    /**
     * 创建双周期平均价格比较策略（兼容现有系统）
     *
     * @param series K线数据系列
     * @param config 策略配置
     * @return BaseStrategy对象，可与addExtraStopRule配合使用
     */
    public static Strategy createDualPeriodAvgStrategy(BarSeries series, StrategyConfig config) {
        if (series.getBarCount() < 2) {
            throw new IllegalArgumentException("数据点不足以计算指标: 至少需要 2 个数据点");
        }

        // 1. 创建基本指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 2. 创建四价平均指标（开盘+最高+最低+收盘）/4
        FourPriceAverageIndicator fourPriceAvg = new FourPriceAverageIndicator(series);

        // 3. 创建上周期四价平均指标
        PreviousValueIndicator prevFourPriceAvg = new PreviousValueIndicator(fourPriceAvg, 1);

        // 4. 创建价格变化百分比指标
        PriceChangePercentIndicator changePercent = new PriceChangePercentIndicator(
                fourPriceAvg, prevFourPriceAvg, series);

        // 5. 创建入场信号指标
        DualPeriodSignalIndicator signalIndicator = new DualPeriodSignalIndicator(changePercent, series, config);

        // 6. 入场规则：信号指标发出买入或卖出信号
        Rule entryRule = new DualPeriodEntryRule(signalIndicator, closePrice, series, config);

        // 7. 出场规则：移动止盈和止损规则（你提供的）
        StopLossRule stopLossRule = new StopLossRule(
                closePrice,
                DecimalNum.valueOf(config.getStopLossPercent().doubleValue()));

        TrailingStopLossRule trailingStopLossRule = new TrailingStopLossRule(
                closePrice,
                DecimalNum.valueOf(config.getTrailingProfitPercent().doubleValue()));

        // 8. 创建基础策略
        return new BaseStrategy("双周期平均价格比较策略", entryRule, new OrRule(stopLossRule, trailingStopLossRule));
    }

    /**
     * 四价平均指标（开盘+最高+最低+收盘）/4
     */
    private static class FourPriceAverageIndicator extends CachedIndicator<Num> {
        private final BarSeries series;

        public FourPriceAverageIndicator(BarSeries series) {
            super(series);
            this.series = series;
        }

        @Override
        public int getCountOfUnstableBars() {
            return 0;
        }

        @Override
        protected Num calculate(int index) {
            Bar bar = series.getBar(index);
            Num open = bar.getOpenPrice();
            Num high = bar.getHighPrice();
            Num low = bar.getLowPrice();
            Num close = bar.getClosePrice();

            return open.plus(high).plus(low).plus(close).dividedBy(numOf(4));
        }
    }

    /**
     * 价格变化百分比指标
     */
    private static class PriceChangePercentIndicator extends CachedIndicator<Num> {
        private final FourPriceAverageIndicator current;
        private final PreviousValueIndicator previous;
        //private final BarSeries series;

        public PriceChangePercentIndicator(FourPriceAverageIndicator current,
                                           PreviousValueIndicator previous,
                                           BarSeries series) {
            super(series);
            this.current = current;
            this.previous = previous;
            //this.series = series;
        }

        @Override
        public int getCountOfUnstableBars() {
            return 1; // 需要前一周期数据
        }

        @Override
        protected Num calculate(int index) {
            if (index < 1) {
                return numOf(0);
            }

            Num currentValue = current.getValue(index);
            Num previousValue = previous.getValue(index);

            // 避免除零
            if (previousValue.isZero()) {
                return numOf(0);
            }

            // 计算百分比变化
            return currentValue.minus(previousValue)
                    .dividedBy(previousValue)
                    .multipliedBy(numOf(100));
        }
    }

    /**
     * 双周期信号指标
     * 根据价格变化百分比判断多空信号
     */
    private static class DualPeriodSignalIndicator extends CachedIndicator<Integer> {
        private final PriceChangePercentIndicator changePercent;
        @Getter
        private final BarSeries series;
        private final StrategyConfig config;
        private final Map<Integer, Integer> signalCache = new HashMap<>();

        public DualPeriodSignalIndicator(PriceChangePercentIndicator changePercent,
                                         BarSeries series, StrategyConfig config) {
            super(series);
            this.changePercent = changePercent;
            this.series = series;
            this.config = config;
        }

        @Override
        public int getCountOfUnstableBars() {
            return 1;
        }

        @Override
        protected Integer calculate(int index) {
            if (signalCache.containsKey(index)) {
                return signalCache.get(index);
            }

            if (index < 1) {
                return 0; // 无信号
            }

            Num change = changePercent.getValue(index);
            int signal = 0; // 0:无信号, 1:买入信号, -1:卖出信号

            // 判断多空信号
            BigDecimal longThreshold = config.getLongThreshold();
            BigDecimal shortThreshold = config.getShortThreshold();

            if (change.doubleValue() >= longThreshold.doubleValue()) {
                signal = 1; // 买入信号
            } else if (change.doubleValue() <= -shortThreshold.doubleValue()) {
                signal = -1; // 卖出信号
            }

            signalCache.put(index, signal);
            return signal;
        }

        /**
         * 获取信号类型
         */
        public String getSignalType(int index) {
            Integer signal = getValue(index);
            if (signal == 1) {
                return "BUY";
            } else if (signal == -1) {
                return "SELL";
            }
            return "NONE";
        }

    }

    /**
     * 双周期入场规则
     */
    private static class DualPeriodEntryRule extends AbstractRule {
        private final DualPeriodSignalIndicator signalIndicator;
        private final ClosePriceIndicator closePrice;
        private final BarSeries series;
        private final StrategyConfig config;
        private final Map<String, Integer> lastSignalIndex = new HashMap<>();

        public DualPeriodEntryRule(DualPeriodSignalIndicator signalIndicator,
                                   ClosePriceIndicator closePrice,
                                   BarSeries series,
                                   StrategyConfig config) {
            this.signalIndicator = signalIndicator;
            this.closePrice = closePrice;
            this.series = series;
            this.config = config;
        }

        @Override
        public boolean isSatisfied(int index, TradingRecord tradingRecord) {
            // 检查是否已经有持仓
            if (tradingRecord.getCurrentPosition().isOpened()) {
                return false;
            }

            // 获取信号
            int signal = signalIndicator.getValue(index);

            if (signal == 0) {
                return false; // 无信号
            }

            // 检查最小K线数
            if (series.getBarCount() < config.getMinBars()) {
                return false;
            }

            // 获取交易品种标识
            String symbol = series.getName();

            // 检查同周期内是否已经交易过（防止重复交易）
            if (lastSignalIndex.containsKey(symbol)) {
                int lastIndex = lastSignalIndex.get(symbol);
                long barsBetween = index - lastIndex;

                // 如果间隔的K线数小于最小间隔，不交易
                if (barsBetween < config.getMinBarInterval()) {
                    return false;
                }
            }

            // 记录本次信号索引
            lastSignalIndex.put(symbol, index);

            // 记录信号信息（可用于日志）
            String signalType = signalIndicator.getSignalType(index);
            Num currentPrice = closePrice.getValue(index);

            // 记录信号日志（实际使用时可以改为logger）
            if (config.isDebug()) {
                System.out.printf("信号触发: %s | 价格: %s | 信号: %s%n",
                        symbol, currentPrice, signalType);
            }

            return true;
        }
    }

    /**
     * 创建Num的便捷方法
     */
    private static Num numOf(Number number) {
        return DecimalNum.valueOf(number);
    }
}
