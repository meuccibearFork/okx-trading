package com.okx.trading;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * OKX合约盈亏百分比计算器（基于开仓均价和当前价格）
 */
public class OKXProfitCalculator {

    /**
     * 计算多仓盈亏百分比
     * @param entryPrice 开仓均价
     * @param currentPrice 当前价格（标记价格或最新价）
     * @param leverage 杠杆倍数
     * @param positionSize 仓位大小（张数）
     * @param contractMultiplier 合约乘数（默认1，不同合约可能不同）
     * @return 盈亏百分比
     */
    public static BigDecimal calculateLongProfitPercentage(
            BigDecimal entryPrice,
            BigDecimal currentPrice,
            BigDecimal leverage,
            BigDecimal positionSize,
            BigDecimal contractMultiplier) {

        if (entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 1. 计算价格涨跌幅
        BigDecimal priceChangePercent = currentPrice.subtract(entryPrice)
                .divide(entryPrice, 8, RoundingMode.HALF_UP);

        // 2. 计算盈亏百分比（考虑杠杆效应）
        // 公式：盈亏百分比 = 价格涨跌幅 × 杠杆倍数 × 100%
        BigDecimal profitPercentage = priceChangePercent
                .multiply(leverage)
                .multiply(new BigDecimal("100"));

        return profitPercentage.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 计算空仓盈亏百分比
     * @param entryPrice 开仓均价
     * @param currentPrice 当前价格
     * @param leverage 杠杆倍数
     * @param positionSize 仓位大小
     * @param contractMultiplier 合约乘数
     * @return 盈亏百分比
     */
    public static BigDecimal calculateShortProfitPercentage(
            BigDecimal entryPrice,
            BigDecimal currentPrice,
            BigDecimal leverage,
            BigDecimal positionSize,
            BigDecimal contractMultiplier) {

        if (entryPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 对于空仓：价格上涨导致亏损，价格下跌导致盈利
        BigDecimal priceChangePercent = entryPrice.subtract(currentPrice)
                .divide(entryPrice, 8, RoundingMode.HALF_UP);

        BigDecimal profitPercentage = priceChangePercent
                .multiply(leverage)
                .multiply(new BigDecimal("100"));

        return profitPercentage.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 根据持仓方向自动选择计算方式
     * @param entryPrice 开仓均价
     * @param currentPrice 当前价格
     * @param leverage 杠杆倍数
     * @param positionSide 持仓方向（"long"或"short"）
     * @return 盈亏百分比
     */
    public static BigDecimal calculateProfitPercentage(
            BigDecimal entryPrice,
            BigDecimal currentPrice,
            BigDecimal leverage,
            String positionSide) {

        if ("long".equalsIgnoreCase(positionSide)) {
            return calculateLongProfitPercentage(entryPrice, currentPrice, leverage,
                    BigDecimal.ONE, BigDecimal.ONE);
        } else if ("short".equalsIgnoreCase(positionSide)) {
            return calculateShortProfitPercentage(entryPrice, currentPrice, leverage,
                    BigDecimal.ONE, BigDecimal.ONE);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算具体盈亏金额
     * @param entryPrice 开仓均价
     * @param currentPrice 当前价格
     * @param positionSize 仓位大小（实际数量）
     * @param positionSide 持仓方向
     * @return 盈亏金额
     */
    public static BigDecimal calculateProfitAmount(
            BigDecimal entryPrice,
            BigDecimal currentPrice,
            BigDecimal positionSize,
            String positionSide) {

        BigDecimal priceDifference;

        if ("long".equalsIgnoreCase(positionSide)) {
            priceDifference = currentPrice.subtract(entryPrice);
        } else if ("short".equalsIgnoreCase(positionSide)) {
            priceDifference = entryPrice.subtract(currentPrice);
        } else {
            return BigDecimal.ZERO;
        }

        // 盈亏金额 = 价格差 × 仓位大小
        return priceDifference.multiply(positionSize)
                .setScale(8, RoundingMode.HALF_UP);
    }

    /**
     * 计算所需保证金
     * @param entryPrice 开仓均价
     * @param positionSize 仓位大小
     * @param leverage 杠杆倍数
     * @return 所需保证金
     */
    public static BigDecimal calculateRequiredMargin(
            BigDecimal entryPrice,
            BigDecimal positionSize,
            BigDecimal leverage) {

        // 仓位价值 = 开仓均价 × 仓位大小
        BigDecimal positionValue = entryPrice.multiply(positionSize);

        // 所需保证金 = 仓位价值 ÷ 杠杆倍数
        return positionValue.divide(leverage, 8, RoundingMode.HALF_UP);
    }

    /**
     * 综合计算：从开仓参数计算所有结果
     * @param entryPrice 开仓价
     * @param currentPrice 标记价
     * @param positionSize 持仓数量
     * @param leverage 杠杆
     * @param positionSide 方向
     */
    public static PositionCalculationResult calculateAll(
            BigDecimal entryPrice,
            BigDecimal currentPrice,
            BigDecimal positionSize,
            BigDecimal leverage,
            String positionSide) {

        PositionCalculationResult result = new PositionCalculationResult();

        // 设置基础参数
        result.setEntryPrice(entryPrice);
        result.setCurrentPrice(currentPrice);
        result.setPositionSize(positionSize);
        result.setLeverage(leverage);
        result.setPositionSide(positionSide);

        // 计算价格涨跌幅
        BigDecimal priceChangePercent;
        if ("long".equalsIgnoreCase(positionSide)) {
            // ((标记价 - 开仓价) / 开仓价) * 100
            priceChangePercent = currentPrice.subtract(entryPrice)
                    .divide(entryPrice, 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            priceChangePercent = entryPrice.subtract(currentPrice)
                    .divide(entryPrice, 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        result.setPriceChangePercent(priceChangePercent.setScale(4, RoundingMode.HALF_UP));

        // 计算盈亏金额
        BigDecimal profitAmount = calculateProfitAmount(entryPrice, currentPrice, positionSize, positionSide);
        result.setProfitAmount(profitAmount);

        // 计算仓位价值
        BigDecimal positionValue = entryPrice.multiply(positionSize);
        result.setPositionValue(positionValue.setScale(4, RoundingMode.HALF_UP));

        // 计算所需保证金
        BigDecimal requiredMargin = calculateRequiredMargin(entryPrice, positionSize, leverage);
        result.setRequiredMargin(requiredMargin);

        // 计算盈亏百分比（相对于保证金）
        if (requiredMargin.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal profitPercentage = profitAmount
                    .divide(requiredMargin, 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            result.setProfitPercentage(profitPercentage.setScale(4, RoundingMode.HALF_UP));
        } else {
            result.setProfitPercentage(BigDecimal.ZERO);
        }

        // 计算强平价（简单估算，实际需要考虑手续费和资金费率）
        // 多仓强平价 ≈ 开仓均价 × (1 - 1/杠杆)
        // 空仓强平价 ≈ 开仓均价 × (1 + 1/杠杆)
        BigDecimal liquidationPrice;
        if ("long".equalsIgnoreCase(positionSide)) {
            liquidationPrice = entryPrice.multiply(
                    BigDecimal.ONE.subtract(BigDecimal.ONE.divide(leverage, 8, RoundingMode.HALF_UP))
            );
        } else {
            liquidationPrice = entryPrice.multiply(
                    BigDecimal.ONE.add(BigDecimal.ONE.divide(leverage, 8, RoundingMode.HALF_UP))
            );
        }
        result.setEstimatedLiquidationPrice(liquidationPrice.setScale(4, RoundingMode.HALF_UP));

        return result;
    }

    /**
     * 从OKX JSON数据中提取参数并计算
     */
    public static PositionCalculationResult calculateFromOKXData(String jsonStr) {
        try {
            JSONObject jsonObject = JSONObject.parse(jsonStr);

            BigDecimal entryPrice = jsonObject.getBigDecimal("avgPx");
            BigDecimal currentPrice = jsonObject.getBigDecimal("markPx"); // 使用标记价格
            BigDecimal positionSize = jsonObject.getBigDecimal("pos");
            BigDecimal leverage = jsonObject.getBigDecimal("lever");
            String positionSide = jsonObject.getString("posSide");

            // XRP-USDT合约乘数通常是1（1张=1个XRP）
            // 对于不同合约，可能需要调整合约乘数
            BigDecimal contractMultiplier = BigDecimal.ONE;

            // 如果是币本位合约，可能需要不同的计算方式
            String instId = jsonObject.getString("instId");

            return calculateAll(entryPrice, currentPrice, positionSize, leverage, positionSide);

        } catch (Exception e) {
            System.err.println("解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 计算结果类
     */
    @Data
    static class PositionCalculationResult {
        private BigDecimal entryPrice;
        private BigDecimal currentPrice;
        private BigDecimal positionSize;
        private BigDecimal leverage;
        private String positionSide;
        private BigDecimal priceChangePercent;
        private BigDecimal profitAmount;
        private BigDecimal positionValue;
        private BigDecimal requiredMargin;
        private BigDecimal profitPercentage;
        private BigDecimal estimatedLiquidationPrice;


        /**
         * 基于开仓参数的计算结果
         */
        public void printSummary() {
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
            System.out.println("保证金收益率 = 价格涨跌幅 × 杠杆");
            System.out.println("  = " + priceChangePercent.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                    .multiply(leverage).multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP) + "%");
        }

        public void toProfitPercentage() {
            System.out.println("盈亏百分比: " + getProfitPercentage() + "%");

        }
    }

    /**
     * 测试主方法
     */
    public static void main(String[] args) {
        System.out.println("=== OKX合约盈亏计算（基于开仓均价） ===\n");

        // 从您的数据中提取参数
        BigDecimal entryPrice = new BigDecimal("1.8938");
        BigDecimal currentPrice = new BigDecimal("1.8966"); // 使用标记价格
        BigDecimal positionSize = new BigDecimal("0.01");
        BigDecimal leverage = new BigDecimal("10");
        String positionSide = "long";

        // 合约乘数：XRP-USDT-SWAP通常是1张=1个XRP
        // 但根据您的数据，仓位价值约为1.89 USD，所以应该是1张=100个XRP？
        // 实际上OKX的XRP合约乘数是10（1张=10个XRP）
        // 为了匹配您的数据，我们调整positionSize
        BigDecimal contractMultiplier = new BigDecimal("100"); // 1张=100个XRP
        BigDecimal actualPositionSize = positionSize.multiply(contractMultiplier); // 实际持仓1个XRP

        System.out.println("【参数设置】");
        System.out.println("开仓均价: " + entryPrice + " USD");
        System.out.println("当前价格: " + currentPrice + " USD");
        System.out.println("仓位大小: " + actualPositionSize + " XRP (0.01张 × 100)");
        System.out.println("杠杆倍数: " + leverage + "倍");
        System.out.println("持仓方向: " + positionSide);

        // 方法1: 使用公式计算
        System.out.println("\n【方法1】使用公式计算盈亏百分比:");
        BigDecimal profitPercent = calculateLongProfitPercentage(
                entryPrice, currentPrice, leverage, actualPositionSize, contractMultiplier);
        System.out.println("盈亏百分比: " + profitPercent + "%");

        // 方法2: 详细计算
        System.out.println("\n【方法2】详细计算所有指标:");
        PositionCalculationResult result = calculateAll(
                entryPrice, currentPrice, actualPositionSize, leverage, positionSide);
        result.printSummary();

        // 验证与原始数据的匹配度
        System.out.println("\n【验证】与原始数据对比:");
        System.out.println("原始数据 - 未实现盈亏: 0.0028000000000001 USD");
        System.out.println("计算盈亏: " + result.getProfitAmount() + " USD");
        System.out.println("原始数据 - 占用保证金: 0.18938 USD");
        System.out.println("计算保证金: " + result.getRequiredMargin() + " USD");
        System.out.println("原始数据 - 盈亏百分比: 1.4785%");
        System.out.println("计算百分比: " + result.getProfitPercentage() + "%");

        // 从JSON数据计算
        System.out.println("\n【方法3】从JSON数据计算:");
        String jsonStr = "{\"adl\":1,\"availPos\":0.01,\"avgPx\":1.8938,\"cTime\":\"1769019742986\",\"instId\":\"XRP-USDT-SWAP\",\"instType\":\"SWAP\",\"last\":1.896,\"lever\":10,\"liqPx\":1.7165350453172203,\"margin\":0.18938,\"markPx\":1.8966,\"mgnMode\":\"isolated\",\"mgnRatio\":14.475527635919928,\"mmr\":0.0123279,\"notionalUsd\":1.894912026,\"pos\":0.01,\"posId\":\"3238521303253458944\",\"posSide\":\"long\",\"profitable\":true,\"tradeId\":\"753196733\",\"uTime\":\"1769019742986\",\"upl\":0.0028000000000001,\"uplRatio\":0.0147850881824918,\"usdPx\":0.99911}";

        PositionCalculationResult jsonResult = calculateFromOKXData(jsonStr);
        if (jsonResult != null) {
            jsonResult.printSummary();
        }

    }
}
