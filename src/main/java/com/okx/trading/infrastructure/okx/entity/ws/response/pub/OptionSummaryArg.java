package com.okx.trading.infrastructure.okx.entity.ws.response.pub;

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
public class OptionSummaryArg implements WsChannelResponseArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.OPT_SUMMARY;

    /**
     * 交易品种
     */
    @JSONField(name = "instFamily")
    @JsonProperty("instFamily")
    private String instFamily;


}

