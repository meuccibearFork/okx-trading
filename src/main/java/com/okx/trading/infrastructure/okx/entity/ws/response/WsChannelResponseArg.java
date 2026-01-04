package com.okx.trading.infrastructure.okx.entity.ws.response;

import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;

/**
 * The interface of websocket channel argument
 */
public interface WsChannelResponseArg extends WsResponseArg {

    Channel getChannel();

}
