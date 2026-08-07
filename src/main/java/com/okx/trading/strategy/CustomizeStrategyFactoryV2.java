package com.okx.trading.strategy;

import com.okx.trading.constant.log.LoggerName;
import com.okx.trading.model.entity.RealTimeStrategyEntity;
import com.okx.trading.model.market.Candlestick;
import com.okx.trading.service.HistoricalDataService;
import com.okx.trading.strategy.cust.CustomizeStrategyFactory;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/8/7 20:51
 */
@Service
public class CustomizeStrategyFactoryV2 {

    private static final Logger strategyLogger = LoggerFactory.getLogger(LoggerName.WSS_STRATEGY_MSG);

    @Resource
    private CustomizeStrategyFactory customizeStrategyFactory;

    @Resource
    private HistoricalDataService historicalDataService;

    /**
     * 处理策略信号
     * 真正执行实时策略逻辑，判断买卖信号的地方
     */
    public void processStrategySignal(long time, RealTimeStrategyEntity state, Candlestick candlestick, BarSeries series) {
        strategyLogger.info("[数据]K线数据 {}", candlestick);

        //同一策略同周期内不能重复交易，买、卖只能触发一次，防止短时间都满足多次交易的情况
        synchronized (state) {

            long intervalSeconds = historicalDataService.getIntervalMinutes(candlestick.getIntervalVal()) * 60;
            customizeStrategyFactory.stopLoss(time, series, state, candlestick);
            boolean signalOfSamePeriod = false;
            if (state.getLastTradeTime() != null) {
                LocalDateTime lastTradeTime = state.getLastTradeTime();
                //同周期只触发一次交易信号
                signalOfSamePeriod = lastTradeTime.isAfter(candlestick.getOpenTime()) && lastTradeTime.isBefore(candlestick.getOpenTime().plusSeconds(intervalSeconds));
            }
            strategyLogger.info("processStrategySignal forbiddenTradeTime:{} signalOfSamePeriod:{}", Duration.between(candlestick.getOpenTime().plusSeconds(historicalDataService.getIntervalMinutes(candlestick.getIntervalVal()) * 60), LocalDateTime.now()).abs().get(ChronoUnit.SECONDS) > 15, signalOfSamePeriod);

            // 检查交易信号
            if (1 == candlestick.getState()) {
                customizeStrategyFactory.yjwStrategy(time, series, state, candlestick);
            }
        }
    }

}
