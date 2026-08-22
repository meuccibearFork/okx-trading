package com.okx.trading.blackhorse;

import cn.hutool.core.date.DateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BacktestRunner {
    public static void main(String[] args) {
        // 可接受命令行参数：trigger offset points
        double trigger = 10.0, offset = 10.0, points = 10.0;
        if (args.length >= 3) {
            trigger = Double.parseDouble(args[0]);
            offset = Double.parseDouble(args[1]);
            points = Double.parseDouble(args[2]);
        }

        // 生成模拟 1 小时 K 线（1000 根）
        List<Bar> bars = generateSimulatedBars(1000);

        // 初始化策略
        BlackHorseStrategyA strategy = new BlackHorseStrategyA(trigger, offset, points);

        // 回测
        for (Bar bar : bars) {
            strategy.onBar(bar);
        }

        // 输出统计报告
        StatsCollector stats = strategy.getStats();
        String report = stats.generateReport();
        System.out.println(report);

        // 生成 AI 提示词
        String prompt = PromptGenerator.generateAIPrompt(stats, trigger, offset, points);
        System.out.println("\n========== AI 调参提示词 ==========");
        System.out.println(prompt);
    }

    private static List<Bar> generateSimulatedBars(int count) {
        List<Bar> bars = new ArrayList<>();
        long baseTime = System.currentTimeMillis() - count * 3600_000L;
        double price = 100.0;
        Random rand = new Random(42);
        for (int i = 0; i < count; i++) {
            long time = baseTime + i * 3600_000L;
            double change = (rand.nextDouble() - 0.48) * 2.0;
            price += change;
            double open = price;
            double high = open + rand.nextDouble() * 3;
            double low = open - rand.nextDouble() * 3;
            double close = low + rand.nextDouble() * (high - low);
            bars.add(new Bar(open, high, low, close, time));
        }
        return bars;
    }
}
