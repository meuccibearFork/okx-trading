package com.okx.trading.strategy.cust;

import com.okx.trading.constant.TradingSignal;
import lombok.Data;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

@Data
public class Position {
    private TradingSignal type;
    private Num entryPrice;
    private Num stopLoss;
    private Num highestPrice;
    private Num lowestPrice;
    private boolean trailingActivated = false;
    private ZonedDateTime entryTime;

    public Position(TradingSignal type, Num entryPrice, Num stopLoss) {
        this.type = type;
        this.entryPrice = entryPrice;
        this.stopLoss = stopLoss;
        this.entryTime = ZonedDateTime.now();

        if (type == TradingSignal.LONG) {
            this.highestPrice = entryPrice;
        } else {
            this.lowestPrice = entryPrice;
        }
    }

    public Num calculateProfit(Num currentPrice) {
        if (type == TradingSignal.LONG) {
            return currentPrice.minus(entryPrice);
        } else if (type == TradingSignal.SHORT) {
            return entryPrice.minus(currentPrice);
        }
        return DecimalNum.valueOf(0);
    }
}
