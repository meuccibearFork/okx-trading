package com.okx.trading.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * API通用响应包装类
 * @param <T> 数据类型
 */
@Setter
@Getter
public class ApiResponse<T> {
    private String code;

    private String msg;

    private List<T> data;

    // 构造方法
    public ApiResponse() {}

    public ApiResponse(String code, String msg, List<T> data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 判断请求是否成功
    public boolean isSuccess() {
        return "0".equals(code);
    }

    // 获取第一个数据（如果有）
    public Optional<T> getFirstData() {
        return data != null && !data.isEmpty() ? Optional.of(data.get(0)) : Optional.empty();
    }

    @Override
    public String toString() {
        return "ApiResponse{code='" + code + "', msg='" + msg + "', data=" + data + "}";
    }
}
