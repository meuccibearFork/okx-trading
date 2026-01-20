package com.okx.trading.dto;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 持仓查询响应专用类（可选，提供类型别名）
 */
public class PositionsResponse extends ApiResponse<PositionDetail> {

    /**
     * 过滤出特定产品类型的持仓
     */
    public List<PositionDetail> filterByInstType(InstType instType) {
        return getData().stream()
                .filter(p -> p.getInstType() == instType)
                .toList();
    }

    /**
     * 过滤出有盈利的持仓
     */
    public List<PositionDetail> getProfitablePositions() {
        return getData().stream()
                .filter(p -> p.getUpl() != null && p.getUpl().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    /**
     * 计算总未实现盈亏
     */
    public BigDecimal getTotalUpl() {
        return getData().stream()
                .map(PositionDetail::getUpl)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 增强的持仓信息打印方法
     */
    public void printEnhancedPositions() {
        PositionsResponse response = this;
        if (!response.isSuccess()) {
            System.err.println("请求失败: " + response.getMsg());
            return;
        }

        List<PositionDetail> positions = response.getData();
        if (positions == null || positions.isEmpty()) {
            System.out.println("当前没有持仓");
            return;
        }

        System.out.println("=== 持仓概览 ===");
        System.out.printf("总持仓数: %d | 总未实现盈亏: %s USD%n",
                positions.size(), response.getTotalUpl());

        // 按产品类型分组显示
        Map<InstType, List<PositionDetail>> grouped = positions.stream()
                .collect(Collectors.groupingBy(PositionDetail::getInstType));

        for (Map.Entry<InstType, List<PositionDetail>> entry : grouped.entrySet()) {
            System.out.printf("%n【%s】%n", entry.getKey().getDescription());

            for (PositionDetail pos : entry.getValue()) {
                String uplColor = pos.getUpl().compareTo(BigDecimal.ZERO) >= 0 ? "\u001B[32m" : "\u001B[31m";
                System.out.printf("  %-20s %-6s 数量:%-10s 均价:%-10s %s盈亏:%-10s\u001B[0m%n",
                        pos.getInstId(),
                        pos.getPosSide().getDescription(),
                        pos.getPos().toPlainString(),
                        pos.getAvgPx().toPlainString(),
                        uplColor,
                        pos.getUpl().toPlainString());

                // 显示风险信息（仅对合约类）
                if (pos.getInstType() == InstType.SWAP || pos.getInstType() == InstType.FUTURES) {

                    System.out.printf("    强平价: %-10s 保证金率: %-6s 杠杆: %s倍%n    json:%s%n",
                            pos.getLiqPx() != null ? pos.getLiqPx().toPlainString() : "N/A",
                            pos.getMgnRatio() != null ?
                                    pos.getMgnRatio().multiply(BigDecimal.valueOf(100)).toPlainString() + "%" : "N/A",
                            pos.getLever().toPlainString(), JSONObject.toJSONString(pos));
                }
            }
        }

        // 显示统计信息
        System.out.printf("%n=== 统计信息 ===");
        System.out.printf("%n盈利持仓: %d个", response.getProfitablePositions().size());

        BigDecimal totalMargin = positions.stream()
                .map(PositionDetail::getMargin)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.printf("%n总占用保证金: %s USD%n", totalMargin.toPlainString());
    }
}
