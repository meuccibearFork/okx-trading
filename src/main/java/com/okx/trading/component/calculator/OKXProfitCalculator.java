package com.okx.trading.component.calculator;

import com.okex.open.api.bean.PositionDetail;
import com.okex.open.api.constant.PositionSide;
import com.okx.trading.component.calculator.dto.PositionCalculationResult;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * OKX合约盈亏百分比计算器（基于开仓均价和当前价格）
 */
@Slf4j
public class OKXProfitCalculator {

    /**
     * 计算具体盈亏金额
     *
     * @param entryPrice   开仓均价
     * @param currentPrice 当前价格
     * @param positionSize 仓位大小（实际数量）
     * @param positionSide 持仓方向
     * @return 盈亏金额
     */
    public static BigDecimal calculateProfitAmount(
            BigDecimal entryPrice,
            BigDecimal currentPrice,
            BigDecimal positionSize,
            PositionSide positionSide) {

        BigDecimal priceDifference;
        if (PositionSide.LONG == positionSide) {
            priceDifference = currentPrice.subtract(entryPrice);
        } else if (PositionSide.SHORT == positionSide) {
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
     *
     * @param entryPrice   开仓均价
     * @param positionSize 仓位大小
     * @param leverage     杠杆倍数
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
     * 计算价格涨跌幅
     *
     * @param entryPrice   开仓均价
     * @param currentPrice 当前价格
     * @param positionSide 持仓方向
     */
    public static BigDecimal getBigDecimal(BigDecimal entryPrice, BigDecimal currentPrice, PositionSide positionSide) {
        BigDecimal priceChangePercent;
        if (PositionSide.LONG == positionSide) {
            // ((标记价 - 开仓价) / 开仓价) * 100
            priceChangePercent = currentPrice.subtract(entryPrice)
                    .divide(entryPrice, 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        } else {
            priceChangePercent = entryPrice.subtract(currentPrice)
                    .divide(entryPrice, 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return priceChangePercent;
    }

    /**
     * 综合计算：从开仓参数计算所有结果
     *
     */
    public static PositionCalculationResult calculateAll(PositionDetail positionDetail) {
        //开仓价
        BigDecimal entryPrice = positionDetail.getAvgPx();
        //标记价
        BigDecimal currentPrice = positionDetail.getMarkPx();
        //持仓数量
        BigDecimal positionSize = positionDetail.getPos();
        //杠杆
        BigDecimal leverage = positionDetail.getLever();
        //方向
        PositionSide positionSide = positionDetail.getPosSide();


        PositionCalculationResult result = new PositionCalculationResult();

        // 设置基础参数
        result.setEntryPrice(entryPrice);
        result.setCurrentPrice(currentPrice);
        result.setPositionSize(positionSize);
        result.setLeverage(leverage);
        result.setPositionSide(positionSide);

        // 计算价格涨跌幅
        BigDecimal priceChangePercent = OKXProfitCalculator.getBigDecimal(entryPrice, currentPrice, positionSide);
        result.setPriceChangePercent(priceChangePercent.setScale(4, RoundingMode.HALF_UP));

        // 计算盈亏金额
        BigDecimal profitAmount = OKXProfitCalculator.calculateProfitAmount(entryPrice, currentPrice, positionSize, positionSide);
        result.setProfitAmount(profitAmount);

        // 计算仓位价值
        BigDecimal positionValue = entryPrice.multiply(positionSize);
        result.setPositionValue(positionValue.setScale(4, RoundingMode.HALF_UP));

        // 计算所需保证金
        BigDecimal requiredMargin = OKXProfitCalculator.calculateRequiredMargin(entryPrice, positionSize, leverage);
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
        if (PositionSide.LONG == positionSide) {
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
}
