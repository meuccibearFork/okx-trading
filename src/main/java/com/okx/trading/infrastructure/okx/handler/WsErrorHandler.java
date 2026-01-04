package com.okx.trading.infrastructure.okx.handler;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import com.okx.trading.infrastructure.okx.OkxWsApiService;
import com.okx.trading.infrastructure.okx.entity.ws.response.ErrorResponse;
import com.okx.trading.infrastructure.okx.enumeration.ws.WsChannel;

@Slf4j
public class WsErrorHandler implements WsHandler {

    private final WsChannel wsChannel;
    private final ErrorResponse errorResponse;

    public WsErrorHandler(WsChannel wsChannel, String message) {
        this.wsChannel = wsChannel;
        this.errorResponse = JSONObject.parseObject(message, ErrorResponse.class);
    }

    @Override
    public void handle(OkxWsApiService okxWsApiService) {
        log.debug("wsChannel:{}", wsChannel);
        okxWsApiService.getWsMessageListener().onOperateError(errorResponse);
    }

}
