package com.okx.trading.strategy.test;

import com.okx.trading.strategy.DynamicStopLossTracker;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 使用示例（修复版本）
 */
@Slf4j
public class DynamicStopLossTrackerExample {

    public static void main(String[] args) {
        log.info("开始动态止损系统测试...\n");

        try {
            // 使用Builder模式创建追踪器
            DynamicStopLossTracker tracker = DynamicStopLossTracker.builder()
                    .entryPrice(BigDecimal.valueOf(100))
                    .initialStopLossPercent(BigDecimal.valueOf(-5))
                    .incrementPercent(BigDecimal.valueOf(5))
                    .build();

            // 模拟价格变化 - 从99开始，避免一开始就触发止损
            List<BigDecimal> priceSequence = List.of(
                    BigDecimal.valueOf(99),   // -1%
                    BigDecimal.valueOf(102),  // +2%
                    BigDecimal.valueOf(105),  // +5%，应该调整止损点到0%
                    BigDecimal.valueOf(107),  // +7%
                    BigDecimal.valueOf(110),  // +10%，应该调整止损点到+5%
                    BigDecimal.valueOf(108),  // +8%，但止损点已经是+5%，不会回撤
                    BigDecimal.valueOf(115),  // +15%，应该调整止损点到+10%
                    BigDecimal.valueOf(112)   // +12%，触发止损！
            );

            log.info("开始模拟价格更新...\n");

            for (int i = 0; i < priceSequence.size(); i++) {
                log.info("\n===== 第{}次价格更新: {} =====", i + 1, priceSequence.get(i));

                boolean adjusted = tracker.updatePrice(priceSequence.get(i));

                // 每次更新后显示状态
                tracker.logStatus();

                if (tracker.isStopLossTriggered()) {
                    log.info("\n⚠️ 止损已被触发！交易结束。");
                    break;
                }
            }

            // 显示调整历史
            tracker.logAdjustmentHistory();

            // 显示最终统计
            TradeStatistics stats = tracker.getStatistics();
            log.info("\n" +
                    "┌─────────────────────────────────────────────────────┐\n" +
                    "│                   最终统计                          │\n" +
                    "├─────────────────────────────────────────────────────┤\n" +
                    "│ 最终状态: {}\n" +
                    "│ 入场价格: {}\n" +
                    "│ 最终价格: {}\n" +
                    "│ 最终收益率: {}%\n" +
                    "│ 最高收益率: {}%\n" +
                    "│ 总调整次数: {}\n" +
                    "│ 总运行时间: {}秒\n" +
                    "│ 最终盈亏比: {}\n" +
                    "└─────────────────────────────────────────────────────┘",
                    stats.getStatus().getDescription(),
                    stats.getEntryPrice().setScale(4, RoundingMode.HALF_UP),
                    stats.getCurrentPrice().setScale(4, RoundingMode.HALF_UP),
                    stats.getCurrentReturn().setScale(4, RoundingMode.HALF_UP),
                    stats.getMaxReturnAchieved().setScale(4, RoundingMode.HALF_UP),
                    stats.getAdjustmentCount(),
                    java.time.Duration.between(stats.getStartTime(), stats.getLastUpdateTime()).getSeconds(),
                    stats.getRiskRewardRatio().setScale(4, RoundingMode.HALF_UP)
            );

        } catch (Exception e) {
            log.error("测试过程中出现异常: {}", e.getMessage(), e);
        }
    }
}
