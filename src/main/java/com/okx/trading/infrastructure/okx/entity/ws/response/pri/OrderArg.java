package com.okx.trading.infrastructure.okx.entity.ws.response.pri;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.okx.trading.infrastructure.okx.entity.ws.response.WsChannelResponseArg;
import com.okx.trading.infrastructure.okx.enumeration.InstrumentType;
import com.okx.trading.infrastructure.okx.enumeration.ws.Channel;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class OrderArg  implements WsChannelResponseArg {

    @Builder.Default
    @JSONField(name = "channel")
    @JsonProperty("channel")
    private Channel channel = Channel.ORDERS;

    /**
     * 产品类型
     * SPOT：币币
     * MARGIN：币币杠杆
     * SWAP：永续合约
     * FUTURES：交割合约
     * OPTION：期权
     * ANY：全部
     */
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

