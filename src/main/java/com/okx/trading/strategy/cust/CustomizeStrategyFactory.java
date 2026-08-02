package com.okx.trading.strategy.cust;

import cn.hutool.core.util.ObjectUtil;
import com.okex.open.api.bean.account.result.PositionDetail;
import com.okex.open.api.bean.account.result.tracker.DynamicStopLossTracker;
import com.okex.open.api.bean.account.result.tracker.TradeStatistics;
import com.okex.open.api.bean.calculator.PositionCalculationResult;
import com.okex.open.api.calculator.OKXProfitCalculator;
import com.okex.open.api.service.trade.TradingService;
import com.okx.trading.constant.log.LoggerName;
import com.okx.trading.model.entity.RealTimeStrategyEntity;
import com.okx.trading.model.market.Candlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 自定义-策略工厂 - 高级策略集合
 */
@Service
public class CustomizeStrategyFactory {

    private static final Logger log = LoggerFactory.getLogger(LoggerName.WSS_MSG);
    private static final Logger strategyLogger = LoggerFactory.getLogger(LoggerName.WSS_STRATEGY_MSG);

    private final TradingService tradingService;

    PositionDetail positionDetail;

    @Value("${strategy.yjw.tp:}")
    private double triggerPoints = 2.0;

    private final int leverage = 3;

    public CustomizeStrategyFactory(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    public Strategy yjwStrategy(BarSeries barSeries, RealTimeStrategyEntity state, Candlestick candlestick) {
        try {
            // 获取最新的Bar
            int endIndex = barSeries.getEndIndex();
            Bar currentBar = barSeries.getBar(endIndex);
            Bar previousBar = barSeries.getBar(endIndex - 1);

            // 检查是否是新K线
            boolean isNewBar = !currentBar.getEndTime().equals(previousBar.getEndTime());
            strategyLogger.info("[数据]Current Bar: isNewBar:{} endIndex:{} currentBar:{} previousBar:{}", isNewBar, endIndex, currentBar, previousBar);

            if (isNewBar) {
                // 计算信号
                Signal signal = calculateSignal(currentBar, previousBar);

                init(state.getSymbolSwap());
                // 无持仓时开仓
                if (positionDetail == null) {
                    if (signal.isLongSignal()) {
                        openLongPosition(currentBar, previousBar, state, candlestick);
                    } else if (signal.isShortSignal()) {
                        openShortPosition(currentBar, previousBar, state, candlestick);
                    } else {
                        strategyLogger.info("[数据][计算不出信号操作] 品种: {}, 时间: {}", state.getSymbolSwap(), currentBar.getEndTime());
                    }
                }
            }
        } catch (Exception e) {
            //2026-08-02 22:30:00.937 [OkHttp https://ws.okx.com:8443/...] INFO  wss.strategy.msg - [数据]K线数据 1785680940000 MEME-USDT-SWAP(2026-08-02T22:29-candle1m) 开盘/收盘价:0.00050530/0.00050530 最高/最低价:0.00050530/0.00050530 reconnectType:BUSINESS state:1 成交量(计价货币/以币为单位/以张为单位):0.00000000 / 0.00000000 / 0.00000000
            //2026-08-02 22:30:01.339 [OkHttp https://ws.okx.com:8443/...] INFO  wss.strategy.msg - [数据]Current Bar: isNewBar:true endIndex:102 currentBar:{end time: 2026-08-02T14:30:00Z, close price: 0.0005053, open price: 0.0005053, low price: 0.0005053 high price: 0.0005053, volume:      0} previousBar:{end time: 2026-08-02T14:29:00Z, close price: 0.0005053, open price: 0.0005053, low price: 0.0005053 high price: 0.0005053, volume:    365}
            strategyLogger.error("处理K线数据失败", e);
        }

        return null;
    }

    DynamicStopLossTracker tracker;

    void init(String symbolSwap) {
        if (ObjectUtil.isEmpty(positionDetail)) {
            final var positions = tradingService.getPositions(symbolSwap);
            positionDetail = positions.getPositionDetailOne(symbolSwap);
        }
    }

    public void stopLoss(BarSeries barSeries, RealTimeStrategyEntity state, Candlestick candlestick) {
        //strategyLogger.info("\t[检测] barSeries:{} state:{} candlestick:{}", barSeries, state, candlestick);
        // 获取最新的Bar
        Bar currentBar = barSeries.getBar(barSeries.getEndIndex());

        init(state.getSymbolSwap());
        if (positionDetail == null) {
            return;
        }

        positionDetail.setMarkPx(currentBar.getClosePrice().bigDecimalValue());

        if (ObjectUtil.isEmpty(tracker)) {
            // 使用Builder模式创建追踪器
            tracker = DynamicStopLossTracker.builder()
                    .entryPrice(BigDecimal.valueOf(100))
                    .initialStopLossPercent(BigDecimal.valueOf(-triggerPoints))
                    .incrementPercent(BigDecimal.valueOf(triggerPoints))
                    .build();
        }

        PositionCalculationResult positionCalculationResult = OKXProfitCalculator.calculateAll(tracker, positionDetail);

        // 每次更新后显示状态
        strategyLogger.info(positionCalculationResult.printSummary("\t"));

        if (tracker.isStopLossTriggered()) {
            strategyLogger.info("\n⚠️ 止损已被触发！交易结束。");

            tracker.logStatus();
            // 显示调整历史
            tracker.logAdjustmentHistory();

            closePosition("止损触发", currentBar.getClosePrice(), state, candlestick, positionDetail);
            tracker = null;
            positionDetail = null;
            // 显示最终统计
            TradeStatistics stats = tracker.getStatistics();
            strategyLogger.info("""
                            
                            ┌─────────────────────────────────────────────────────┐
                            │                   最终统计                          │
                            ├─────────────────────────────────────────────────────┤
                            │ 最终状态: {}
                            │ 入场价格: {}
                            │ 最终价格: {}
                            │ 最终收益率: {}%
                            │ 最高收益率: {}%
                            │ 总调整次数: {}
                            │ 总运行时间: {}秒
                            │ 最终盈亏比: {}
                            └─────────────────────────────────────────────────────┘""",
                    stats.getStatus().getDescription(),
                    stats.getEntryPrice().setScale(2, RoundingMode.HALF_UP),
                    stats.getCurrentPrice().setScale(2, RoundingMode.HALF_UP),
                    stats.getCurrentReturn().setScale(2, RoundingMode.HALF_UP),
                    stats.getMaxReturnAchieved().setScale(2, RoundingMode.HALF_UP),
                    stats.getAdjustmentCount(),
                    java.time.Duration.between(stats.getStartTime(), stats.getLastUpdateTime()).getSeconds(),
                    stats.getRiskRewardRatio().setScale(2, RoundingMode.HALF_UP)
            );
        }

    }

    /**
     * 计算交易信号
     */
    private Signal calculateSignal(Bar currentBar, Bar previousBar) {
        // 计算平均价
        Num avg4 = calculateAveragePrice(currentBar);
        Num avg4Prev = calculateAveragePrice(previousBar);

        // 生成信号
        boolean longSignal = avg4.isGreaterThan(avg4Prev);
        boolean shortSignal = avg4.isLessThan(avg4Prev);

        return Signal.builder()
                .longSignal(longSignal)
                .shortSignal(shortSignal)
                .avgPrice(avg4)
                .avgPricePrev(avg4Prev)
                .build();
    }

    /**
     * 计算平均价
     */
    private Num calculateAveragePrice(Bar bar) {
        if (bar == null) return DecimalNum.valueOf(0);

        return bar.getOpenPrice()
                .plus(bar.getHighPrice())
                .plus(bar.getLowPrice())
                .plus(bar.getClosePrice())
                .dividedBy(DecimalNum.valueOf(4));
    }

    /**
     * 开多仓
     */
//    private void openLongPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
//        Num entryPrice = currentBar.getClosePrice();
//        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 + triggerPoints / 100)));
//        strategyLogger.info("[操作][开多仓] 入场价: {}, 止损价: {}, 时间: {} symbol:{}", entryPrice, stopLoss, currentBar.getEndTime(), state.getSymbolSwap());
//    }
    private void openLongPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        Num entryPrice = currentBar.getClosePrice();
        double percent = triggerPoints / 100.0;  // 0.02

        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 - percent)));
        Num takeProfit = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 + percent)));

        // 计算账户权益风险：价格波动% × 杠杆
        double equityRisk = triggerPoints * leverage;  // 2.0 * 3 = 6.0%

        strategyLogger.info(
                "[操作][开多仓] 品种: {}, 时间: {}, 入场: {}, 止损: {}(-{}%), 止盈: {}(+{}%), 杠杆: {}x, 账户风险: {}%",
                state.getSymbolSwap(),
                currentBar.getEndTime(),
                entryPrice,
                stopLoss,
                triggerPoints,          // 价格波动 2.0%
                takeProfit,
                triggerPoints,          // 价格波动 2.0%
                leverage,               // 3x
                equityRisk              // 6.0%（杠杆放大后的风险）
        );

        //tradingService.tradeByUsdtValue(state.getSymbol(), BUY_OPEN_LONG_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);
    }

    /**
     * 开空仓
     */
//    private void openShortPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
//        Num entryPrice = currentBar.getClosePrice();
//        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 - triggerPoints / 100)));
//        strategyLogger.info("[操作][开空仓] 入场价: {}, 止损价: {}, 时间: {} symbol:{}", entryPrice, stopLoss, currentBar.getEndTime(), state.getSymbolSwap());
//    }
    private void openShortPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        Num entryPrice = currentBar.getClosePrice();
        double percent = triggerPoints / 100.0;  // 0.02

        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 + percent)));   // 止损在上（正确）
        Num takeProfit = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 - percent))); // 止盈在下

        // 计算账户权益风险：价格波动% × 杠杆
        double equityRisk = triggerPoints * leverage;  // 2.0 * 3 = 6.0%

        strategyLogger.info(
                "[操作][开空仓] 品种: {}, 时间: {}, 入场: {}, 止损: {}(+{}%), 止盈: {}(-{}%), 杠杆: {}x, 账户风险: {}%",
                state.getSymbolSwap(),
                currentBar.getEndTime(),
                entryPrice,
                stopLoss,
                triggerPoints,          // 价格波动 2.0%
                takeProfit,
                triggerPoints,          // 价格波动 2.0%
                leverage,               // 3x
                equityRisk              // 6.0%（杠杆放大后的风险）
        );
        //tradingService.tradeByUsdtValue(state.getSymbolSwap(), SELL_OPEN_SHORT_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);

    }

    /**
     * 平仓
     */
    private void closePosition(String reason, Num exitPrice, RealTimeStrategyEntity state, Candlestick candlestick, PositionDetail positionDetail) {
        strategyLogger.info("[操作][开空仓] reason:{} 止损价: {} symbol:{} candlestick:{} positionDetail:{}", reason, exitPrice, state.getSymbolSwap(), candlestick, positionDetail);
        //tradingService.closePosition(state.getSymbolSwap(), PositionSide.SHORT == positionDetail.getPosSide() ? BUY_CLOSE_SHORT_ISOLATED : SELL_CLOSE_LONG_ISOLATED);
    }

}
