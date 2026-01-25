package com.okx.trading.strategy.cust;

import com.okx.trading.constant.PositionType;
import com.okx.trading.constant.TradingSignal;
import com.okx.trading.strategy.AvgIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.num.Num;

/**
 * 自定义-策略工厂
 */
@Slf4j
@Component
public class CustomizeStrategyFactory {

    public final HeimaStrategyBuilder strategyBuilder;

    public CustomizeStrategyFactory(HeimaStrategyBuilder strategyBuilder) {
        this.strategyBuilder = strategyBuilder;
    }

    //TODO [aaaa](https://chat.deepseek.com/a/chat/s/d003ca08-71eb-4942-a5b2-46e34d2dd387)
    //TODO https://chat.deepseek.com/a/chat/s/4cf059ed-1da0-4e47-92a6-24c830ea2c74
    //TODO https://chat.deepseek.com/a/chat/s/201ff8f5-a0f8-4e52-b53a-d1491e4c3c5b
    public Strategy yjwStrategy(BarSeries series) {


        int endIndex = series.getEndIndex();
        Bar currentBar = series.getBar(endIndex);
        currentBar.getEndTime();

        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
        HighPriceIndicator highPrice = new HighPriceIndicator(series);
        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        AvgIndicator bop = new AvgIndicator(openPrice, highPrice, lowPrice, closePrice, series);

        Num currentAvg = bop.calculate(0);
        Num previousAvg = bop.calculate(1);

        TradingSignal signal;
        if (currentAvg.isGreaterThan(previousAvg)) {
            signal = TradingSignal.LONG;
        } else if (currentAvg.isLessThan(previousAvg)) {
            signal = TradingSignal.SHORT;
        }

        // 使用策略构建器
//        return strategyBuilder.buildHeimaStrategy(series, higherTimeframeSeries);


//        // 检查当前持仓
//        if (currentPosition.getType() == PositionType.NONE) {
//            // 无持仓，检查开仓信号
//            handleEntrySignal(signal, currentFourHourBar, previousFourHourBar);
//        } else {
//            // 有持仓，检查退出条件
//            handlePositionManagement(newBar);
//        }

        return null;
    }










}
