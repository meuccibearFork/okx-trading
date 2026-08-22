package com.okx.trading.blackhorse;

public class PromptGenerator {
    public static String generateAIPrompt(StatsCollector stats,
                                          double trigger, double offset, double points) {
        String report = stats.generateReport();
        return String.format("""
                【角色】你是一位拥有10年经验的量化交易策略优化专家。
                【任务】基于以下回测统计数据，优化"黑马策略A"的止损/止盈参数。

                【当前策略参数】
                - 触发移动止盈的盈利阈值 (trigger_points): %.1f
                - 移动止损偏移量 (trail_offset): %.1f
                - 移动止损启动点数 (trail_points): %.1f

                【回测统计结果】
                %s

                【优化目标】
                1. 提高胜率和盈亏比。
                2. 控制最大回撤在15%%以内。
                3. 建议方向：调大 trigger_points 以过滤噪音？还是调小 trail_offset 以保护利润？

                【要求】
                请给出3组新的参数组合 (trigger, offset, points)，并说明每组参数适用的市场环境（震荡/趋势）。
                同时请指出当前代码是否存在其他逻辑缺陷，并给出改进建议。
                """, trigger, offset, points, report);
    }
}
