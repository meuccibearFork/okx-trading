package com.okx.trading.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 持仓详细信息类
 * 对应API文档中的单个持仓对象
 */
@Data
public class PositionDetail {
    // 基础信息
    @JSONField(name = "instType")
    private InstType instType;

    @JSONField(name = "instId")
    private String instId;

    @JSONField(name = "posId")
    private String posId;

    @JSONField(name = "posSide")
    private PosSide posSide;

    // 持仓数量相关
    @JSONField(name = "pos")
    private BigDecimal pos;

    @JSONField(name = "availPos")
    private BigDecimal availPos;

    @JSONField(name = "avgPx")
    private BigDecimal avgPx;

    @JSONField(name = "upl")
    private BigDecimal upl;

    @JSONField(name = "uplRatio")
    private BigDecimal uplRatio;

    // 保证金相关
    @JSONField(name = "mgnMode")
    private MgnMode mgnMode;

    @JSONField(name = "liqPx")
    private BigDecimal liqPx;

    @JSONField(name = "imr")
    private BigDecimal imr;

    @JSONField(name = "margin")
    private BigDecimal margin;

    @JSONField(name = "mgnRatio")
    private BigDecimal mgnRatio;

    @JSONField(name = "mmr")
    private BigDecimal mmr;

    // 其他信息
    @JSONField(name = "lever")
    private BigDecimal lever;

    @JSONField(name = "interest")
    private BigDecimal interest;

    @JSONField(name = "tradeId")
    private String tradeId;

    @JSONField(name = "cTime")
    private String cTimeStr;

    @JSONField(name = "uTime")
    private String uTimeStr;

    // 盈亏计算相关
    @JSONField(name = "last")
    private BigDecimal last;

    @JSONField(name = "markPx")
    private BigDecimal markPx;

    @JSONField(name = "usdPx")
    private BigDecimal usdPx;

    // 风险指标
    @JSONField(name = "notionalUsd")
    private BigDecimal notionalUsd;

    @JSONField(name = "adl")
    private Integer adl;

    // 期权相关字段省略，可根据需要添加

    // 无参构造方法（JSON需要）
    public PositionDetail() {}

    // 便捷方法：获取Instant类型的时间
    public Instant getCTime() {
        return parseInstant(cTimeStr);
    }

    public Instant getUTime() {
        return parseInstant(uTimeStr);
    }

    private Instant parseInstant(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            // 支持两种时间格式：带毫秒和不带毫秒
            if (timeStr.contains(".")) {
                return Instant.parse(timeStr);
            } else {
                return Instant.parse(timeStr + "Z");
            }
        } catch (Exception e) {
            return null;
        }
    }

    // 便捷计算：名义价值
    public BigDecimal calculateNotionalValue() {
        if (pos != null && markPx != null) {
            return pos.multiply(markPx);
        }
        return BigDecimal.ZERO;
    }

    // 便捷计算：保证金率
    public BigDecimal calculateMarginRatio() {
        if (margin != null && notionalUsd != null && notionalUsd.compareTo(BigDecimal.ZERO) != 0) {
            return margin.divide(notionalUsd, 4, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // 判断是否为盈利持仓
    public boolean isProfitable() {
        return upl != null && upl.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String toString() {
        return String.format("Position[%s %s %s pos=%s avgPx=%s upl=%s]",
                instType != null ? instType.getDescription() : "N/A",
                instId,
                posSide != null ? posSide.getDescription() : "N/A",
                pos != null ? pos.toPlainString() : "0",
                avgPx != null ? avgPx.toPlainString() : "0",
                upl != null ? upl.toPlainString() : "0");
    }
}
