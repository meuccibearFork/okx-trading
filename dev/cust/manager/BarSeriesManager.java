package com.okx.trading.cust.manager;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class BarSeriesManager {

    // 存储不同时间框架的BarSeries
    private final Map<String, BarSeries> barSeriesMap = new ConcurrentHashMap<>();

    /**
     * 策略核心数据系列(获取基础时间框架序列)(基础时间框架（如5分钟）
     */
    @Getter
    private BarSeries baseSeries;
    /**
     * 获取高时间框架序列(高时间框架（4小时）
     */
    @Getter
    private BarSeries higherSeries;

    @PostConstruct
    public void init() {
        // 初始化基础系列
        baseSeries = new BaseBarSeriesBuilder()
                .withName("BTC-USDT_5m")
                .build();

        // 初始化4小时系列
        higherSeries = new BaseBarSeriesBuilder()
                .withName("BTC-USDT_4H")
                .build();

        barSeriesMap.put("BTC-USDT_5m", baseSeries);
        barSeriesMap.put("BTC-USDT_4H", higherSeries);

        log.info("BarSeries管理器初始化完成");
    }

    /**
     * 更新Bar数据
     *
     * @param seriesKey 系列键名
     * @param newBar    新的Bar数据
     * @param isNewBar  是否是新Bar（true=新增，false=更新最后一根）
     */
    public synchronized void updateBar(String seriesKey, Bar newBar, boolean isNewBar) {
        BarSeries series = barSeriesMap.get(seriesKey);
        if (series == null) {
            log.warn("BarSeries不存在: {}", seriesKey);
            return;
        }

        try {
            if (series.getBarCount() == 0) {
                // 第一次添加
                series.addBar(newBar);
                log.info("首次添加Bar到系列: {}, 时间={}", seriesKey, newBar.getEndTime());
                return;
            }

            Bar lastBar = series.getLastBar();

            if (!isNewBar) {
                // 更新最后一根Bar（当前K线还未完成）
                if (lastBar.getEndTime().equals(newBar.getEndTime())) {
                    // 相同时间，更新Bar
                    updateLastBar(series, newBar);
                } else {
                    // 应该不会发生这种情况，除非数据异常
                    log.warn("时间不匹配: last={}, new={}",
                            lastBar.getEndTime(), newBar.getEndTime());
                }
            } else {
                // 新增一根Bar（K线已完成）
                if (lastBar.getEndTime().equals(newBar.getEndTime())) {
                    // 更新时间相同的Bar（用最终确认的数据）
                    updateLastBar(series, newBar);
                } else if (lastBar.getEndTime().isBefore(newBar.getEndTime())) {
                    // 新增Bar
                    series.addBar(newBar);
                    log.debug("新增Bar到系列: {}, 时间={}, 总数量={}",
                            seriesKey, newBar.getEndTime(), series.getBarCount());

                    // 触发策略检查（这里可以调用策略服务）
                    triggerStrategyCheck();
                } else {
                    log.warn("收到的时间早于最后一根Bar: last={}, new={}",
                            lastBar.getEndTime(), newBar.getEndTime());
                }
            }

        } catch (Exception e) {
            log.error("更新Bar数据异常", e);
        }
    }

    /**
     * 更新最后一根Bar
     */
    private void updateLastBar(BarSeries series, Bar newBar) {
        // 移除最后一根，添加新的
        int lastIndex = series.getEndIndex();
        if (series.getBarCount() > 0) {
            // 注意：ta4j的BarSeries没有直接更新方法，需要重新构建
            // 这里简化为移除后添加
            series.addBar(newBar, true); // 第二个参数true表示替换最后一根
        }
    }

    /**
     * 触发策略检查
     */
    private void triggerStrategyCheck() {
        // 这里可以注入策略服务进行信号检查
        log.debug("K线数据更新，可触发策略检查");
        // 实际实现中，这里应该调用：strategyService.checkSignal()
    }

    /**
     * 根据键名获取系列
     */
    public BarSeries getSeries(String key) {
        return barSeriesMap.get(key);
    }

    /**
     * 获取最新的收盘价
     */
    public Double getLatestClose(String seriesKey) {
        BarSeries series = barSeriesMap.get(seriesKey);
        if (series != null && series.getBarCount() > 0) {
            return series.getLastBar().getClosePrice().doubleValue();
        }
        return null;
    }

    /**
     * 获取系列数据量
     */
    public int getBarCount(String seriesKey) {
        BarSeries series = barSeriesMap.get(seriesKey);
        return series != null ? series.getBarCount() : 0;
    }

    /**
     * 清理旧数据（防止内存泄漏）
     */
    public void cleanOldData(int maxBars) {
        barSeriesMap.forEach((key, series) -> {
            if (series.getBarCount() > maxBars) {
                // 这里需要实现数据清理逻辑
                log.debug("系列 {} 数据量: {}", key, series.getBarCount());
            }
        });
    }
}
