package com.okx.trading.infrastructure.okx.entity.rest.trading.grid;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.okx.trading.infrastructure.okx.entity.rest.IOkxRestReq;

import java.math.BigDecimal;

/**
 * @author Forest Wang
 * @package com.okx.trading.infrastructure.okx.entity.rest.trading.grid
 * @class AddGridInvestmentReq
 * @email forestwanglin@gmail.cn
 * @date 2024/7/25
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddGridInvestmentReq implements IOkxRestReq {

    /**
     * 策略订单ID
     */
    @NonNull
    @JSONField(name = "algoId")
    @JsonProperty("algoId")
    private String algoId;

    /**
     * 加仓数量
     */
    @NonNull
    @JSONField(name = "amt")
    @JsonProperty("amt")
    private BigDecimal amt;

}
