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
public class MarkPriceCandlesticksArg implements WsChannelRequestArg {

    // 新建的时候必须指定channel
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel;

    /**
     * 产品ID
     */
    @NonNull
    @JSONField(name = "instId")
    @JsonProperty("instId")
    private String instId;

}
