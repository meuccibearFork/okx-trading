package com.okx.trading.infrastructure.okx.handler;

import com.okx.trading.infrastructure.okx.OkxWsApiService;

public interface WsHandler {

    void handle(OkxWsApiService okxWsApiService);

}
