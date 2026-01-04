package com.okx.trading;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.okx.trading.infrastructure.okx.OkxApiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import com.okx.trading.infrastructure.okx.entity.rest.trading.order.booking.PlaceOrderReq;
import com.okx.trading.infrastructure.okx.entity.ws.pri.Order;
import com.okx.trading.infrastructure.okx.enumeration.AlgoOrderType;
import com.okx.trading.infrastructure.okx.enumeration.OrderType;
import com.okx.trading.infrastructure.okx.enumeration.Side;
import com.okx.trading.infrastructure.okx.enumeration.ws.TdMode;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

import static com.okx.trading.infrastructure.okx.OkxApiService.defaultClient;

@Slf4j
public class OkxServiceTest {

    private OkxApiService getOkxService() {
        // 模拟盘
        String apiKey = System.getenv("OKX_API_KEY");
        String passphrase = System.getenv("OKX_PASSPHRASE");
        String secretKey = System.getenv("OKX_SECRET_KEY");

        System.out.println(apiKey);
        System.out.println(passphrase);
        System.out.println(secretKey);

        Duration defaultTimeout = Duration.ofSeconds(30);
        boolean simulated = true;
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 7890));
        OkHttpClient client = defaultClient(apiKey, secretKey, passphrase, simulated, defaultTimeout)
                .newBuilder()
//                .proxy(proxy)
                .build();
//        return new OkxApiService(buildApi(apiKey, secretKey, passphrase, simulated, defaultTimeout), client, simulated);
        return new OkxApiService(apiKey, secretKey, passphrase);
    }

    @Test
    public void testGET() {
        OkxApiService service = getOkxService();
        final var accountBalanceOkxRestResponse = service.getBalance("BTC");
        log.info("testGET: {}", JSON.toJSONString(accountBalanceOkxRestResponse));
    }

    @Test
    public void placeOrder() {
        OkxApiService service = getOkxService();
        var response = service.placeOrder(PlaceOrderReq.builder()
                .instId("BTC-USDT")
                .tdMode(TdMode.ISOLATED)
                .side(Side.BUY)
                .ordType(OrderType.MARKET)
                .sz(BigDecimal.ONE)
                .build());

//        response = service.cancelOrder(CancelOrderReq.builder()
//                .instId("BTC-USDT")
//                .ordId("hell")
//                .build());


        log.info("test: {}", response);
    }

    @Test
    public void getAiParam() {
        OkxApiService service = getOkxService();
        var response = service.getGridAiParameterPublic(
                AlgoOrderType.GRID,
                "BTC-USDT", null, "7D");
        log.info("test: {}", response);
    }

    @Test
    public void testIssue2() throws JsonProcessingException {
        ObjectMapper objectMapper = defaultObjectMapper();
        Order order = objectMapper.readValue("{\"tpTriggerPxType\": \"a\"}", Order.class);
        System.out.println(order);
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

}
