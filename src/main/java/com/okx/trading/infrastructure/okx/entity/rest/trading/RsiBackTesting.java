package com.okx.trading.infrastructure.okx.entity.rest.trading;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.okx.trading.infrastructure.okx.entity.rest.IOkxRestRsp;
import com.okx.trading.infrastructure.okx.entity.rest.IRestEntity;

/**
 * @author Forest Wang
 * @package com.okx.trading.infrastructure.okx.entity.rest.trading
 * @class RsiBackTesting
 * @email forestwanglin@gmail.cn
 * @date 2024/7/25
 */
@ToString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RsiBackTesting implements IRestEntity, IOkxRestRsp {

    /**
     * 触发次数
     */
    @JSONField(name = "triggerNum")
    @JsonProperty("triggerNum")
    private Integer triggerNum;

}
