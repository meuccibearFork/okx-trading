package com.okx.trading.infrastructure.okx.entity.ws.request.biz;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.okx.trading.infrastructure.okx.entity.ws.request.WsChannelRequestArg;
import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;


@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GridSubOrderArg implements WsChannelRequestArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.GRID_SUB_ORDERS;

    /**
     * 	Algo Order ID
     */
    @NonNull
    @JSONField(name = "algoId")
    @JsonProperty("algoId")
    private String algoId;

}
