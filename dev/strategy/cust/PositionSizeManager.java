package com.okx.trading.strategy.cust;

import org.ta4j.core.BarSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/1/24 00:47
 */
public interface PositionSizeManager {
    Num calculatePositionSize(int index, Num price, Num cash, BarSeries series, TradingRecord tradingRecord);

    Num calculatePositionSize(int index, Num price, Num cash, BarSeries series);
}
