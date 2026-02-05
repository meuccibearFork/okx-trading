package com.okx.trading.config;

import com.okex.open.api.config.APIConfiguration;
import com.okex.open.api.enums.I18nEnum;
import com.okex.open.api.service.marketData.MarketDataAPIService;
import com.okex.open.api.service.marketData.impl.MarketDataAPIServiceImpl;
import com.okex.open.api.service.publicData.PublicDataAPIService;
import com.okex.open.api.service.publicData.impl.PublicDataAPIServiceImpl;
import com.okex.open.api.service.trade.TradeAPIService;
import com.okex.open.api.service.trade.TradingService;
import com.okex.open.api.service.trade.impl.OkxRealTradingService;
import com.okex.open.api.service.trade.impl.TradeAPIServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/1/25 02:08
 */
@Configuration
public class OkxClientConfig {

    @Resource
    OkxApiConfig okxApiConfig;

    private APIConfiguration config;

    @Bean
    public TradingService realTradingService(){
        return new OkxRealTradingService(convertConfiguration());
    }

    /**
     * 贸易API服务
     */
    @Bean
    public TradeAPIService tradeAPIService(){
        return new TradeAPIServiceImpl(convertConfiguration());
    }

    /**
     * 市场数据
     */
    @Bean
    public MarketDataAPIService marketDataAPIService() {
        return new MarketDataAPIServiceImpl(convertConfiguration());
    }

    /**
     * 公共数据
     */
    @Bean
    public PublicDataAPIService publicDataAPIService() {
        return new PublicDataAPIServiceImpl(convertConfiguration());
    }

    /**
     * 转换配置
     * @return 配置项
     */
    private APIConfiguration convertConfiguration(){
        if(config == null){
            config = new APIConfiguration();

            //传入https://www.okx.com 或 https://aws.okx.com
            //you can set the domain as https://www.okx.com or https://aws.okx.com
            config.setDomain(okxApiConfig.getBaseUrl());

            config.setApiKey(okxApiConfig.getApiKey());
            config.setSecretKey(okxApiConfig.getSecretKey());
            config.setPassphrase(okxApiConfig.getPassphrase());

            //请求模拟盘的接口需要传入1，否则传入0
            //if you want to request the endpoint in demo trading,please input 1,otherwise,please input 0
            config.setXSimulatedTrading(okxApiConfig.isUseMockData() ? "1" : "0");

            config.setPrint(true);
            /* config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);*/
            config.setI18n(I18nEnum.ENGLISH);
        }

        return config;
    }
}
