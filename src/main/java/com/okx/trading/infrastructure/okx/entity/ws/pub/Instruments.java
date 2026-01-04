package com.okx.trading.infrastructure.okx.entity.ws.pub;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.okx.trading.infrastructure.okx.entity.ws.WsSubscribeEntity;
import com.okx.trading.infrastructure.okx.enumeration.InstrumentType;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Instruments implements WsSubscribeEntity {

    /**
     * 产品类型
     */
    @JSONField(name = "instType")
    @JsonProperty("instType")
    private InstrumentType instType;

    /**
     * 产品ID，如 LTC-USD-SWAP
     */
    @JSONField(name = "instId")
    @JsonProperty("instId")
    private String instId;

}
