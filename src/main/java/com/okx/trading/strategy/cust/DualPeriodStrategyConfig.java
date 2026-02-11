package com.okx.trading.strategy.cust;

import lombok.Builder;
import lombok.Data;

/**
 * 策略配置类（用于外部配置）
 */
@Data
@Builder
public class DualPeriodStrategyConfig {
    // 移动止损相关参数
    @Builder.Default
    private double stopLossPercent = 0.02;      // 2%止损

    @Builder.Default
    private double trailingStartPercent = 0.02; // 2%开始移动止损

    @Builder.Default
    private double trailingStepPercent = 0.01;  // 每1%移动一次

    @Builder.Default
    private double minProfitLockPercent = 0.02; // 确保至少2%利润

    // 信号阈值
    @Builder.Default
    private double longThresholdPercent = 0.0;  // 多头信号阈值

    @Builder.Default
    private double shortThresholdPercent = 0.0; // 空头信号阈值

    // 其他配置
    @Builder.Default
    private boolean enableLong = true;          // 启用多头

    @Builder.Default
    private boolean enableShort = true;         // 启用空头

    @Builder.Default
    private int maxPositions = 1;               // 最大持仓数量
}

