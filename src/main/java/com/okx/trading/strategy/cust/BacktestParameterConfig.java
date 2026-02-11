package com.okx.trading.strategy.cust;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 配置类（示例）
 */
@Data
public class BacktestParameterConfig {
    private BigDecimal longThreshold = new BigDecimal("0.2");
    private BigDecimal shortThreshold = new BigDecimal("0.2");
    private BigDecimal stopLossPercent = new BigDecimal("2.0");
    private BigDecimal trailingProfitPercent = new BigDecimal("2.0");

    // 获取配置实例（单例或从配置文件中加载）
    public static BacktestParameterConfig getBacktestParameterConfig() {
        // 这里实现获取配置的逻辑
        return new BacktestParameterConfig();
    }
}
