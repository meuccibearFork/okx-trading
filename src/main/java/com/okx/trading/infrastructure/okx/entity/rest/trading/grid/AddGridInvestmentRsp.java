package com.okx.trading.infrastructure.okx.entity.rest.trading.grid;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import com.okx.trading.infrastructure.okx.entity.rest.IOkxRestRsp;

/**
 * @author Forest Wang
 * @package com.okx.trading.infrastructure.okx.entity.rest.trading.grid
 * @class AddGridInvestmentRsp
 * @email forestwanglin@gmail.cn
 * @date 2024/7/25
 */
@ToString
@Data
public class AddGridInvestmentRsp implements IOkxRestRsp {

    @JSONField(name = "algoId")
    @JsonProperty("algoId")
    private String algoId;

}
