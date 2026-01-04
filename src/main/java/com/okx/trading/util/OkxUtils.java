package com.okx.trading.util;

import com.okx.trading.config.OkxApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/1/3 01:18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OkxUtils {

    private final OkxApiConfig okxApiConfig;

    /**
     * 构建请求头
     *
     * @param timestamp   ISO格式的时间戳
     * @param method      HTTP方法
     * @param requestPath 请求路径
     * @param body        请求体
     * @param isSimulated 是否为模拟交易
     * @return 包含认证信息的请求头
     */
    public Map<String, String> buildHeaders(String timestamp, String method, String requestPath, String body, boolean isSimulated) {
        Map<String, String> headers = new HashMap<>();

        headers.put("OK-ACCESS-KEY", okxApiConfig.getApiKey());
        headers.put("OK-ACCESS-SIGN", SignatureUtil.sign(timestamp, method, requestPath, body, okxApiConfig.getSecretKey()));
        headers.put("OK-ACCESS-TIMESTAMP", timestamp);
        headers.put("OK-ACCESS-PASSPHRASE", okxApiConfig.getPassphrase());
        headers.put("Content-Type", "application/json");

        // 如果是模拟交易，设置模拟交易的标志
        if (isSimulated) {
            headers.put("x-simulated-trading", "1");
        }

        return headers;
    }

    /**
     *
     * @param timestamp
     * @param method
     * @param requestPath
     * @param body
     * @param isSimulated
     * @return
     */
    public Request.Builder buildRequest(String timestamp, String method, String requestPath, String body, boolean isSimulated) {
        Map<String, String> stringStringMap = buildHeaders(timestamp, method, requestPath, body, isSimulated);
        Request.Builder requestBuilder = new Request.Builder();

        stringStringMap.forEach(requestBuilder::addHeader);
        return requestBuilder;
    }

}
