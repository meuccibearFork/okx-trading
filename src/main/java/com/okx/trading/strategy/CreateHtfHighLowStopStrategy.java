package com.okx.trading.strategy;

import com.okx.trading.util.Ta4jNumUtil;
import org.ta4j.core.*;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

public class CreateHtfHighLowStopStrategy {
    public static Strategy createHtfHighLowStopStrategy(BarSeries series) {
        int htfPeriod = 1;
        int pricePeriod = 20;

        if (series.getBarCount() <= pricePeriod) {
            throw new IllegalArgumentException("数据点不足以计算指标");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        HighPriceIndicator highPrice = new HighPriceIndicator(series);
        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);

        SMAIndicator priceSma = new SMAIndicator(closePrice, pricePeriod);

        Indicator<Num> hourlyAvgPrice = new TransformIndicator(closePrice, value -> {
            int index = series.getEndIndex();
            Num open = openPrice.getValue(index);
            Num high = highPrice.getValue(index);
            Num low = lowPrice.getValue(index);
            Num close = closePrice.getValue(index);
            return open.plus(high).plus(low).plus(close).dividedBy(Ta4jNumUtil.valueOf(4));
        });

        HighestValueIndicator htfHigh = new HighestValueIndicator(highPrice, htfPeriod);
        LowestValueIndicator htfLow = new LowestValueIndicator(lowPrice, htfPeriod);

        Rule entryRule = new OverIndicatorRule(hourlyAvgPrice, priceSma);
        Rule exitRule = new UnderIndicatorRule(closePrice, htfLow);

        return new BaseStrategy(entryRule, exitRule);
    }
}
