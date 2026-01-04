package com.okx.trading.infrastructure.okx.handler;

import lombok.extern.slf4j.Slf4j;
import com.okx.trading.infrastructure.okx.OkxWsApiService;
import com.okx.trading.infrastructure.okx.WsMessageListener;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsOnceResponse;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsResponseArg;
import com.okx.trading.infrastructure.okx.enumeration.ws.Operation;
import com.okx.trading.infrastructure.okx.enumeration.ws.WsChannel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class WsOnceHandler<T extends WsResponseArg> implements WsHandler {

    private final WsOnceResponse<T> response;
    private final WsChannel wsChannel;
    private final Operation operation;

    public WsOnceHandler(Class<T> tClass,
                         WsChannel wsChannel,
                         String message,
                         Operation operation) {
        this.response = WsOnceResponse.tryParse(tClass, message);
        this.wsChannel = wsChannel;
        this.operation = operation;
    }

    @Override
    public void handle(OkxWsApiService okxWsApiService) {
        log.debug("wsChannel {} {}", wsChannel, operation);
        try {
            if (response != null && okxWsApiService.getWsMessageListener() != null) {
                Method method = WsMessageListener.class.getDeclaredMethod(operation.getRspCallbackMethodName(), WsOnceResponse.class);
                method.setAccessible(true);
                method.invoke(okxWsApiService.getWsMessageListener(), response);
            }
        } catch (NoSuchMethodException e) {
            log.error("handle error", e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
