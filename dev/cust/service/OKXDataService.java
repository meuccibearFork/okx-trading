package com.okx.trading.cust.service;

import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;

public interface OKXDataService {


    BarSeries fetchKlines(String symbol, String timeframe, int limit);

    BigDecimal getTickerPrice(String symbol);

    JSONObject getAccountBalance(String ccy);


//
//    private final OKXConfig okxConfig;
//    private final OkHttpClient okHttpClient;
//    private final OKXSignUtil signUtil;
//
//    /**
//     * 获取K线数据
//     */
//    public BarSeries fetchKlines(String symbol, String timeframe, int limit) {
//        try {
//            String instId = convertSymbol(symbol);
//            String url = okxConfig.getApi().getBaseUrl() +
//                "/api/v5/market/candles?instId=" + instId +
//                "&bar=" + timeframe + "&limit=" + limit;
//
//            Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//
//            // 签名请求（如果API需要）
//            if (!okxConfig.getApi().isDemoMode()) {
//                request = signUtil.signRequest(request,
//                    okxConfig.getApi().getKey(),
//                    okxConfig.getApi().getSecret(),
//                    okxConfig.getApi().getPassphrase());
//            }
//
//            try (Response response = okHttpClient.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("HTTP请求失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                JSONObject result = signUtil.parseResponse(responseBody);
//
//                return parseCandlesToBarSeries(result, symbol, timeframe);
//            }
//        } catch (Exception e) {
//            log.error("获取K线数据失败: symbol={}, timeframe={}", symbol, timeframe, e);
//            throw new RuntimeException("获取K线数据失败", e);
//        }
//    }
//
//    /**
//     * 获取实时价格
//     */
//    public Num getTickerPrice(String symbol) {
//        try {
//            String instId = convertSymbol(symbol);
//            String url = okxConfig.getApi().getBaseUrl() +
//                "/api/v5/market/ticker?instId=" + instId;
//
//            Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//
//            try (Response response = okHttpClient.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("HTTP请求失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                JSONObject result = signUtil.parseResponse(responseBody);
//
//                if (result instanceof JSONArray && ((JSONArray) result).size() > 0) {
//                    JSONObject ticker = ((JSONArray) result).getJSONObject(0);
//                    String priceStr = ticker.getString("last");
//                    return PrecisionNum.valueOf(new BigDecimal(priceStr));
//                }
//
//                throw new RuntimeException("未获取到价格数据");
//            }
//        } catch (Exception e) {
//            log.error("获取实时价格失败: symbol={}", symbol, e);
//            throw new RuntimeException("获取实时价格失败", e);
//        }
//    }
//
//    /**
//     * 获取交易对信息
//     */
//    public JSONObject getInstrumentInfo(String symbol) {
//        try {
//            String instId = convertSymbol(symbol);
//            String url = okxConfig.getApi().getBaseUrl() +
//                "/api/v5/public/instruments?instType=SPOT&instId=" + instId;
//
//            Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//
//            try (Response response = okHttpClient.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("HTTP请求失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                return signUtil.parseResponse(responseBody);
//            }
//        } catch (Exception e) {
//            log.error("获取交易对信息失败: symbol={}", symbol, e);
//            throw new RuntimeException("获取交易对信息失败", e);
//        }
//    }
//
//    /**
//     * 解析K线数据为BarSeries
//     */
//    private BarSeries parseCandlesToBarSeries(JSONObject data, String symbol, String timeframe) {
//        try {
//            JSONArray candles = data.getJSONArray("data");
//            BaseBarSeriesBuilder builder = new BaseBarSeriesBuilder()
//                .withName(symbol)
//                .withNumTypeOf(PrecisionNum.class);
//
//            List<Bar> bars = new ArrayList<>();
//
//            // OKX返回的数据是时间倒序，需要反转
//            for (int i = candles.size() - 1; i >= 0; i--) {
//                JSONArray candle = candles.getJSONArray(i);
//
//                // 解析字段：[时间戳, 开盘价, 最高价, 最低价, 收盘价, 成交量, 成交额]
//                long timestamp = candle.getLong(0);
//                BigDecimal open = candle.getBigDecimal(1);
//                BigDecimal high = candle.getBigDecimal(2);
//                BigDecimal low = candle.getBigDecimal(3);
//                BigDecimal close = candle.getBigDecimal(4);
//                BigDecimal volume = candle.getBigDecimal(5);
//                BigDecimal amount = candle.getBigDecimal(6);
//
//                // 创建Bar对象（ta4j 0.18方式）
//                Bar bar = BaseBar.builder()
//                    .timePeriod(parseDuration(timeframe))
//                    .endTime(Instant.ofEpochMilli(timestamp))
//                    .openPrice(PrecisionNum.valueOf(open))
//                    .highPrice(PrecisionNum.valueOf(high))
//                    .lowPrice(PrecisionNum.valueOf(low))
//                    .closePrice(PrecisionNum.valueOf(close))
//                    .volume(PrecisionNum.valueOf(volume))
//                    .amount(PrecisionNum.valueOf(amount))
//                    .build();
//
//                bars.add(bar);
//            }
//
//            // 添加到builder
//            for (Bar bar : bars) {
//                builder.addBar(bar);
//            }
//
//            return builder.build();
//
//        } catch (Exception e) {
//            log.error("解析K线数据失败", e);
//            throw new RuntimeException("解析K线数据失败", e);
//        }
//    }
//
//    /**
//     * 转换时间框架字符串为Duration
//     */
//    private Duration parseDuration(String timeframe) {
//        switch (timeframe) {
//            case "1m": return Duration.ofMinutes(1);
//            case "3m": return Duration.ofMinutes(3);
//            case "5m": return Duration.ofMinutes(5);
//            case "15m": return Duration.ofMinutes(15);
//            case "30m": return Duration.ofMinutes(30);
//            case "1H": return Duration.ofHours(1);
//            case "4H": return Duration.ofHours(4);
//            case "1D": return Duration.ofDays(1);
//            default: return Duration.ofHours(1);
//        }
//    }
//
//    /**
//     * 转换交易对格式
//     */
//    private String convertSymbol(String symbol) {
//        return symbol.replace("-", "");
//    }
//
//    /**
//     * 获取账户余额
//     */
//    public JSONObject getAccountBalance(String ccy) {
//        try {
//            String url = okxConfig.getApi().getBaseUrl() +
//                "/api/v5/account/balance" +
//                (ccy != null ? "?ccy=" + ccy : "");
//
//            Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//
//            if (!okxConfig.getApi().isDemoMode()) {
//                request = signUtil.signRequest(request,
//                    okxConfig.getApi().getKey(),
//                    okxConfig.getApi().getSecret(),
//                    okxConfig.getApi().getPassphrase());
//            }
//
//            try (Response response = okHttpClient.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("HTTP请求失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                return signUtil.parseResponse(responseBody);
//            }
//        } catch (Exception e) {
//            log.error("获取账户余额失败", e);
//            throw new RuntimeException("获取账户余额失败", e);
//        }
//    }
//
//    /**
//     * 获取当前持仓
//     */
//    public JSONArray getPositions() {
//        try {
//            String url = okxConfig.getApi().getBaseUrl() + "/api/v5/account/positions";
//
//            Request request = new Request.Builder()
//                .url(url)
//                .get()
//                .build();
//
//            if (!okxConfig.getApi().isDemoMode()) {
//                request = signUtil.signRequest(request,
//                    okxConfig.getApi().getKey(),
//                    okxConfig.getApi().getSecret(),
//                    okxConfig.getApi().getPassphrase());
//            }
//
//            try (Response response = okHttpClient.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("HTTP请求失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                JSONObject result = signUtil.parseResponse(responseBody);
//
//                if (result instanceof JSONArray) {
//                    return (JSONArray) result;
//                } else {
//                    return new JSONArray();
//                }
//            }
//        } catch (Exception e) {
//            log.error("获取持仓失败", e);
//            throw new RuntimeException("获取持仓失败", e);
//        }
//    }
}
