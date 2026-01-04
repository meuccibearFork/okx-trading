package com.okx.trading.infrastructure.okx.entity.ws.request.pri;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.okx.trading.infrastructure.okx.entity.ws.request.WsChannelRequestArg;
import com.okx.trading.infrastructure.okx.enumeration.InstrumentType;
import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class LiquidationWarningArg implements WsChannelRequestArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.LIQUIDATION_WARNING;

    /**
     * 产品类型
     */
    @NonNull
    @JSONField(name = "instType")
    @JsonProperty("instType")
    private InstrumentType instType;

    /**
     * 交易品种
     * 适用于交割/永续/期权
     */
    @JSONField(name = "instFamily")
    @JsonProperty("instFamily")
    private String instFamily;

    /**
     * 产品ID
     */
    @JSONField(name = "instId")
    @JsonProperty("instId")
    private String instId;

}
