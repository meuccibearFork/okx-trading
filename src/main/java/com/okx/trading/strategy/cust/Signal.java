package com.okx.trading.strategy.cust;

import lombok.Data;
import org.ta4j.core.num.Num;

/**
 * 信号类
 */
@Data
@lombok.Builder
public class Signal {
    private boolean longSignal;
    private boolean shortSignal;
    private Num avgPrice;
    private Num avgPricePrev;
}
