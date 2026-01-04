package com.okx.trading;

import com.alibaba.fastjson2.JSON;
import com.okx.trading.model.entity.CandlestickEntity;
import com.okx.trading.service.HistoricalDataService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DemoApplicationTests {


    @Resource
    HistoricalDataService historicalDataService;

    @Test
    void contextLoads() {

        // 获取历史数据
        String symbol = "XRP-USDT";
        String interval = "4H";
        List<CandlestickEntity> candlesticks = historicalDataService.fetchAndSaveHistoryWithIntegrityCheck(symbol, interval, "2025-12-31 00:48:20", "2025-12-31 20:48:20");

        System.out.println(JSON.toJSONString(candlesticks));


    }

}
