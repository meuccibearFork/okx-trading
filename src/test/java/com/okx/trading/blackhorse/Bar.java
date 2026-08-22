package com.okx.trading.blackhorse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bar {
    private double open;
    private double high;
    private double low;
    private double close;
    private long time;  // 毫秒时间戳
}
