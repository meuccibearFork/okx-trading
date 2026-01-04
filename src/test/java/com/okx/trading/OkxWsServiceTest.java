package com.okx.trading;

import com.alibaba.fastjson2.JSON;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import com.okx.trading.infrastructure.okx.entity.ws.biz.IndexCandlesticks;
import com.okx.trading.infrastructure.okx.entity.ws.pri.Account;
import com.okx.trading.infrastructure.okx.entity.ws.pub.OpenInterest;
import com.okx.trading.infrastructure.okx.entity.ws.request.WsRequest;
import com.okx.trading.infrastructure.okx.entity.ws.request.pri.*;
import com.okx.trading.infrastructure.okx.entity.ws.request.pub.InstrumentsArg;
import com.okx.trading.infrastructure.okx.entity.ws.request.pub.OpenInterestArg;
import com.okx.trading.infrastructure.okx.entity.ws.response.ErrorResponse;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsOnceResponse;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsResponse;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsSubscribeResponse;
import com.okx.trading.infrastructure.okx.entity.ws.response.biz.IndexCandlesticksArg;
import com.okx.trading.infrastructure.okx.enumeration.InstrumentType;
import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;
import com.okx.trading.infrastructure.okx.enumeration.ws.Operation;
import com.okx.trading.infrastructure.okx.enumeration.ws.WsChannel;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.okx.trading.infrastructure.okx.OkxWsApiService.defaultClient;

@Slf4j
public class OkxWsServiceTest {

    private OkxWsApiService getOkxWsService() {
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client = defaultClient(Duration.ofMillis(300000))
                .newBuilder()
//                .proxy(proxy)
                .build();
        return new OkxWsApiService(client, true);
    }

    @Test
    public void entityTest() {
        WsRequest wsRequest = WsRequest.builder()
                .op(Operation.SUBSCRIBE)
                .args(List.of(
                        InstrumentsArg.builder()
                                .channel(Channel.INSTRUMENTS)
                                .instType(InstrumentType.SPOT)
                                .build()
                )).build();
        log.info("{}: {}", wsRequest, JSON.toJSONString(wsRequest));
    }

    @Test
    public void test() throws InterruptedException {
        OkxWsApiService wsApiService = getOkxWsService();
        wsApiService.connect(WsChannel.PUBLIC);
        wsApiService.connect(WsChannel.PRIVATE);
        wsApiService.connect(WsChannel.BUSINESS);

        wsApiService.setWsMessageListener(new WsMessageListener() {

            @Override
            public void onLoginSuccess() {
                log.info("onLoginSuccess");
            }

            @Override
            public void onOperateAccount(@NonNull WsResponse<com.okx.trading.infrastructure.okx.entity.ws.response.pri.AccountArg> response) {
                log.info("onOperateAccount: {}", JSON.toJSONString(response));
            }

            @Override
            public void onReceiveAccount(@NonNull WsSubscribeResponse<com.okx.trading.infrastructure.okx.entity.ws.response.pri.AccountArg, Account> response) {
                log.info("onReceiveAccount: {}", JSON.toJSONString(response));
            }

            @Override
            public void onOperateError(@NonNull ErrorResponse response) {
                log.info("onOperateError: {}", JSON.toJSONString(response));
            }

            @Override
            public void onReceiveOpenInterest(@NonNull WsSubscribeResponse<com.okx.trading.infrastructure.okx.entity.ws.response.pub.OpenInterestArg, OpenInterest> response) {
                log.info("onReceiveOpenInterest: {}", JSON.toJSONString(response));
            }

            @Override
            public void onOperateOpenInterest(@NonNull WsResponse<com.okx.trading.infrastructure.okx.entity.ws.response.pub.OpenInterestArg> response) {
                log.info("onOperateOpenInterest: {}", JSON.toJSONString(response));
            }

            @Override
            public void onOperateIndexCandle(@NonNull WsResponse<IndexCandlesticksArg> response) {
                log.info("onOperateIndexCandle: {}", JSON.toJSONString(response));
            }

            @Override
            public void onReceiveIndexCandle(@NonNull WsSubscribeResponse<IndexCandlesticksArg, IndexCandlesticks> response) {
                log.info("onReceiveIndexCandle: {}", JSON.toJSONString(response));
            }

            @Override
            public void onPlaceOrderResponse(@NonNull WsOnceResponse<com.okx.trading.infrastructure.okx.entity.ws.response.pri.PlaceOrderArg> response) {
                log.info("onPlaceOrderResponse: {}", JSON.toJSONString(response));
            }
        });

        wsApiService.subscribeAccount(AccountArg.builder()
                .ccy("BTC")
                .build());
        TimeUnit.SECONDS.sleep(5L);
        wsApiService.subscribeOpenInterest(OpenInterestArg.builder()
                .instId("LTC-USD-SWAP")
                .build());
        TimeUnit.SECONDS.sleep(5L);
        wsApiService.subscribeOpenInterest(OpenInterestArg.builder()
                .instId("BTC-USD-SWAP")
                .build());
        TimeUnit.SECONDS.sleep(10L);
        wsApiService.unsubscribeOpenInterest(OpenInterestArg.builder()
                .instId("BTC-USD-SWAP")
                .build());

        wsApiService.subscribeOpenInterest(OpenInterestArg.builder()
                .instId("LTC-USD-SWAP")
                .build());
        TimeUnit.SECONDS.sleep(10L);
        wsApiService.unsubscribeOpenInterest(OpenInterestArg.builder()
                .instId("LTC-USD-SWAP")
                .build());
        TimeUnit.SECONDS.sleep(5);
        // 模拟盘
        String apiKey = System.getenv("API_KEY");
        String passphrase = System.getenv("PASSPHRASE");
        String secretKey = System.getenv("SECRET_KEY");


//        apiKey = "b81e589c-864a-4401-bedf-819608d0c51f";
//        passphrase = "9WVku96XdasrC9u.";
//        secretKey = "F4B5AD175F7AE9457048ED8E39BDB91D";
        wsApiService.login(LoginArg.builder()
                .apiKey(apiKey)
                .passphrase(passphrase)
                .timestamp(System.currentTimeMillis() / 1000 + "")
                .build(), secretKey);

        TimeUnit.SECONDS.sleep(5);

        wsApiService.subscribeAccount(AccountArg.builder()
                .ccy("BTC")
                .build());

        wsApiService.subscribePositions(PositionsArg.builder()
                .instType(InstrumentType.ANY)
                .build());

        wsApiService.subscribeBalanceAndPosition(BalanceAndPositionArg.builder().build());
        wsApiService.subscribeLiquidationWarning(LiquidationWarningArg.builder()
                        .instType(InstrumentType.ANY)
                .build());
//        wsApiService.subscribeAccountGreeks(AccountGreeksArg.builder().build());
//        wsApiService.subscribeDepositInfo(DepositInfoArg.builder()
//                .ccy("BTC")
//                .build());

//        wsApiService.subscribeIndexCandle(IndexCandleArg.builder()
//                .channel(Channel.INDEX_CANDLE_1M)
//                .instId("BTC-USD")
//                .build());
        TimeUnit.MINUTES.sleep(30L);
    }

}
