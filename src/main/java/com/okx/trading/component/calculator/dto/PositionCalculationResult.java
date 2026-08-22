package com.okx.trading.component.calculator.dto;

import com.okex.open.api.constant.PositionSide;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算结果类
 */
@Slf4j
@Data
public class PositionCalculationResult {
    /**
     * 开仓均价
     */
    private BigDecimal entryPrice;

    /**
     * 当前价格
     */
    private BigDecimal currentPrice;

    /**
     * 仓位大小
     */
    private BigDecimal positionSize;

    /**
     * 杠杆倍数
     */
    private BigDecimal leverage;

    /**
     * 持仓方向
     */
    private PositionSide positionSide;

    /**
     * 价格涨跌幅
     */
    private BigDecimal priceChangePercent;

    /**
     * 盈亏金额(USDT)
     */
    private BigDecimal profitAmount;

    /**
     * 仓位价值
     */
    private BigDecimal positionValue;

    /**
     * 所需保证金
     */
    private BigDecimal requiredMargin;

    /**
     * 盈亏百分比
     */
    private BigDecimal profitPercentage;

    /**
     * 估算强平价
     */
    private BigDecimal estimatedLiquidationPrice;

    /**
     * 基于开仓参数的计算结果
     *
     */
    public String printSummary() {
        return printSummary("\n");
    }

    public String printSummary(String prefix) {
        // 先计算所需变量
        BigDecimal priceDifference = currentPrice.subtract(entryPrice).setScale(6, RoundingMode.HALF_UP);
        BigDecimal marginReturnRate = priceChangePercent.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                .multiply(leverage)
                .multiply(new BigDecimal("100"))
                .setScale(4, RoundingMode.HALF_UP);

        // 合并输出一段日志
        String logBuilder = "\n=== 基于开仓参数的计算结果 ===" +
                "\n持仓方向: " + positionSide +
                "#n开仓均价: " + entryPrice +
                "#n当前价格: " + currentPrice +
                "#n仓位大小: " + positionSize +
                "#n杠杆倍数: " + leverage + "倍" +
                "\n--------------------------------" +
                "\n价格涨跌幅: " + priceChangePercent + "%" +
                "#n盈亏金额: " + profitAmount + " USD" +
                "#n仓位价值: " + positionValue + " USD" +
                "#n所需保证金: " + requiredMargin + " USD" +
                "#n盈亏百分比: " + profitPercentage + "%[profitPercentage]" +
                "#n估算强平价: " + estimatedLiquidationPrice +
                "\n--------------------------------" +
                "\n验证计算:" +
                "#n价格差: " + priceDifference +
                "#n杠杆收益率 = 价格涨跌幅 × 杠杆" +
                "#n  = " + marginReturnRate + "%[marginReturnRate]";
        return logBuilder.replaceAll("#n", prefix);
    }

    public void printOutSummary() {
        System.out.println("\n=== 基于开仓参数的计算结果 ===");
        System.out.println("持仓方向: " + positionSide);
        System.out.println("开仓均价: " + entryPrice);
        System.out.println("当前价格: " + currentPrice);
        System.out.println("仓位大小: " + positionSize);
        System.out.println("杠杆倍数: " + leverage + "倍");
        System.out.println("--------------------------------");
        System.out.println("价格涨跌幅: " + priceChangePercent + "%");
        System.out.println("盈亏金额: " + profitAmount + " USD");
        System.out.println("仓位价值: " + positionValue + " USD");
        System.out.println("所需保证金: " + requiredMargin + " USD");
        System.out.println("盈亏百分比: " + profitPercentage + "%");
        System.out.println("估算强平价: " + estimatedLiquidationPrice);
        System.out.println("--------------------------------");
        System.out.println("验证计算:");
        System.out.println("价格差: " + currentPrice.subtract(entryPrice).setScale(6, RoundingMode.HALF_UP));
        System.out.println("杠杆收益率 = 价格涨跌幅 × 杠杆");
        System.out.println("  = " + priceChangePercent.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                .multiply(leverage).multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP) + "%");
    }

    /**
     * 杠杆收益率
     */
    public BigDecimal leverageYield(){
        return priceChangePercent.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                .multiply(leverage)
                .multiply(new BigDecimal("100"))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public void toProfitPercentage() {
        System.out.println("盈亏百分比: " + getProfitPercentage() + "%");
    }
}
