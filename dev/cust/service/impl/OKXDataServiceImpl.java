package com.okx.trading.cust.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.okx.trading.cust.service.OKXDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OKXDataServiceImpl implements OKXDataService {

    private final OkHttpClient okHttpClient;

    @Value("${okx.api.base-url:https://www.okx.com}")
    private String baseUrl;

    @Value("${okx.api.key:}")
    private String apiKey;

    @Value("${okx.api.secret:}")
    private String secretKey;

    @Value("${okx.api.passphrase:}")
    private String passphrase;

    @Value("${okx.api.demo-mode:true}")
    private boolean demoMode;

    @Override
    public BarSeries fetchKlines(String symbol, String timeframe, int limit) {
        try {
            String instId = convertSymbol(symbol);
            String url = baseUrl + "/api/v5/market/candles?instId=" + instId
                       + "&bar=" + timeframe + "&limit=" + limit;

            log.info("请求OKX K线数据: {}, {}, {}", symbol, timeframe, limit);

            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            // 如果是实盘，添加签名
            if (!demoMode) {
                request = signRequest(request);
            }

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("OKX API请求失败: {}", response.code());
                    throw new RuntimeException("HTTP请求失败: " + response.code());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);

                String code = jsonResponse.getString("code");
                if (!"0".equals(code)) {
                    String msg = jsonResponse.getString("msg");
                    log.error("OKX API错误: {} - {}", code, msg);
                    throw new RuntimeException("OKX API错误: " + msg);
                }

                JSONArray data = jsonResponse.getJSONArray("data");
                return parseCandlesToBarSeries(data, symbol, timeframe);
            }

        } catch (Exception e) {
            log.error("获取K线数据失败: symbol={}, timeframe={}", symbol, timeframe, e);
            throw new RuntimeException("获取K线数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public BigDecimal getTickerPrice(String symbol) {
        try {
            String instId = convertSymbol(symbol);
            String url = baseUrl + "/api/v5/market/ticker?instId=" + instId;

            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            if (!demoMode) {
                request = signRequest(request);
            }

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("HTTP请求失败: " + response.code());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);

                String code = jsonResponse.getString("code");
                if (!"0".equals(code)) {
                    String msg = jsonResponse.getString("msg");
                    throw new RuntimeException("OKX API错误: " + msg);
                }

                JSONArray data = jsonResponse.getJSONArray("data");
                if (data != null && !data.isEmpty()) {
                    JSONObject ticker = data.getJSONObject(0);
                    String lastPrice = ticker.getString("last");
                    return new BigDecimal(lastPrice);
                }

                throw new RuntimeException("未获取到价格数据");
            }
        } catch (Exception e) {
            log.error("获取实时价格失败: {}", symbol, e);
            throw new RuntimeException("获取实时价格失败: " + e.getMessage(), e);
        }
    }

    @Override
    public JSONObject getAccountBalance(String ccy) {
        try {
            String url = baseUrl + "/api/v5/account/balance";
            if (ccy != null && !ccy.isEmpty()) {
                url += "?ccy=" + ccy;
            }

            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            if (!demoMode) {
                request = signRequest(request);
            }

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("HTTP请求失败: " + response.code());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = JSONObject.parseObject(responseBody);

                String code = jsonResponse.getString("code");
                if (!"0".equals(code)) {
                    String msg = jsonResponse.getString("msg");
                    throw new RuntimeException("OKX API错误: " + msg);
                }

                return jsonResponse;
            }
        } catch (Exception e) {
            log.error("获取账户余额失败", e);
            throw new RuntimeException("获取账户余额失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析K线数据为BarSeries
     */
    private BarSeries parseCandlesToBarSeries(JSONArray candles, String symbol, String timeframe) {
        try {
            if (candles == null || candles.isEmpty()) {
                throw new RuntimeException("K线数据为空");
            }

            BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder()
                .withName(symbol);

            List<Bar> bars = new ArrayList<>();

            // OKX返回的数据是时间倒序，需要反转
            for (int i = candles.size() - 1; i >= 0; i--) {
                JSONArray candle = candles.getJSONArray(i);

                // 解析字段：[时间戳, 开盘价, 最高价, 最低价, 收盘价, 成交量, 成交额]
                String timestampStr = candle.getString(0);
                String openStr = candle.getString(1);
                String highStr = candle.getString(2);
                String lowStr = candle.getString(3);
                String closeStr = candle.getString(4);
                String volumeStr = candle.getString(5);
                String amountStr = candle.getString(6);

                long timestamp = Long.parseLong(timestampStr);
                BigDecimal open = new BigDecimal(openStr);
                BigDecimal high = new BigDecimal(highStr);
                BigDecimal low = new BigDecimal(lowStr);
                BigDecimal close = new BigDecimal(closeStr);
                BigDecimal volume = new BigDecimal(volumeStr);
                BigDecimal amount = new BigDecimal(amountStr);

                // 创建Bar对象（ta4j 0.18方式）
                Bar bar = BaseBar.builder()
                    .timePeriod(parseDuration(timeframe))
                    .endTime(Instant.ofEpochMilli(timestamp))
                    .openPrice(DecimalNum.valueOf(open))
                    .highPrice(DecimalNum.valueOf(high))
                    .lowPrice(DecimalNum.valueOf(low))
                    .closePrice(DecimalNum.valueOf(close))
                    .volume(DecimalNum.valueOf(volume))
                    .amount(DecimalNum.valueOf(amount))
                    .build();

                bars.add(bar);
            }

            // 添加到builder
            for (Bar bar : bars) {
                builder.addBar(bar);
            }

            BarSeries series = builder.build();
            log.info("成功解析K线数据: {}根, 时间范围: {} 到 {}",
                series.getBarCount(),
                series.getFirstBar().getEndTime(),
                series.getLastBar().getEndTime());

            return series;

        } catch (Exception e) {
            log.error("解析K线数据失败", e);
            throw new RuntimeException("解析K线数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换时间框架字符串为Duration
     */
    private Duration parseDuration(String timeframe) {
        switch (timeframe.toLowerCase()) {
            case "1m": return Duration.ofMinutes(1);
            case "5m": return Duration.ofMinutes(5);
            case "15m": return Duration.ofMinutes(15);
            case "30m": return Duration.ofMinutes(30);
            case "1h": return Duration.ofHours(1);
            case "4h": return Duration.ofHours(4);
            case "1d": return Duration.ofDays(1);
            default: return Duration.ofHours(1);
        }
    }

    /**
     * 转换交易对格式
     */
    private String convertSymbol(String symbol) {
        return symbol.replace("-", "");
    }

    /**
     * 签名请求（简化版，实际需要完整实现）
     */
    private Request signRequest(Request originalRequest) {
        // 这里需要实现OKX的签名逻辑
        // 由于签名逻辑较复杂，这里提供简化示例
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String method = originalRequest.method();
        String requestPath = originalRequest.url().encodedPath();
        String query = originalRequest.url().encodedQuery();

        if (query != null && !query.isEmpty()) {
            requestPath += "?" + query;
        }

        String body = "";
        if (originalRequest.body() != null) {
            // 这里需要获取请求体内容
        }

        // 生成签名（需要实现HMAC SHA256）
        String signature = generateSignature(timestamp, method, requestPath, body);

        return originalRequest.newBuilder()
            .addHeader("OK-ACCESS-KEY", apiKey)
            .addHeader("OK-ACCESS-SIGN", signature)
            .addHeader("OK-ACCESS-TIMESTAMP", timestamp)
            .addHeader("OK-ACCESS-PASSPHRASE", passphrase)
            .addHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * 生成签名（需要实现）
     */
    private String generateSignature(String timestamp, String method,
                                    String requestPath, String body) {
        // 实际实现：使用HMAC SHA256加密 timestamp + method + requestPath + body
        // 这里返回示例值
        return "mock_signature_for_demo_mode";
    }
}
