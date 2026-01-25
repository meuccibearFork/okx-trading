package com.okx.trading.strategy.cust.rule;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.rules.AbstractRule;

/**
 * 检测新K线形成的规则 - 替代ta4j 0.18中已移除的TimestampIndicator + NotEqualRule组合
 */
public class NewBarRule extends AbstractRule {
    private final BarSeries series;

    public NewBarRule(BarSeries series) {
        this.series = series;
    }

    @Override
    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        if (index < 1 || series.getBarCount() <= 1) {
            return false;
        }

        // 获取当前和前一根K线的结束时间
        Bar currentBar = series.getBar(index);
        Bar previousBar = series.getBar(index - 1);

        // 如果结束时间不同，说明是新K线
        return !currentBar.getEndTime().equals(previousBar.getEndTime());
    }
}
