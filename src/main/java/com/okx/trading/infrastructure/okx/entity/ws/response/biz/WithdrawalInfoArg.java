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
public class WithdrawalInfoArg implements WsChannelResponseArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.WITHDRAWAL_INFO;

    /**
     * 币种名称，如 BTC
     */
    @JSONField(name = "ccy")
    @JsonProperty("ccy")
    private String ccy;

    /**
     * 用户标识
     */
    @JSONField(name = "uid")
    @JsonProperty("uid")
    private String uid;

}
