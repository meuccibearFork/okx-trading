package com.okx.trading.component.tracker;

import com.okx.trading.component.tracker.DynamicStopLossTracker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 交易统计信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeStatistics {
    private LocalDateTime startTime;
    private LocalDateTime lastUpdateTime;
    private BigDecimal entryPrice;
    private BigDecimal currentPrice;
    private BigDecimal currentReturn;
    private BigDecimal maxReturnAchieved;
    private BigDecimal currentStopLossPercent;
    private BigDecimal stopLossPrice;
    private int adjustmentCount;
    private int priceUpdateCount;
    private DynamicStopLossTracker.Status status;

    /**
     * 计算盈亏比（修复除以零错误）
     */
    public BigDecimal getRiskRewardRatio() {
        if (currentReturn.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal risk = currentStopLossPercent.abs();
        if (risk.compareTo(BigDecimal.ZERO) == 0) {
            // 如果风险为0（止损点为0%），则盈亏比为无穷大，用9999表示
            return BigDecimal.valueOf(9999.99);
        }
        return currentReturn.divide(risk, 4, RoundingMode.HALF_UP);
    }
}
