package com.okx.trading.infrastructure.okx.entity.rest.trading.grid;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import com.okx.trading.infrastructure.okx.entity.rest.IOkxRestRsp;

import java.math.BigDecimal;

/**
 * @author Forest Wang
 * @package com.okx.trading.infrastructure.okx.entity.rest.trading.grid
 * @class ComputeGridMarginBalanceRsp
 * @email forestwanglin@gmail.cn
 * @date 2024/7/25
 */
@ToString
@Data
public class ComputeGridMarginBalanceRsp implements IOkxRestRsp {

    /**
     * 最多可调整的保证金数量
     */
    @JSONField(name = "maxAmt")
    @JsonProperty("maxAmt")
    private BigDecimal maxAmt;

    /**
     * 调整保证金后的杠杠倍数
     */
    @JSONField(name = "lever")
    @JsonProperty("lever")
    private Integer lever;

}
