package com.okx.trading.strategy;

import com.alibaba.fastjson2.JSON;
import com.okx.trading.util.Ta4jNumUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

/**
 * 自定义-策略工厂 - 高级策略集合
 */
@Slf4j
public class CustomizeStrategyFactory {


    public static Strategy yjwStrategy(BarSeries series, String htf, double triggerPoints, double trailOffsetIn, int trailPointsIn) {
        // 获取更高时间框架的数据
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
        HighPriceIndicator highPrice = new HighPriceIndicator(series);
        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        AvgIndicator bop = new AvgIndicator(openPrice, highPrice, lowPrice, closePrice, series);

        Num avg4 = bop.calculate(0);
        Num avg4Prev = bop.calculate(1);
        boolean newHTFBar = !closePrice.getValue(0).isEqual(closePrice.getValue(1));

        // 信号生成
        Rule longSignal = new AndRule(
                new CrossedUpIndicatorRule(closePrice, avg4Prev),
                new BooleanRule(newHTFBar)
        );
        Rule shortSignal = new AndRule(
                new CrossedDownIndicatorRule(closePrice, avg4Prev),
                new BooleanRule(newHTFBar)
        );

        // 初始止损
        Num prevHTFHigh = series.getBar(1).getHighPrice();
        Num prevHTFLow = series.getBar(1).getLowPrice();

        // 持仓信息 & 盈利点数计算
        int position_size = 0;
        boolean isLong = position_size > 0;
        boolean isShort = position_size < 0;
        Num entryPrice = Ta4jNumUtil.valueOf(0);

        Num profitPoints = Ta4jNumUtil.valueOf(0);
        if (isLong) {
            profitPoints = series.getLastBar().getClosePrice().minus(entryPrice);
        } else if (isShort) {
            profitPoints = entryPrice.minus(series.getLastBar().getClosePrice());
        }

        // 下单与退出逻辑
//        Rule enterLongRule = new AndRule(longSignal, new IsFlatRule(position));
//        Rule enterShortRule = new AndRule(shortSignal, new IsFlatRule(position));

        Rule exitLongRule = new OrRule(
                new StopLossRule(closePrice, prevHTFLow),
                new TrailingStopLossRule(closePrice, Ta4jNumUtil.valueOf(trailOffsetIn), trailPointsIn)
        );
        Rule exitShortRule = new OrRule(
                new StopLossRule(closePrice, prevHTFHigh),
                new TrailingStopLossRule(closePrice, Ta4jNumUtil.valueOf(trailOffsetIn), trailPointsIn)
        );

        // 创建策略
        return new BaseStrategy("MultiLevelTakeProfitStopLossStrategy",
                exitLongRule,
                exitShortRule);
    }

    public static Strategy yjwStrategy(BarSeries series) {
        // 获取更高时间框架的数据
        AvgIndicator bop = new AvgIndicator(new OpenPriceIndicator(series), new HighPriceIndicator(series), new LowPriceIndicator(series), new ClosePriceIndicator(series), series);

        @Getter
        @Setter
        class EntryRule implements Rule {
            private AvgIndicator bop;

            public EntryRule(AvgIndicator bop) {
                setBop(bop);
            }

            /**
             * @param index         the bar index
             * @param tradingRecord the potentially needed trading history
             * @return true if this rule is satisfied for the provided index, false
             * otherwise
             */
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                // 是否上涨
                int whetherItRises = bop.compareTo(0, 1);
                log.info("EntryRule.isSatisfied:index:{} whetherItRises: {} tradingRecord:{}", index, whetherItRises, JSON.toJSONString(tradingRecord));
                boolean result = false;
                if (whetherItRises == -1) {

                } else if (whetherItRises == 0) {

                } else if (whetherItRises == 1) {
                    result = true;
                }

                return result;
            }
        }

        @Getter
        @Setter
        class ExitRule implements Rule {
            private AvgIndicator bop;

            public ExitRule(AvgIndicator bop) {
                setBop(bop);
            }

            /**
             * @param index         the bar index
             * @param tradingRecord the potentially needed trading history
             * @return true if this rule is satisfied for the provided index, false
             * otherwise
             */
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                // 是否上涨
                int whetherItRises = bop.compareTo(0, 1);
                boolean result = false;
                if (whetherItRises == -1) {

                } else if (whetherItRises == 0) {

                } else if (whetherItRises == 1) {
                    result = true;
                }

                return result;

            }
        }

        ExitRule exitRule = new ExitRule(bop);
        EntryRule entryRule = new EntryRule(bop);
        return new BaseStrategy(entryRule, exitRule);
    }

    /**
     * 创建多层次止盈止损策略
     * 使用不同层次的止盈止损点来管理风险和锁定利润
     * <p>
     * 策略逻辑：
     * 1. 当RSI<30且价格突破20日均线时买入
     * 2. 设置多个止盈点：2%、4%、6%
     * 3. 设置多个止损点：-1%、-2%、-3%
     * 4. 根据价格变化动态调整止盈止损位
     */
    public static Strategy createMultiLevelTakeProfitStopLossStrategy(BarSeries series) {
        int rsiPeriod = 14;
        int smaPeriod = 20;

        if (series.getBarCount() <= Math.max(rsiPeriod, smaPeriod)) {
            throw new IllegalArgumentException("数据点不足以计算指标: 至少需要 " + (Math.max(rsiPeriod, smaPeriod) + 1) + " 个数据点");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiPeriod);
        SMAIndicator sma20 = new SMAIndicator(closePrice, smaPeriod);
        ATRIndicator atr = new ATRIndicator(series, 14);

        // 多层次止盈止损指标
        class MultiLevelTPSLIndicator extends CachedIndicator<Num> {
            @Override
            public int getCountOfUnstableBars() {
                return 0;
            }

            private final ClosePriceIndicator closePrice;
            private final ATRIndicator atr;
            private final double[] takeProfitLevels = {0.02, 0.04, 0.06}; // 2%, 4%, 6%
            private final double[] stopLossLevels = {-0.01, -0.02, -0.03}; // -1%, -2%, -3%
            private Num entryPrice = null;
            private boolean isLong = false;
            private int currentTPLevel = 0;
            private int currentSLLevel = 0;

            public MultiLevelTPSLIndicator(ClosePriceIndicator closePrice, ATRIndicator atr, BarSeries series) {
                super(series);
                this.closePrice = closePrice;
                this.atr = atr;
            }

            @Override
            protected Num calculate(int index) {
                if (index == 0) {
                    return Ta4jNumUtil.valueOf(0);
                }

                Num currentPrice = closePrice.getValue(index);

                // 如果还没有入场价格，返回0
                if (entryPrice == null) {
                    return Ta4jNumUtil.valueOf(0);
                }

                // 计算当前收益率
                Num profitRate = currentPrice.minus(entryPrice).dividedBy(entryPrice);

                // 检查止盈条件
                for (int i = currentTPLevel; i < takeProfitLevels.length; i++) {
                    if (profitRate.doubleValue() >= takeProfitLevels[i]) {
                        currentTPLevel = i + 1;
                        // 动态调整止损位 - 当达到某个止盈点时，将止损位上移
                        if (i > 0) {
                            currentSLLevel = Math.min(currentSLLevel + 1, stopLossLevels.length - 1);
                        }
                        return Ta4jNumUtil.valueOf(1); // 部分止盈信号
                    }
                }

                // 检查止损条件
                if (profitRate.doubleValue() <= stopLossLevels[currentSLLevel]) {
                    return Ta4jNumUtil.valueOf(-1); // 止损信号
                }

                return Ta4jNumUtil.valueOf(0); // 持仓信号
            }

            public void setEntryPrice(Num price) {
                this.entryPrice = price;
                this.isLong = true;
                this.currentTPLevel = 0;
                this.currentSLLevel = 0;
            }

            public void reset() {
                this.entryPrice = null;
                this.isLong = false;
                this.currentTPLevel = 0;
                this.currentSLLevel = 0;
            }
        }

        MultiLevelTPSLIndicator multiLevelTPSL = new MultiLevelTPSLIndicator(closePrice, atr, series);

        // 动态止盈止损规则
        class DynamicTakeProfitRule implements Rule {
            private final MultiLevelTPSLIndicator indicator;

            public DynamicTakeProfitRule(MultiLevelTPSLIndicator indicator) {
                this.indicator = indicator;
            }

            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                if (tradingRecord.getCurrentPosition().isOpened()) {
                    // 设置入场价格
                    if (indicator.entryPrice == null) {
                        Trade entryTrade = tradingRecord.getCurrentPosition().getEntry();
                        indicator.setEntryPrice(entryTrade.getNetPrice());
                        log.info("<entryTrade>:{}", JSON.toJSONString(entryTrade));
                    }

                    Num signal = indicator.getValue(index);
                    return signal.doubleValue() == 1 || signal.doubleValue() == -1;
                }
                return false;
            }
        }

        class DynamicStopLossRule implements Rule {
            private final MultiLevelTPSLIndicator indicator;

            public DynamicStopLossRule(MultiLevelTPSLIndicator indicator) {
                this.indicator = indicator;
            }

            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                if (tradingRecord.getCurrentPosition().isOpened()) {
                    Num signal = indicator.getValue(index);
                    return signal.doubleValue() == -1;
                }
                return false;
            }
        }

        // 入场规则：RSI超卖且价格突破20日均线
        Rule entryRule = new UnderIndicatorRule(rsi, Ta4jNumUtil.valueOf(30))
                .and(new CrossedUpIndicatorRule(closePrice, sma20));

        // 出场规则：多层次止盈止损或RSI超买
        Rule exitRule = new OrRule(
                new OrRule(
                        new DynamicTakeProfitRule(multiLevelTPSL),
                        new DynamicStopLossRule(multiLevelTPSL)
                ),
                new OverIndicatorRule(rsi, Ta4jNumUtil.valueOf(70))
        );

        // 在出场时重置指标
        class ResetOnExitRule implements Rule {
            private final Rule originalRule;
            private final MultiLevelTPSLIndicator indicator;

            public ResetOnExitRule(Rule originalRule, MultiLevelTPSLIndicator indicator) {
                this.originalRule = originalRule;
                this.indicator = indicator;
            }

            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                boolean shouldExit = originalRule.isSatisfied(index, tradingRecord);
                if (shouldExit) {
                    indicator.reset();
                }
                return shouldExit;
            }
        }

        Rule finalExitRule = new ResetOnExitRule(exitRule, multiLevelTPSL);

        return new BaseStrategy("多层次止盈止损策略", entryRule, finalExitRule);
    }

}
