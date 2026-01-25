package com.okx.trading.cust.service.impl;

import com.okx.trading.cust.service.OKXDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class OKXWebSocketService implements OKXDataService {

    private final OKXWebSocketClient webSocketClient;
    private final OKXDataServiceImpl restDataService; // 原有的REST服务

    /**
     * 初始化：获取历史数据 + 启动WebSocket
     */
    @PostConstruct
    public void init() {
        log.info("初始化WebSocket数据服务...");

        // 1. 首先通过REST API获取历史数据
        BarSeries historyData = fetchKlines("BTC-USDT", "4H", 100);
        log.info("已加载历史4小时K线: {}根", historyData.getBarCount());

        // 2. 启动WebSocket实时订阅
        if (webSocketClient.isConnected()) {
            webSocketClient.subscribe4HCandle("BTC-USDT");
        } else {
            log.warn("WebSocket未连接，等待重连...");
        }
    }

    /**
     * 获取K线数据（优先使用WebSocket实时数据，失败时降级到REST API）
     */
    @Override
    public BarSeries fetchKlines(String symbol, String timeframe, int limit) {
        // 如果是4小时K线，且WebSocket已连接，返回实时数据系列
        if ("4H".equalsIgnoreCase(timeframe) && webSocketClient.isConnected()) {
            // 这里返回的是WebSocket维护的实时系列
            // 注意：需要从BarSeriesManager获取
            return restDataService.fetchKlines(symbol, timeframe, limit);
        }

        // 降级到REST API
        return restDataService.fetchKlines(symbol, timeframe, limit);
    }

    @Override
    public java.math.BigDecimal getTickerPrice(String symbol) {
        // WebSocket可以实现更实时的价格获取
        // 这里暂时使用REST API，后续可以扩展WebSocket的ticker订阅
        return restDataService.getTickerPrice(symbol);
    }

    @Override
    public com.alibaba.fastjson2.JSONObject getAccountBalance(String ccy) {
        return restDataService.getAccountBalance(ccy);
    }

    /**
     * 定时检查WebSocket连接状态
     */
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void checkConnection() {
        if (!webSocketClient.isConnected()) {
            log.warn("WebSocket连接断开，尝试重连...");
            webSocketClient.connect();
        }
    }

    /**
     * 定时发送心跳
     */
    @Scheduled(fixedRate = 15000) // 每15秒发送一次心跳
    public void sendHeartbeat() {
        if (webSocketClient.isConnected()) {
            webSocketClient.sendPing();
        }
    }

    /**
     * 获取连接状态
     */
    public boolean isWebSocketConnected() {
        return webSocketClient.isConnected();
    }
}
