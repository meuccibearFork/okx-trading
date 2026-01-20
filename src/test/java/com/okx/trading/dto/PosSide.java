package com.okx.trading.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 持仓方向枚举
 */
@Getter
public enum PosSide {
    @JsonProperty("long")
    LONG("多仓"),

    @JsonProperty("short")
    SHORT("空仓"),

    @JsonProperty("net")
    NET("净持仓");

    private final String description;

    PosSide(String description) {
        this.description = description;
    }

}
