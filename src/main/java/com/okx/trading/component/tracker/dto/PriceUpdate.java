package com.okx.trading.component.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格更新记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceUpdate {
    private BigDecimal price;
    private BigDecimal returnPercentage;
    private LocalDateTime updateTime;
}
