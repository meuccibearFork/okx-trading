package com.okx.trading.infrastructure.okx.entity.rest.trading.grid;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import com.okx.trading.infrastructure.okx.entity.rest.IOkxRestRsp;

/**
 * @author Forest Wang
 * @package com.okx.trading.infrastructure.okx.entity.rest.trading.grid
 * @class InstantTriggerGridAlgoOrderReq
 * @email forestwanglin@gmail.cn
 * @date 2024/7/25
 */
@ToString
@Data
public class InstantTriggerGridAlgoOrderRsp implements IOkxRestRsp {

    /**
     * 策略订单ID
     */
    @JSONField(name = "algoId")
    @JsonProperty("algoId")
    private String algoId;

    /**
     * 用户自定义策略ID
     */
    @JSONField(name = "algoClOrdId")
    @JsonProperty("algoClOrdId")
    private String algoClOrdId;

}
