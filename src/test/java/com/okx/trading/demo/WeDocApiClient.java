package com.okx.trading.demo;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WeDocApiClient {

    public static final String BASE_URL = "https://doc.weixin.qq.com/"; // 基础URL，实际请求会使用完整URL
    public static final String TARGET_URL_1 = "formcol/answer_page?f=json&flow_journaluuids=6T6FxibuV9ficaQdPnwZJHTFWZA5XDTLpWoipQChXUe8rTaaog73MJGsiCKkpPbxJj&form_id=AA8A0QccAAgALoArAbsAAYUlY38EA8T8j_base&sid=1uZEZYwUV3QukTN0ALsyagAA&type=11&wedoc_xsrf=1";
    // 您提供了两个URL，此处以第一个为例。第二个URL的构建方式完全相同。

    public static OkHttpClient buildOkHttpClient() {
        // 创建一个日志拦截器，用于在控制台输出请求和响应信息（开发阶段非常有用）
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别为BODY，可看到头部和体

        // 创建自定义的请求头拦截器
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 获取原始请求
                Request originalRequest = chain.request();

                // 构建新的请求，并添加所有必需的头部
                Request newRequest = originalRequest.newBuilder()
                        .header("accept", "application/json, text/plain, */*")
                        .header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .header("priority", "u=1, i")
                        .header("sec-ch-ua", "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"macOS\"")
                        .header("sec-fetch-dest", "empty")
                        .header("sec-fetch-mode", "cors")
                        .header("sec-fetch-site", "same-origin")
                        // 注意：Cookie是高度敏感且具有时效性的信息。此处直接使用您提供的字符串。
                        // 在实际生产环境中，应考虑从安全的存储中动态获取或通过登录流程维护。
                        .header("cookie", "fingerprint=fc891b0d14dd471799981a02e0fae99441; low_login_enable=1; fingerprint=rcnuccgbcurwmmvab2i6dt1yfspxr4uczcmxmjvhdn6uw63pcgpto; docMessageCenterCookie=; markHashId_L=c92f4118-d661-45bd-9bf0-c51a420d9813; RK=Ld8Ru2POkn; ptcz=721c63611e053345a7e2f06af2cd00b174ff3b0d7e0e6918533fd3f9fc2ca69b; backup_cdn_domain=res.wx.qq.com; ptui_loginuin=767140550; tdoc_uid=13102702826427622; wedoc_openid=wozbKqDgAA_3G705emXPW4qZnad0joKg; _clck=fv4ljf|1|g1x|0; window_width=1920; optimal_cdn_domain=rescdn.qqmail.com; wedoc_sid=1uZEZYwUV3QukTN0ALsyagAA; wedoc_sids=13102702826427622&1uZEZYwUV3QukTN0ALsyagAA; wedoc_skey=13102702826427622&15ff0bd17cdfc3f218ff8741506d75ba; wedoc_ticket=13102702826427622&CAESIEFBbGFxCiReMX9gYobiw-GK5d70HM0M6r2IdN6PXlbk; TOK=4f93a63a93e5b3e0; traceid=4f93a63a93; hashkey=4f93a63a")
                        .method(originalRequest.method(), originalRequest.body())
                        .build();
                // 继续执行请求
                return chain.proceed(newRequest);
            }
        };

        // 构建并配置OkHttpClient
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 连接超时设置
                .readTimeout(30, TimeUnit.SECONDS)    // 读取超时设置
                .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时设置
                .addInterceptor(headerInterceptor);   // 添加自定义头部拦截器

        // 仅在开发调试时添加日志拦截器，生产环境应移除
        clientBuilder.addInterceptor(loggingInterceptor);

        return clientBuilder.build();
    }
}
