package com.okx.trading.blackhorse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlackHorseStrategyA {
    private final int htfMinutes = 240;          // 4小时
    private final double triggerPoints;
    private final double trailOffset;
    private final double trailPoints;
    private final double pipSize;

    @Getter
    private int position = 0;                // 0:空, 1:多, -1:空
    @Getter
    private double stopLossPrice = 0.0;
    @Getter
    private double currentProfitPoints = 0.0;
    @Getter
    private boolean trailingActive = false;

    private double entryPrice = 0.0;
    private double highestSinceEntry = 0.0;
    private double lowestSinceEntry = 0.0;

    private Bar currentHtfBar = null;
    private Bar previousHtfBar = null;
    @Getter
    private boolean isNewHtfBar = false;
    private long lastHtfTime = 0;
    private double prevHtfHigh = 0.0;
    private double prevHtfLow = 0.0;

    @Setter
    private StatsCollector stats;

    public BlackHorseStrategyA(double triggerPoints, double trailOffset, double trailPoints) {
        this(triggerPoints, trailOffset, trailPoints, 1.0);
    }

    public BlackHorseStrategyA(double triggerPoints, double trailOffset, double trailPoints, double pipSize) {
        this.triggerPoints = triggerPoints;
        this.trailOffset = trailOffset;
        this.trailPoints = trailPoints;
        this.pipSize = pipSize;
        this.stats = new StatsCollector();
    }

    public StatsCollector getStats() { return stats; }

    public void onBar(Bar bar) {
        updateHtfData(bar);

        if (position == 0) {
            if (isNewHtfBar && currentHtfBar != null && previousHtfBar != null) {
                double avgCurrent = (currentHtfBar.getOpen() + currentHtfBar.getHigh() + currentHtfBar.getLow() + currentHtfBar.getClose()) / 4.0;
                double avgPrev = (previousHtfBar.getOpen() + previousHtfBar.getHigh() + previousHtfBar.getLow() + previousHtfBar.getClose()) / 4.0;
                if (avgCurrent > avgPrev) {
                    openLong(bar.getClose());
                } else if (avgCurrent < avgPrev) {
                    openShort(bar.getClose());
                }
            }
        } else {
            updateProfit(bar.getClose());

            if (trailingActive) {
                updateTrailingStop(bar.getClose());
            }

            if (!trailingActive && currentProfitPoints >= triggerPoints && currentProfitPoints >= trailPoints) {
                activateTrailingStop();
            }

            checkStopLoss(bar);
            stats.updateFloatingProfit(currentProfitPoints);
        }

        if (isNewHtfBar && currentHtfBar != null) {
            previousHtfBar = currentHtfBar;
            prevHtfHigh = previousHtfBar.getHigh();
            prevHtfLow = previousHtfBar.getLow();
        }
    }

    private void updateHtfData(Bar bar) {
        long periodStart = (bar.getTime() / (htfMinutes * 60 * 1000L)) * (htfMinutes * 60 * 1000L);
        if (periodStart != lastHtfTime) {
            if (currentHtfBar != null) {
                previousHtfBar = currentHtfBar;
            }
            currentHtfBar = new Bar(bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), periodStart);
            lastHtfTime = periodStart;
            isNewHtfBar = true;
        } else {
            if (currentHtfBar != null) {
                currentHtfBar.setHigh(Math.max(currentHtfBar.getHigh(), bar.getHigh()));
                currentHtfBar.setLow(Math.min(currentHtfBar.getLow(), bar.getLow()));
                currentHtfBar.setClose(bar.getClose());
            }
            isNewHtfBar = false;
        }
    }

    private void openLong(double price) {
        position = 1;
        entryPrice = price;
        stopLossPrice = prevHtfLow;
        highestSinceEntry = price;
        lowestSinceEntry = price;
        currentProfitPoints = 0.0;
        trailingActive = false;
        stats.onOpen("LONG", price, System.currentTimeMillis());
        log.info("[开多] 价格={}, 初始止损={}", String.format("%.2f", price), String.format("%.2f", stopLossPrice));
    }

    private void openShort(double price) {
        position = -1;
        entryPrice = price;
        stopLossPrice = prevHtfHigh;
        highestSinceEntry = price;
        lowestSinceEntry = price;
        currentProfitPoints = 0.0;
        trailingActive = false;
        stats.onOpen("SHORT", price, System.currentTimeMillis());
        log.info("[开空] 价格={}, 初始止损={}", String.format("%.2f", price), String.format("%.2f", stopLossPrice));
    }

    private void closePosition(double price, String reason) {
        stats.onClose(price, System.currentTimeMillis(), currentProfitPoints);
        log.info("[平仓] {} @ {}, 盈亏={} 点", reason, String.format("%.2f", price), String.format("%.2f", currentProfitPoints));
        position = 0;
        entryPrice = 0.0;
        stopLossPrice = 0.0;
        currentProfitPoints = 0.0;
        trailingActive = false;
    }

    private void updateProfit(double currentPrice) {
        if (position == 1) {
            currentProfitPoints = (currentPrice - entryPrice) / pipSize;
            highestSinceEntry = Math.max(highestSinceEntry, currentPrice);
        } else if (position == -1) {
            currentProfitPoints = (entryPrice - currentPrice) / pipSize;
            lowestSinceEntry = Math.min(lowestSinceEntry, currentPrice);
        } else {
            currentProfitPoints = 0.0;
        }
    }

    private void activateTrailingStop() {
        trailingActive = true;
        log.info("[移动止盈] 已激活");
        updateTrailingStop(position == 1 ? highestSinceEntry : lowestSinceEntry);
    }

    private void updateTrailingStop(double currentPrice) {
        if (!trailingActive) return;
        if (position == 1) {
            double newStop = highestSinceEntry - trailOffset;
            if (newStop > stopLossPrice) {
                stopLossPrice = newStop;
                log.info("[移动止损] 多头更新: 最高={}, 新止损={}",
                    String.format("%.2f", highestSinceEntry), String.format("%.2f", stopLossPrice));
            }
        } else if (position == -1) {
            double newStop = lowestSinceEntry + trailOffset;
            if (newStop < stopLossPrice) {
                stopLossPrice = newStop;
                log.info("[移动止损] 空头更新: 最低={}, 新止损={}",
                    String.format("%.2f", lowestSinceEntry), String.format("%.2f", stopLossPrice));
            }
        }
    }

    private void checkStopLoss(Bar bar) {
        if (position == 1 && bar.getLow() <= stopLossPrice) {
            closePosition(stopLossPrice, "多头止损");
        } else if (position == -1 && bar.getHigh() >= stopLossPrice) {
            closePosition(stopLossPrice, "空头止损");
        }
    }
}
