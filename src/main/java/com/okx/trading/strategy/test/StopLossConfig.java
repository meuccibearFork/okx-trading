//package com.okx.trading.strategy.test;
//
//import com.okx.trading.strategy.DynamicStopLossTracker;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//
///**
// * 配置类（可选，用于统一配置）
// */
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class StopLossConfig {
//    private BigDecimal initialStopLossPercent;
//    private BigDecimal incrementPercent;
//    private BigDecimal entryPrice;
//
//    /**
//     * 创建追踪器
//     */
//    public DynamicStopLossTracker createTracker() {
//        return DynamicStopLossTracker.builder()
//                .entryPrice(entryPrice)
//                .initialStopLossPercent(initialStopLossPercent)
//                .incrementPercent(incrementPercent)
//                .build();
//    }
//}
