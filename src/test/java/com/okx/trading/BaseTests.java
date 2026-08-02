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

        config.setApiKey("8cd3dc15-f58e-44b1-96e8-8764364ba81f");
        config.setSecretKey("6119C3AA4FC26E9F037E979D5328E85D");
        config.setPassphrase("123456Abc.");
        config.setXSimulatedTrading("0");

//        config.setApiKey("c2af6981-07ca-4dc1-8996-8ef4553a310f");
//        config.setSecretKey("9643C5511388B03F1BEF06115B709F30");
//        config.setPassphrase(".9WVku96XdasrC9u");
//        config.setXSimulatedTrading("1");

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
