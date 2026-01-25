package com.okx.trading.strategy.cust;

import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

/**
 * 固定百分比仓位管理器 - ta4j 0.18 兼容
 */
public class FixedPercentPositionSizeManager implements PositionSizeManager {

    private final double positionPercent;

    public FixedPercentPositionSizeManager(double positionPercent) {
        this.positionPercent = positionPercent;
    }

    @Override
    public Num calculatePositionSize(int index, Num price, Num cash, BarSeries series, TradingRecord tradingRecord) {
        // 计算可用于交易的金额
        Num availableCash = cash.multipliedBy(DecimalNum.valueOf(positionPercent / 100.0));

        // 计算可购买的数量
        return availableCash.dividedBy(price);
    }

    @Override
    public Num calculatePositionSize(int index, Num price, Num cash, BarSeries series) {
        return calculatePositionSize(index, price, cash, series, null);
    }
}
