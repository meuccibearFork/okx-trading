package com.okx.trading.cust.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trade_orders", indexes = {
    @Index(name = "idx_order_id", columnList = "orderId"),
    @Index(name = "idx_symbol_status", columnList = "symbol,status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class TradeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderId;  // OKX订单ID

    @Column(nullable = false)
    private String symbol;   // 交易对

    @Column(nullable = false)
    private String side;     // buy/sell

    @Column(nullable = false)
    private String type;     // market/limit/stop

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal filledPrice = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status;   // pending/filled/canceled/failed

    @Column(nullable = false)
    private String category = "strategy";  // strategy/manual

    @Column
    private String strategyName = "HeimaA";

    @Column
    private String stopLossOrderId;

    @Column
    private String takeProfitOrderId;

    @Column(precision = 20, scale = 8)
    private BigDecimal stopLossPrice;

    @Column(precision = 20, scale = 8)
    private BigDecimal takeProfitPrice;

    @Column(columnDefinition = "TEXT")
    private String metadata;  // JSON格式的附加信息

    @CreationTimestamp
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime filledAt;

    // 转换方法
    public Num getPriceNum() {
        return DecimalNum.valueOf(price);
    }

    public Num getQuantityNum() {
        return DecimalNum.valueOf(quantity);
    }

    public void setMetadataObject(JSONObject json) {
        this.metadata = json.toJSONString();
    }

    public JSONObject getMetadataObject() {
        return metadata != null ? JSONObject.parseObject(metadata) : new JSONObject();
    }

    public boolean isFilled() {
        return "filled".equals(status);
    }

    public boolean isActive() {
        return "pending".equals(status) || "partially_filled".equals(status);
    }
}
