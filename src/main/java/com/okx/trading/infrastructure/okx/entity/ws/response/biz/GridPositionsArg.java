package com.okx.trading.infrastructure.okx.entity.ws.response.biz;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsChannelResponseArg;
import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class GridPositionsArg implements WsChannelResponseArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.DEPOSIT_INFO;

    /**
     * Algo Order ID
     */
    @NonNull
    @JSONField(name = "algoId")
    @JsonProperty("algoId")
    private String algoId;

    /**
     * User Identifier
     */
    @JSONField(name = "uid")
    @JsonProperty("uid")
    private String uid;

}
