package com.okx.trading;

import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.bean.account.result.PositionDetail;
import com.okex.open.api.bean.calculator.PositionCalculationResult;
import com.okex.open.api.calculator.OKXProfitCalculator;
import com.okex.open.api.constant.PositionSide;

import java.math.BigDecimal;

/**
 * OKX合约盈亏百分比计算器（基于开仓均价和当前价格）
 */
public class OKXProfitCalculatorTest {

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
        PositionSide positionSide = PositionSide.LONG;

        BigDecimal contractMultiplier = new BigDecimal("100"); // 1张=100个XRP
        BigDecimal actualPositionSize = positionSize.multiply(contractMultiplier); // 实际持仓1个XRP

        System.out.println("【参数设置】");
        System.out.println("开仓均价: " + entryPrice + " USD");
        System.out.println("当前价格: " + currentPrice + " USD");
        System.out.println("仓位大小: " + actualPositionSize + " XRP (0.01张 × 100)");
        System.out.println("杠杆倍数: " + leverage + "倍");
        System.out.println("持仓方向: " + positionSide);

        //TODO 方法1: 使用公式计算
        System.out.println("\n【方法1】使用公式计算盈亏百分比:");
        BigDecimal profitPercent = OKXProfitCalculator.calculateLongProfitPercentage(
                entryPrice, currentPrice, leverage, actualPositionSize, contractMultiplier);
        System.out.println("盈亏百分比: " + profitPercent + "%");

        //TODO 方法2: 详细计算
        System.out.println("\n【方法2】详细计算所有指标:");
        PositionCalculationResult result = OKXProfitCalculator.calculateAll(
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

        //TODO 从JSON数据计算
        System.out.println("\n【方法3】从JSON数据计算:");
        String jsonStr = "{\"adl\":1,\"availPos\":0.01,\"avgPx\":1.8938,\"cTime\":\"1769019742986\",\"instId\":\"XRP-USDT-SWAP\",\"instType\":\"SWAP\",\"last\":1.896,\"lever\":10,\"liqPx\":1.7165350453172203,\"margin\":0.18938,\"markPx\":1.8966,\"mgnMode\":\"isolated\",\"mgnRatio\":14.475527635919928,\"mmr\":0.0123279,\"notionalUsd\":1.894912026,\"pos\":0.01,\"posId\":\"3238521303253458944\",\"posSide\":\"long\",\"profitable\":true,\"tradeId\":\"753196733\",\"uTime\":\"1769019742986\",\"upl\":0.0028000000000001,\"uplRatio\":0.0147850881824918,\"usdPx\":0.99911}";

        PositionDetail positionDetail = JSONObject.parseObject(jsonStr, PositionDetail.class);
        PositionCalculationResult positionCalculationResult = positionDetail.calculateFromOKXData();
        if (positionCalculationResult != null) {
            positionCalculationResult.printSummary();
        }
    }


}
