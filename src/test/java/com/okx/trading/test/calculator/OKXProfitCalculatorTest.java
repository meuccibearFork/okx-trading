package com.okx.trading.test.calculator;

import com.alibaba.fastjson.JSONObject;
import com.okex.open.api.component.calculator.dto.PositionDetail;
import com.okex.open.api.component.calculatorTracker.dto.PositionCalculationResult;

/**
 * OKX合约盈亏百分比计算器（基于开仓均价和当前价格）
 */
public class OKXProfitCalculatorTest {

    /**
     * 测试主方法
     */
    public static void main(String[] args) {

        //TODO 从JSON数据计算
        String jsonStr = "{\"adl\":1,\"availPos\":0.01,\"avgPx\":1.8938,\"cTime\":\"1769019742986\",\"instId\":\"XRP-USDT-SWAP\",\"instType\":\"SWAP\",\"last\":1.896,\"lever\":10,\"liqPx\":1.7165350453172203,\"margin\":0.18938,\"markPx\":1.8966,\"mgnMode\":\"isolated\",\"mgnRatio\":14.475527635919928,\"mmr\":0.0123279,\"notionalUsd\":1.894912026,\"pos\":0.01,\"posId\":\"3238521303253458944\",\"posSide\":\"long\",\"profitable\":true,\"tradeId\":\"753196733\",\"uTime\":\"1769019742986\",\"upl\":0.0028000000000001,\"uplRatio\":0.0147850881824918,\"usdPx\":0.99911}";

        PositionDetail positionDetail = JSONObject.parseObject(jsonStr, PositionDetail.class);
        System.out.println("【参数设置】");
        System.out.println("开仓均价: " + positionDetail.getAvgPx() + " USD");
        System.out.println("当前价格: " + positionDetail.getMarkPx() + " USD");
        System.out.println("仓位大小: " + positionDetail.getPos() + " XRP (0.01张 × 100)");
        System.out.println("杠杆倍数: " + positionDetail.getLever() + "倍");
        System.out.println("持仓方向: " + positionDetail.getPosSide());

        PositionCalculationResult positionCalculationResult = positionDetail.calculateAll();
        positionCalculationResult.printSummary();

        System.out.println("计算盈亏: " + positionCalculationResult.getProfitAmount() + " USD");
        System.out.println("计算保证金: " + positionCalculationResult.getRequiredMargin() + " USD");
        System.out.println("计算百分比: " + positionCalculationResult.getProfitPercentage() + "%");

    }


}
