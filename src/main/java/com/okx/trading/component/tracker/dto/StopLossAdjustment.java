package com.okx.trading.component.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 止损点调整记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StopLossAdjustment {
    private String reason;
    private BigDecimal returnAtAdjustment; // 调整时的收益率（百分比）
    private BigDecimal newStopLossPercent; // 新的止损点（百分比）
    private BigDecimal newStopLossPrice;   // 新的止损价格
    private LocalDateTime adjustmentTime;  // 调整时间
}
