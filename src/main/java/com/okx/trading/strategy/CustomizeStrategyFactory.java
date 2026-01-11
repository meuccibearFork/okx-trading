package com.okx.trading.strategy;

import com.alibaba.fastjson2.JSON;
import com.okx.trading.util.Ta4jNumUtil;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;

/**
 * 自定义-策略工厂 - 高级策略集合
 */
@Slf4j
public class CustomizeStrategyFactory {


    /**
     * // This Pine Script® code is subject to the terms of the Mozilla Public License 2.0 at https://mozilla.org/MPL/2.0/
     * // © yjiawen
     * <p>
     * //@version=6
     * strategy("黑马策略A",
     * overlay=true,
     * default_qty_type=strategy.percent_of_equity,
     * default_qty_value=10,   // 模拟 10x 杠杆：每单占用 10% 资金（示例）
     * pyramiding=0,
     * calc_on_order_fills=true,
     * calc_on_every_tick=true)
     * <p>
     * // ===== 输入 =====
     * htf              = input.timeframe("240", "Higher timeframe (4H)")
     * trigger_points   = input.float(10.0, "触发启用移动止盈的盈利阈值 (点)", step=1)
     * trail_offset_in  = input.float(10.0, "移动止盈 - trail_offset (点)", step=1)
     * trail_points_in  = input.float(10.0, "移动止盈 - trail_points (点)", step=1)
     * <p>
     * // ===== 获取 4H OHLC 与 平均价 =====
     * [o4, h4, l4, c4, t4] = request.security(syminfo.tickerid, htf, [open, high, low, close, time])
     * avg4      = (o4 + h4 + l4 + c4) / 4.0 每4小时
     * avg4_prev = avg4[1]    上一次
     * new_htf_bar = ta.change(t4) != 0    上一次和当前的收盘价比较是否一样
     * <p>
     * // ===== 信号 =====
     * long_signal  = new_htf_bar and (avg4 > avg4_prev)
     * short_signal = new_htf_bar and (avg4 < avg4_prev)
     * <p>
     * // ===== 上一根 HTF 高低，用作初始止损 =====
     * prev_htf_high = h4[1]
     * prev_htf_low  = l4[1]
     * <p>
     * // ===== 持仓信息 & 盈利点数计算 =====
     * is_long  = strategy.position_size > 0
     * is_short = strategy.position_size < 0
     * entry_price = strategy.position_avg_price  当前市场定位平均入场价。 如果市场地位平滑，则“NaN”就会退回
     * <p>
     * profit_points = 0.0
     * if is_long
     * profit_points := (close - entry_price)
     * else if is_short
     * profit_points := (entry_price - close)
     * else
     * profit_points := 0.0
     * <p>
     * // ===== 下单与退出逻辑 =====
     * // 我们为每个方向使用独立 entry_id 和 exit_id
     * long_id_heima = "LongEntry"
     * short_id_heima = "ShortEntry"
     * long_exit_id_heima = "LongExit"
     * short_exit_id_heima = "ShortExit"
     * <p>
     * <p>
     * if(strategy.position_size == 0){
     * if (long_signal){
     * // 新开多：带初始 stop 为上一根 4H 的 low（保证有退出参数）
     * strategy.entry(long_id_heima, strategy.long)
     * strategy.exit(long_exit_id_heima, from_entry=long_id_heima, stop=prev_htf_low)
     * }else if (short_signal){
     * // 新开空：带初始 stop 为上一根 4H 的 high
     * strategy.entry(short_id_heima, strategy.short)
     * strategy.exit(short_exit_id_heima, from_entry=short_id_heima, stop=prev_htf_high)
     * }
     * }
     * <p>
     * // 当持仓并且浮动盈利超过 trigger_points 时，改用拖动止盈（必须同时提供 trail_offset 与 trail_points）
     * if is_long and profit_points >= trigger_points
     * // 注意：这里我们重新调用 strategy.exit，使用与拖动相关的参数对 (trail_offset & trail_points)
     * strategy.exit(long_exit_id_heima, from_entry=long_id_heima, trail_offset=trail_offset_in, trail_points=trail_points_in)
     * <p>
     * if is_short and profit_points >= trigger_points
     * strategy.exit(short_exit_id_heima, from_entry=short_id_heima, trail_offset=trail_offset_in, trail_points=trail_points_in)
     * <p>
     * // ===== 可视化提示 =====
     * plotshape(long_signal,  title="开多 (4H)",  location=location.belowbar, color=color.green,  style=shape.triangleup,   size=size.small, text="多")
     * plotshape(short_signal, title="开空 (4H)",  location=location.abovebar, color=color.red,    style=shape.triangledown, size=size.small, text="空")
     */

//    public static Strategy yjwStrategy(BarSeries series, String htf, double triggerPoints, double trailOffsetIn, int trailPointsIn) {
//        // 获取更高时间框架的数据
//        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
//        HighPriceIndicator highPrice = new HighPriceIndicator(series);
//        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//
//        BalanceOfPowerIndicator bop = new BalanceOfPowerIndicator(openPrice, highPrice, lowPrice, closePrice, series);
//
//        Num avg4 = bop.calculate(0);
//        Num avg4Prev = bop.calculate(1);
//        boolean newHTFBar = !closePrice.getValue(0).isEqual(closePrice.getValue(1));
//
//        // 信号生成
//        Rule longSignal = new AndRule(
//            new CrossedUpIndicatorRule(closePrice, avg4Prev),
//            new BooleanRule(newHTFBar)
//        );
//        Rule shortSignal = new AndRule(
//            new CrossedDownIndicatorRule(closePrice, avg4Prev),
//            new BooleanRule(newHTFBar)
//        );
//
//        // 初始止损
//        Num prevHTFHigh = series.getBar(1).getHighPrice();
//        Num prevHTFLow = series.getBar(1).getLowPrice();
//
//        // 持仓信息 & 盈利点数计算
//        int position_size = 0;
//        boolean isLong = position_size > 0;
//        boolean isShort = position_size < 0;
//        Num entryPrice =Ta4jNumUtil.valueOf(0);
//
//        Num profitPoints = Ta4jNumUtil.valueOf(0);
//        if (isLong) {
//            profitPoints = series.getLastBar().getClosePrice().minus(entryPrice);
//        } else if (isShort) {
//            profitPoints = entryPrice.minus(series.getLastBar().getClosePrice());
//        }
//
//        // 下单与退出逻辑
//        Rule enterLongRule = new AndRule(longSignal, new IsFlatRule(position));
//        Rule enterShortRule = new AndRule(shortSignal, new IsFlatRule(position));
//
//        Rule exitLongRule = new OrRule(
//                new StopLossRule(closePrice, prevHTFLow),
//                new TrailingStopLossRule(closePrice, Ta4jNumUtil.valueOf(trailOffsetIn), trailPointsIn)
//        );
//        Rule exitShortRule = new OrRule(
//                new StopLossRule(closePrice, prevHTFHigh),
//                new TrailingStopLossRule(closePrice, Ta4jNumUtil.valueOf(trailOffsetIn), trailPointsIn)
//        );
//
//        // 创建策略
//        return new BaseStrategy("MultiLevelTakeProfitStopLossStrategy",
//                exitLongRule,
//                exitShortRule);
//    }

//    public static Strategy createAdvancedMultiLevelStrategy(BarSeries series) {
//        // 获取更高时间框架的数据
//        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
//        HighPriceIndicator highPrice = new HighPriceIndicator(series);
//        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//
//        AvgIndicator bop = new AvgIndicator(openPrice, highPrice, lowPrice, closePrice, series);
//
//        Num avg4 = bop.calculate(0);
//        Num avg4Prev = bop.calculate(1);
//
//        int compareToInt= avg4.compareTo(avg4Prev);
//
//        //空仓
//            //涨了(开仓
//
//        //持仓
//            //涨了（设置止损点 10%
//            //不涨（达到止损点平仓
//
//
//        int rsiPeriod = 14;
//        int smaPeriod = 20;
//
//        if (series.getBarCount() <= Math.max(rsiPeriod, smaPeriod)) {
//            throw new IllegalArgumentException("数据点不足以计算指标: 至少需要 " + (Math.max(rsiPeriod, smaPeriod) + 1) + " 个数据点");
//        }
//
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        RSIIndicator rsi = new RSIIndicator(closePrice, rsiPeriod);
//        SMAIndicator sma20 = new SMAIndicator(closePrice, smaPeriod);
//        ATRIndicator atr = new ATRIndicator(series, 14);
//
//        // 多层次止盈止损指标
//        class MultiLevelTPSLIndicator extends CachedIndicator<Num> {
//            @Override
//            public int getCountOfUnstableBars() {
//                return 0;
//            }
//
//            private final ClosePriceIndicator closePrice;
//            private final ATRIndicator atr;
//            private final double[] takeProfitLevels = {0.02, 0.04, 0.06}; // 2%, 4%, 6%
//            private final double[] stopLossLevels = {-0.01, -0.02, -0.03}; // -1%, -2%, -3%
//            private Num entryPrice = null;
//            private boolean isLong = false;
//            private int currentTPLevel = 0;
//            private int currentSLLevel = 0;
//
//            public MultiLevelTPSLIndicator(ClosePriceIndicator closePrice, ATRIndicator atr, BarSeries series) {
//                super(series);
//                this.closePrice = closePrice;
//                this.atr = atr;
//            }
//
//            @Override
//            protected Num calculate(int index) {
//                if (index == 0) {
//                    return Ta4jNumUtil.valueOf(0);
//                }
//
//                Num currentPrice = closePrice.getValue(index);
//
//                // 如果还没有入场价格，返回0
//                if (entryPrice == null) {
//                    return Ta4jNumUtil.valueOf(0);
//                }
//
//                // 计算当前收益率
//                Num profitRate = currentPrice.minus(entryPrice).dividedBy(entryPrice);
//
//                // 检查止盈条件
//                for (int i = currentTPLevel; i < takeProfitLevels.length; i++) {
//                    if (profitRate.doubleValue() >= takeProfitLevels[i]) {
//                        currentTPLevel = i + 1;
//                        // 动态调整止损位 - 当达到某个止盈点时，将止损位上移
//                        if (i > 0) {
//                            currentSLLevel = Math.min(currentSLLevel + 1, stopLossLevels.length - 1);
//                        }
//                        return Ta4jNumUtil.valueOf(1); // 部分止盈信号
//                    }
//                }
//
//                // 检查止损条件
//                if (profitRate.doubleValue() <= stopLossLevels[currentSLLevel]) {
//                    return Ta4jNumUtil.valueOf(-1); // 止损信号
//                }
//
//                return Ta4jNumUtil.valueOf(0); // 持仓信号
//            }
//
//            public void setEntryPrice(Num price) {
//                this.entryPrice = price;
//                this.isLong = true;
//                this.currentTPLevel = 0;
//                this.currentSLLevel = 0;
//            }
//
//            public void reset() {
//                this.entryPrice = null;
//                this.isLong = false;
//                this.currentTPLevel = 0;
//                this.currentSLLevel = 0;
//            }
//        }
//
//        MultiLevelTPSLIndicator multiLevelTPSL = new MultiLevelTPSLIndicator(closePrice, atr, series);
//        System.out.println(multiLevelTPSL.entryPrice);
//
//        // 动态止盈止损规则
//        class DynamicTakeProfitRule implements Rule {
//            private final MultiLevelTPSLIndicator indicator;
//
//            public DynamicTakeProfitRule(MultiLevelTPSLIndicator indicator) {
//                this.indicator = indicator;
//            }
//
//            @Override
//            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
//                if (tradingRecord.getCurrentPosition().isOpened()) {
//                    // 设置入场价格
//                    if (indicator.entryPrice == null) {
//                        Trade entryTrade = tradingRecord.getCurrentPosition().getEntry();
//                        indicator.setEntryPrice(entryTrade.getNetPrice());
//                    }
//
//                    Num signal = indicator.getValue(index);
//                    return signal.doubleValue() == 1 || signal.doubleValue() == -1;
//                }
//                return false;
//            }
//        }
//
//        class DynamicStopLossRule implements Rule {
//            private final MultiLevelTPSLIndicator indicator;
//
//            public DynamicStopLossRule(MultiLevelTPSLIndicator indicator) {
//                this.indicator = indicator;
//            }
//
//            @Override
//            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
//                if (tradingRecord.getCurrentPosition().isOpened()) {
//                    Num signal = indicator.getValue(index);
//                    return signal.doubleValue() == -1;
//                }
//                return false;
//            }
//        }
//
//        // 入场规则：RSI超卖且价格突破20日均线
//        Rule entryRule = new UnderIndicatorRule(rsi, Ta4jNumUtil.valueOf(30))
//                .and(new CrossedUpIndicatorRule(closePrice, sma20));
//
//        // 出场规则：多层次止盈止损或RSI超买
//        Rule exitRule = new OrRule(
//                new OrRule(
//                        new DynamicTakeProfitRule(multiLevelTPSL),
//                        new DynamicStopLossRule(multiLevelTPSL)
//                ),
//                new OverIndicatorRule(rsi, Ta4jNumUtil.valueOf(70))
//        );
//
//        // 在出场时重置指标
//        class ResetOnExitRule implements Rule {
//            private final Rule originalRule;
//            private final MultiLevelTPSLIndicator indicator;
//
//            public ResetOnExitRule(Rule originalRule, MultiLevelTPSLIndicator indicator) {
//                this.originalRule = originalRule;
//                this.indicator = indicator;
//            }
//
//            @Override
//            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
//                boolean shouldExit = originalRule.isSatisfied(index, tradingRecord);
//                if (shouldExit) {
//                    indicator.reset();
//                }
//                return shouldExit;
//            }
//        }
//
//
//        Rule finalExitRule = new ResetOnExitRule(exitRule, multiLevelTPSL);
//
//        return new BaseStrategy("多层次止盈止损策略", entryRule, finalExitRule);
//    }

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
//    public static Strategy createMultiLevelTakeProfitStopLossStrategy(BarSeries series) {


    /**
     * 创建高级多层次止盈止损策略
     * 结合技术指标和风险管理的综合策略
     */
//    public static Strategy createAdvancedMultiLevelStrategy(BarSeries series) {




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

// 力量平衡指标
class AvgIndicator extends CachedIndicator<Num> {

    @Override
    public int getCountOfUnstableBars() {
        return 0;
    }

    private final OpenPriceIndicator open;
    private final HighPriceIndicator high;
    private final LowPriceIndicator low;
    private final ClosePriceIndicator close;

    public AvgIndicator(OpenPriceIndicator open, HighPriceIndicator high,
                                   LowPriceIndicator low, ClosePriceIndicator close, BarSeries series) {
        super(series);
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    @Override
    protected Num calculate(int index) {
        Num h = high.getValue(index);
        Num l = low.getValue(index);
        Num c = close.getValue(index);
        Num o = open.getValue(index);

        Num range = o.plus(c).plus(h).plus(l);

        if (range.isZero()) {
            return Ta4jNumUtil.valueOf(0);
        }

        // BOP = (Open + Close + High + Low) /4.0
        return range.dividedBy(Ta4jNumUtil.valueOf(4));
    }




}

