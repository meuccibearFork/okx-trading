package com.okx.trading;

import com.alibaba.fastjson.JSON;
import com.okex.open.api.config.APIConfiguration;
import com.okex.open.api.enums.I18nEnum;
import org.slf4j.Logger;

public class BaseTests {

    public APIConfiguration config;

    public APIConfiguration config() {
        APIConfiguration config = new APIConfiguration();

        //传入https://www.okx.com 或 https://aws.okx.com
        //you can set the domain as https://www.okx.com or https://aws.okx.com
        config.setDomain("https://www.okx.com");

//        apiKey = "";
//        passphrase = ".";
//        secretKey = "";

//        apiKey = "e43e305b-8021-4ff1-a97d-ed3e16357eeb";
//        passphrase = "12345678Abc.";
//        secretKey = "AADF82335968EFCDE116191B4B97D430";
// Ta
//        config.setApiKey("1c755cb1-02fa-400e-bf4a-78b71cbd083f");
//        config.setSecretKey("18690BD7007D9278B5BCA01C9A7F095B");
//        config.setPassphrase("12345678Abc.");

//        config.setApiKey("b81e589c-864a-4401-bedf-819608d0c51f");
//        config.setSecretKey("F4B5AD175F7AE9457048ED8E39BDB91D");
//        config.setPassphrase("9WVku96XdasrC9u.");
//        config.setXSimulatedTrading("0");

        config.setApiKey("c2af6981-07ca-4dc1-8996-8ef4553a310f");
        config.setSecretKey("9643C5511388B03F1BEF06115B709F30");
        config.setPassphrase(".9WVku96XdasrC9u");
        config.setXSimulatedTrading("1");

        //请求模拟盘的接口需要传入1，否则传入0
        //if you want to request the endpoint in demo trading,please input 1,otherwise,please input 0
//        config.setXSimulatedTrading("0");


        config.setPrint(true);
        config.setI18n(I18nEnum.ENGLISH);

        return config;
    }

    public void toResultString(Logger log, String flag, Object object) {
        StringBuilder su = new StringBuilder();
        su.append("\n").append("=====>").append(flag).append(":\n").append(JSON.toJSONString(object));
        log.info(su.toString());
    }
}
