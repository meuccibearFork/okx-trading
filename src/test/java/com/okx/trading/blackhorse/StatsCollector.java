package com.okx.trading.blackhorse;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StatsCollector {
    private final List<TradeRecord> trades = new ArrayList<>();
    private TradeRecord currentTrade = null;
    private double equity = 10000.0;
    private double maxEquity = 10000.0;
    private double minEquity = 10000.0;

    public void onOpen(String direction, double price, long time) {
        currentTrade = new TradeRecord();
        currentTrade.direction = direction;
        currentTrade.entryPrice = price;
        currentTrade.entryTime = time;
        currentTrade.maxFloatingProfit = 0.0;
    }

    public void onClose(double price, long time, double profitPoints) {
        if (currentTrade == null) return;
        currentTrade.exitPrice = price;
        currentTrade.profitPoints = profitPoints;
        currentTrade.exitTime = time;
        trades.add(currentTrade);
        equity += profitPoints;
        maxEquity = Math.max(maxEquity, equity);
        minEquity = Math.min(minEquity, equity);
        currentTrade = null;
    }

    public void updateFloatingProfit(double profitPoints) {
        if (currentTrade != null && profitPoints > currentTrade.maxFloatingProfit) {
            currentTrade.maxFloatingProfit = profitPoints;
        }
    }

    public int getTotalTrades() { return trades.size(); }

    public String generateReport() {
        int total = trades.size();
        if (total == 0) return "无交易记录。";

        long wins = trades.stream().filter(t -> t.profitPoints > 0).count();
        double totalProfit = trades.stream().mapToDouble(t -> t.profitPoints).sum();
        double avgProfit = totalProfit / total;
        double maxProfit = trades.stream().mapToDouble(t -> t.profitPoints).max().orElse(0);
        double minProfit = trades.stream().mapToDouble(t -> t.profitPoints).min().orElse(0);
        double drawdown = (maxEquity - minEquity) / maxEquity * 100;
        double avgWin = trades.stream().filter(t -> t.profitPoints > 0)
                              .mapToDouble(t -> t.profitPoints).average().orElse(0);
        double avgLoss = Math.abs(trades.stream().filter(t -> t.profitPoints < 0)
                              .mapToDouble(t -> t.profitPoints).average().orElse(1));
        double profitFactor = avgWin / (avgLoss == 0 ? 1 : avgLoss);

        return String.format("""
                === 回测统计报告 ===
                总交易次数: %d
                胜率: %.2f%%
                总盈亏(点): %.2f
                平均盈亏(点): %.2f
                最大单笔盈利(点): %.2f
                最大单笔亏损(点): %.2f
                最大回撤: %.2f%%
                盈亏比: %.2f
                """, total, (double) wins / total * 100, totalProfit, avgProfit, maxProfit, minProfit, drawdown, profitFactor);
    }

    // 内部记录类
    private static class TradeRecord {
        String direction;
        double entryPrice, exitPrice, profitPoints, maxFloatingProfit;
        long entryTime, exitTime;
    }
}
