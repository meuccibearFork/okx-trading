package com.okx.trading.strategy.cust;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal; /**
 * 策略配置类
 */
@Data
@Builder
public class StrategyConfig {

    // 开多阈值（百分比）
    @Builder.Default
    private BigDecimal longThreshold = new BigDecimal("0.2");

    // 开空阈值（百分比）
    @Builder.Default
    private BigDecimal shortThreshold = new BigDecimal("0.2");

    // 最小K线数要求
    @Builder.Default
    private int minBars = 2;

    // 最小K线间隔（防止频繁交易）
    @Builder.Default
    private int minBarInterval = 1;

    // 调试模式
    @Builder.Default
    private boolean debug = false;

    // 止损百分比（由外部统一规则处理）
    private BigDecimal stopLossPercent;

    // 移动止损百分比（由外部统一规则处理）
    private BigDecimal trailingProfitPercent;

//
//    // 开多阈值（百分比）
//    @Builder.Default
//    private BigDecimal longThreshold = new BigDecimal("0.5");
//
//    // 开空阈值（百分比）
//    @Builder.Default
//    private BigDecimal shortThreshold = new BigDecimal("0.5");
//
//    // 止损百分比
//    @Builder.Default
//    private BigDecimal stopLossPercent = new BigDecimal("2.0");
//
//    // 移动止损触发条件（盈利百分比）
//    @Builder.Default
//    private BigDecimal trailingStopTrigger = new BigDecimal("2.0");
//
//    // 最小利润保护百分比
//    @Builder.Default
//    private BigDecimal minProfitProtect = new BigDecimal("2.0");
//
//    // 是否允许同周期重复交易
//    @Builder.Default
//    private boolean allowSamePeriodTrade = false;
}
