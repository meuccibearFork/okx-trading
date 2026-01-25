package com.okx.trading.strategy.cust;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OKX数据管理器 - 获取市场数据
 */
public class OKXDataManager {

    private static final String BASE_URL = "https://www.okx.com";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final CloseableHttpClient httpClient;
    private final String apiKey;
    private final String secretKey;
    private final String passphrase;

    public OKXDataManager(String apiKey, String secretKey, String passphrase) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = passphrase;
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * 获取K线数据
     */
    public BarSeries fetchKlines(String symbol, String timeframe, int limit) throws Exception {
        String endpoint = String.format("%s/api/v5/market/candles?instId=%s&bar=%s&limit=%d",
                BASE_URL, convertSymbol(symbol), timeframe, limit);

        HttpGet request = new HttpGet(endpoint);
        addHeaders(request);

        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        return parseCandles(responseBody, symbol, timeframe);
    }

    /**
     * 获取当前价格
     */
    public Num getTicker(String symbol) throws Exception {
        String endpoint = String.format("%s/api/v5/market/ticker?instId=%s",
                BASE_URL, convertSymbol(symbol));

        HttpGet request = new HttpGet(endpoint);
        addHeaders(request);

        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonNode root = mapper.readTree(responseBody);
        if (root.get("code").asText().equals("0")) {
            String priceStr = root.get("data").get(0).get("last").asText();
            return DecimalNum.valueOf(new BigDecimal(priceStr));
        }

        throw new RuntimeException("获取价格失败: " + responseBody);
    }

    /**
     * 获取交易对信息
     */
    public Map<String, Object> getInstrumentInfo(String symbol) throws Exception {
        String endpoint = String.format("%s/api/v5/public/instruments?instType=SPOT&instId=%s",
                BASE_URL, convertSymbol(symbol));

        HttpGet request = new HttpGet(endpoint);
        HttpResponse response = httpClient.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JsonNode root = mapper.readTree(responseBody);
        Map<String, Object> info = new HashMap<>();

        if (root.get("code").asText().equals("0")) {
            JsonNode data = root.get("data").get(0);
            info.put("minSz", new BigDecimal(data.get("minSz").asText()));
            info.put("tickSz", new BigDecimal(data.get("tickSz").asText()));
            info.put("lotSz", new BigDecimal(data.get("lotSz").asText()));
            info.put("ctVal", new BigDecimal(data.get("ctVal").asText()));
        }

        return info;
    }

    /**
     * 解析K线数据为BarSeries
     */
    private BarSeries parseCandles(String responseBody, String symbol, String timeframe) throws Exception {
        JsonNode root = mapper.readTree(responseBody);

        BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder()
                .withName(symbol);

        if (root.get("code").asText().equals("0")) {
            JsonNode data = root.get("data");

            // OKX返回的数据是按时间倒序的，需要反转
            List<Bar> bars = new ArrayList<>();
            for (int i = data.size() - 1; i >= 0; i--) {
                JsonNode candle = data.get(i);

                // 解析字段：时间戳，开盘价，最高价，最低价，收盘价，成交量，成交额
                long timestamp = Long.parseLong(candle.get(0).asText());
                BigDecimal open = new BigDecimal(candle.get(1).asText());
                BigDecimal high = new BigDecimal(candle.get(2).asText());
                BigDecimal low = new BigDecimal(candle.get(3).asText());
                BigDecimal close = new BigDecimal(candle.get(4).asText());
                BigDecimal volume = new BigDecimal(candle.get(5).asText());
                BigDecimal amount = new BigDecimal(candle.get(6).asText());

                // 创建Bar - ta4j 0.18方式

                Bar bar = new BaseBar(
                        parseDuration(timeframe),
                        Instant.ofEpochMilli(timestamp),
                        DecimalNum.valueOf(open),
                        DecimalNum.valueOf(high),
                        DecimalNum.valueOf(low),
                        DecimalNum.valueOf(close),
                        DecimalNum.valueOf(volume),
                        DecimalNum.valueOf(BigDecimal.ZERO), // 默认成交额为0
                        0L // 交易次数，默认为0
                );

                bars.add(bar);
            }

            // 添加到builder
            builder.withBars(bars);
        }

        return builder.build();
    }

    /**
     * 添加请求头
     */
    private void addHeaders(HttpRequestBase request) {
        request.setHeader("OK-ACCESS-KEY", apiKey);
        request.setHeader("OK-ACCESS-SIGN", generateSign(request));
        request.setHeader("OK-ACCESS-PASSPHRASE", passphrase);
        request.setHeader("OK-ACCESS-TIMESTAMP", String.valueOf(System.currentTimeMillis() / 1000));
        request.setHeader("Content-Type", "application/json");
    }

    /**
     * 生成签名（简化版，实际需要完整实现）
     */
    private String generateSign(HttpRequestBase request) {
        // 实际需要根据OKX API规范生成签名
        // 这里返回示例值
        return "example_sign";
    }

    private String convertSymbol(String symbol) {
        return symbol.replace("-", "");
    }

    private Duration parseDuration(String timeframe) {
        switch (timeframe) {
            case "1m":
                return Duration.ofMinutes(1);
            case "5m":
                return Duration.ofMinutes(5);
            case "15m":
                return Duration.ofMinutes(15);
            case "30m":
                return Duration.ofMinutes(30);
            case "1H":
                return Duration.ofHours(1);
            case "4H":
                return Duration.ofHours(4);
            case "1D":
                return Duration.ofDays(1);
            default:
                return Duration.ofHours(1);
        }
    }

    public void close() throws Exception {
        httpClient.close();
    }
}
