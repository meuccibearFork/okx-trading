package com.okx.trading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 保证金模式枚举
 */
@Getter
public enum MgnMode {
    @JsonProperty("isolated")
    ISOLATED("逐仓"),

    @JsonProperty("cross")
    CROSS("全仓");

    private final String description;

    MgnMode(String description) {
        this.description = description;
    }

}
