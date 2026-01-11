package com.okx.trading.demo;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.POST;

public interface WeDocApiService {
    /**
     * 发送第一个POST请求（无请求体）
     * 使用完整的URL覆盖了Retrofit.Builder中设置的BASE_URL
     * @return 返回响应字符串（具体类型应根据实际API返回的JSON结构定义DTO类）
     */
    @POST
    Call<JsonObject> postAnswerPage1(@retrofit2.http.Url String url);

    // 第二个请求的定义方式完全相同
    // @POST
    // Call<String> postAnswerPage2(@retrofit2.http.Url String url);
}
