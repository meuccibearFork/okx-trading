package com.okx.trading.strategy.cust;

import com.okx.trading.strategy.cust.rule.NewBarRule;
import org.ta4j.core.*;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * 黑马策略构建器 - 适配 ta4j 0.18
 */
@Component
@RequiredArgsConstructor
public class HeimaStrategyBuilder {

    private final StrategyConfig config;

    /**
     * 构建完整的黑马策略
     *
     * @param baseSeries   基础时间序列
     * @param higherSeries 高时间框架序列
     * @return 交易策略
     */
    public Strategy buildHeimaStrategy(BarSeries baseSeries, BarSeries higherSeries) {
        // 1. 入场规则
        Rule entryRule = buildEntryRule(higherSeries);

        // 2. 退出规则
        Rule exitRule = buildExitRule(baseSeries, higherSeries);

        // 3. 仓位管理器
        PositionSizeManager positionSizeManager = createPositionSizeManager();

        // 4. 使用新的Builder模式构建策略
//        return new BaseStrategy()
//                .name("HeimaStrategyA")
//                .entryRule(entryRule)
//                .exitRule(exitRule)
//                .positionSizeManager(positionSizeManager)
//                .build();

        return new BaseStrategy("HeimaStrategyA", entryRule, exitRule);
    }

    /**
     * 创建固定百分比仓位管理器
     */
    private PositionSizeManager createPositionSizeManager() {
        return new PositionSizeManager() {
            @Override
            public Num calculatePositionSize(int index, Num price, Num cash, BarSeries series, TradingRecord tradingRecord) {
                // 计算可用资金（总资金的百分比）
                Num availableCash = cash.multipliedBy(
                        DecimalNum.valueOf(config.getPositionPercent() / 100.0)
                );

                // 计算可购买数量
                Num quantity = availableCash.dividedBy(price);

                // 确保最小交易量（这里需要根据交易所规则调整）
                Num minQuantity = DecimalNum.valueOf(0.001); // 示例：最小0.001 BTC
                return quantity.isGreaterThanOrEqual(minQuantity) ? quantity : minQuantity;
            }

            @Override
            public Num calculatePositionSize(int index, Num price, Num cash, BarSeries series) {
                return calculatePositionSize(index, price, cash, series, null);
            }
        };
    }

    /**
     * 构建入场规则
     */
    private Rule buildEntryRule(BarSeries higherSeries) {
        // 1. 计算4小时平均价
        ClosePriceIndicator closePrice = new ClosePriceIndicator(higherSeries);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(higherSeries);
        HighPriceIndicator highPrice = new HighPriceIndicator(higherSeries);
        LowPriceIndicator lowPrice = new LowPriceIndicator(higherSeries);

        // 平均价 = (O+H+L+C)/4
        Indicator<Num> avgPrice = new CachedIndicator<>(higherSeries) {
            /**
             * Returns the number of bars up to which {@code this} Indicator calculates
             * wrong values.
             *
             * @return unstable bars
             */
            @Override
            public int getCountOfUnstableBars() {
                return 0;
            }

            @Override
            protected Num calculate(int index) {
                Num open = higherSeries.getBar(index).getOpenPrice();
                Num high = higherSeries.getBar(index).getHighPrice();
                Num low = higherSeries.getBar(index).getLowPrice();
                Num close = higherSeries.getBar(index).getClosePrice();

                return open.plus(high).plus(low).plus(close).dividedBy(DecimalNum.valueOf(4));
            }
        };

        // 上一根K线平均价
        PreviousValueIndicator prevAvgPrice = new PreviousValueIndicator(avgPrice, 1);

        // 2. 检测新K线
        Rule newBarRule = new NewBarRule(higherSeries);

        // 3. 多空信号
        Rule longSignal = new AndRule(
                newBarRule,
                new OverIndicatorRule(avgPrice, prevAvgPrice)
        );

        Rule shortSignal = new AndRule(
                newBarRule,
                new UnderIndicatorRule(avgPrice, prevAvgPrice)
        );

        // 4. 入场规则（多或空）
        return new OrRule(longSignal, shortSignal);
    }

    /**
     * 构建退出规则（初始止损 + 移动止盈）
     */
    private Rule buildExitRule(BarSeries baseSeries, BarSeries higherSeries) {
        return new AbstractRule() {
            @Override
            public boolean isSatisfied(int index, TradingRecord tradingRecord) {
                if (!tradingRecord.getCurrentPosition().isOpened()) {
                    return false;
                }

                Position position = tradingRecord.getCurrentPosition();
                Num entryPrice = position.getEntry().getNetPrice();
                Num currentPrice = baseSeries.getBar(index).getClosePrice();

                // 计算盈利点数
                Num profitPoints = calculateProfitPoints(position, entryPrice, currentPrice);

                // 检查移动止盈
                if (isTrailingStopTriggered(profitPoints)) {
                    return checkTrailingStop(position, currentPrice);
                }

                // 检查初始止损
                return checkInitialStopLoss(position, currentPrice, higherSeries, index);
            }

            private Num calculateProfitPoints(Position position, Num entryPrice, Num currentPrice) {
                return position.getEntry().isBuy()
                        ? currentPrice.minus(entryPrice)
                        : entryPrice.minus(currentPrice);
            }

            private boolean isTrailingStopTriggered(Num profitPoints) {
                return profitPoints.isGreaterThanOrEqual(
                        DecimalNum.valueOf(config.getTriggerPoints())
                );
            }

            private boolean checkTrailingStop(Position position, Num currentPrice) {
                Num trailOffset = DecimalNum.valueOf(config.getTrailOffset());

                if (position.getEntry().isBuy()) {
                    // 多单：当前价 - 偏移量
                    Num trailingStop = currentPrice.minus(trailOffset);
                    return currentPrice.isLessThanOrEqual(trailingStop);
                } else {
                    // 空单：当前价 + 偏移量
                    Num trailingStop = currentPrice.plus(trailOffset);
                    return currentPrice.isGreaterThanOrEqual(trailingStop);
                }
            }

            private boolean checkInitialStopLoss(Position position, Num currentPrice,
                                                 BarSeries higherSeries, int baseIndex) {
                // 映射基础时间框架索引到高时间框架
                int htIndex = mapToHigherTimeframeIndex(baseIndex, baseSeries, higherSeries);
                if (htIndex <= 0) {
                    return false;
                }

                Bar prevHigherBar = higherSeries.getBar(htIndex - 1);

                if (position.getEntry().isBuy()) {
                    // 多单：跌破上一根4小时低点
                    return currentPrice.isLessThanOrEqual(prevHigherBar.getLowPrice());
                } else {
                    // 空单：突破上一根4小时高点
                    return currentPrice.isGreaterThanOrEqual(prevHigherBar.getHighPrice());
                }
            }

            private int mapToHigherTimeframeIndex(int baseIndex, BarSeries baseSeries, BarSeries higherSeries) {
                // 简化实现：假设每个高时间框架Bar包含固定数量的基础Bar
                int barsPerHigherBar = 48; // 4小时包含48个5分钟Bar
                return Math.min(baseIndex / barsPerHigherBar, higherSeries.getBarCount() - 1);
            }
        };
    }
}
