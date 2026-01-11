package com.okx.trading.demo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // 1. 构建自定义的OkHttpClient
        OkHttpClient okHttpClient = WeDocApiClient.buildOkHttpClient();

        // 2. 构建Retrofit实例，配置基础URL、客户端和转换器
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WeDocApiClient.BASE_URL) // 此处基础URL仍需要设置，但会被方法中的@Url覆盖
                .client(okHttpClient) // 注入我们配置好的OkHttpClient
                .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换器以处理JSON响应
                .build();

        // 3. 创建API服务接口的实例
        WeDocApiService apiService = retrofit.create(WeDocApiService.class);

        // 4. 准备目标URL（即您提供的第一个fetch地址）
        String targetUrl = WeDocApiClient.TARGET_URL_1;

        // 5. 发起异步调用（这里展示阻塞式的同步调用，实际Android开发中应使用enqueue进行异步调用）
        try {
            Call<JsonObject> call = apiService.postAnswerPage1(targetUrl);
            retrofit2.Response<JsonObject> response = call.execute(); // 同步执行
            if (response.isSuccessful() && response.body() != null) {
                JsonObject responseBody = response.body();
                System.out.println("请求成功！");
                System.out.println("响应体: " + responseBody);
                // 此处可以将responseBody解析为具体的Java对象
            } else {
                System.err.println("请求失败！错误码: " + response.code());
                System.err.println("错误信息: " + response.errorBody() != null ? response.errorBody().string() : "Unknown");
            }
        } catch (IOException e) {
            System.err.println("网络请求发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
