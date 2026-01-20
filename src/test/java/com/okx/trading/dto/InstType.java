package com.okx.trading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 产品类型枚举
 */
@Getter
public enum InstType {
    @JsonProperty("SPOT")
    SPOT("现货"),

    @JsonProperty("MARGIN")
    MARGIN("杠杆"),

    @JsonProperty("SWAP")
    SWAP("永续合约"),

    @JsonProperty("FUTURES")
    FUTURES("交割合约"),

    @JsonProperty("OPTION")
    OPTION("期权");

    private final String description;

    InstType(String description) {
        this.description = description;
    }

}

