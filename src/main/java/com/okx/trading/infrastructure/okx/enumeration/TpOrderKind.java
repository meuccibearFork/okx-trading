package com.okx.trading.infrastructure.okx.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum TpOrderKind {

    CONDITION("condition"),
    LIMIT("limit");

    private final String value;

    TpOrderKind(final String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

}
