package com.okx.trading.infrastructure.okx.entity.ws.request.pub;

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
public class LiquidationOrdersArg implements WsChannelRequestArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.LIQUIDATION_ORDERS;

    /**
     * 产品类型
     * MARGIN：币币杠杆
     * SWAP：永续合约
     * FUTURES：交割合约
     * OPTION：期权
     */
    @NonNull
    @JSONField(name = "instType")
    @JsonProperty("instType")
    private InstrumentType instType;

}
