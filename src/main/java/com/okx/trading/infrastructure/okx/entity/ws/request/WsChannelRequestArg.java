package com.okx.trading.infrastructure.okx.entity.ws.request;

import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;

/**
 * The interface of websocket channel argument
 */
public interface WsChannelRequestArg extends WsRequestArg {

    Channel getChannel();

}
