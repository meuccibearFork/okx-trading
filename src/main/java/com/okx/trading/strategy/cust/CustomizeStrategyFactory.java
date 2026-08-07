package com.okx.trading.strategy.cust;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.okex.open.api.component.calculator.dto.PositionCalculationResult;
import com.okex.open.api.component.calculator.dto.PositionDetail;
import com.okex.open.api.component.constant.PositionSide;
import com.okex.open.api.component.tracker.DynamicStopLossTracker;
import com.okex.open.api.component.tracker.TradeStatistics;
import com.okex.open.api.service.trading.TradingService;
import com.okx.trading.constant.log.LoggerName;
import com.okx.trading.model.entity.RealTimeStrategyEntity;
import com.okx.trading.model.market.Candlestick;
import com.okx.trading.service.RedisCacheService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.okex.open.api.component.constant.OkxTradeType.*;


/**
 * 自定义-策略工厂 - 高级策略集合
 */
@Service
public class CustomizeStrategyFactory {

    private static final Logger log = LoggerFactory.getLogger(CustomizeStrategyFactory.class);
    private static final Logger strategyLogger = LoggerFactory.getLogger(LoggerName.WSS_STRATEGY_MSG);

    private final TradingService tradingService;

    PositionDetail positionDetail;

    @Value("${strategy.yjw.initialStopLoss:}")
    private double triggerPoints;

    @Value("${strategy.yjw.incrementPercent:}")
    private double incrementPercent;

    @Resource
    private RedisCacheService redisCacheService;

    private final int leverage = 3;

    public CustomizeStrategyFactory(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    public Strategy yjwStrategy(long time, BarSeries barSeries, RealTimeStrategyEntity state, Candlestick candlestick) {
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
                positionDetail = mockhandlerPositionDetail("yjwStrategy.positionDetail", time, positionDetail);

                // 无持仓时开仓
                if (positionDetail == null) {
                    if (signal.isLongSignal()) {
                        openLongPosition(currentBar, previousBar, state, candlestick);
                        mockhandler("openLongPosition", time);
                    } else if (signal.isShortSignal()) {
                        openShortPosition(currentBar, previousBar, state, candlestick);
                        mockhandler("openShortPosition", time);
                    } else {
                        mockhandler("shortPosition", time);
                        strategyLogger.info("[数据][计算不出信号] 品种: {}, 时间: {}", state.getSymbolSwap(), currentBar.getEndTime());
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
    String isTrackerClose = "isTrackerClose";

    void init(String symbolSwap) {
        if (ObjectUtil.isEmpty(positionDetail)) {
            final var positions = tradingService.getPositions(symbolSwap);
            positionDetail = positions.getPositionDetailOne(symbolSwap);
        }
    }

    public void stopLoss(long time, BarSeries barSeries, RealTimeStrategyEntity state, Candlestick candlestick) {
        // 获取最新的Bar
        Bar currentBar = barSeries.getBar(barSeries.getEndIndex());

        init(state.getSymbolSwap());
        positionDetail = mockhandlerPositionDetail("stopLoss.positionDetail", time, positionDetail);

        if (positionDetail == null || redisCacheService.hasKey(isTrackerClose)) {
            return;
        }

        positionDetail.setMarkPx(currentBar.getClosePrice().bigDecimalValue());

        if (ObjectUtil.isEmpty(tracker)) {
            // 使用Builder模式创建追踪器
            tracker = DynamicStopLossTracker.builder()
                    .entryPrice(BigDecimal.valueOf(100))
                    .initialStopLossPercent(BigDecimal.valueOf(-triggerPoints))
                    .incrementPercent(BigDecimal.valueOf(incrementPercent))
                    .build();

            PositionCalculationResult positionCalculationResult = positionDetail.calculateAll();
            strategyLogger.info(positionCalculationResult.printSummary("\t"));
        }

        PositionCalculationResult positionCalculationResult = positionDetail.calculateAll();

        //TODO 记录
        BigDecimal bigDecimal = positionCalculationResult.getPriceChangePercent().divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                .multiply(positionCalculationResult.getLeverage()).multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
        mockhandlerBigDecimal("percentages1", time, bigDecimal);

        //TODO 记录
        BigDecimal newData = positionCalculationResult.getProfitPercentage().add(BigDecimal.valueOf(100));
        mockhandlerBigDecimal("percentages", time, newData);
        tracker.updateData(newData);
        tracker.logStatus();

        if (tracker.isStopLossTriggered()) {
            mockhandlerBigDecimal("stop", time, bigDecimal);

            strategyLogger.info(positionCalculationResult.printSummary("\t"));

            strategyLogger.info("\n⚠️ 止损已被触发！交易结束。");

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

            redisCacheService.set(isTrackerClose, null, 60, TimeUnit.MINUTES);
        }

    }


    /**
     * 记录
     */
    public void savePercentage(String fileName, String formatted) {
        String filePath = System.getProperty("user.dir") + "/" + fileName + ".txt";

        // 追加一行（Hutool 自动处理换行和文件创建）
        FileUtil.appendLines(Collections.singletonList(formatted), filePath, StandardCharsets.UTF_8);
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
        if (bar == null) {
            return DecimalNum.valueOf(0);
        }

        return bar.getOpenPrice()
                .plus(bar.getHighPrice())
                .plus(bar.getLowPrice())
                .plus(bar.getClosePrice())
                .dividedBy(DecimalNum.valueOf(4));
    }

    /**
     * 开多仓
     */
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

        tradingService.tradeByUsdtValue(state.getSymbol(), BUY_OPEN_LONG_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);
    }

    /**
     * 开空仓
     */
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
        tradingService.tradeByUsdtValue(state.getSymbolSwap(), SELL_OPEN_SHORT_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);

    }

    /**
     * 平仓
     */
    private void closePosition(String reason, Num exitPrice, RealTimeStrategyEntity state, Candlestick candlestick, PositionDetail positionDetail) {
        strategyLogger.info("[操作][开空仓] reason:{} 止损价: {} symbol:{} candlestick:{} positionDetail:{}", reason, exitPrice, state.getSymbolSwap(), candlestick, positionDetail);
        tradingService.closePosition(state.getSymbolSwap(), PositionSide.SHORT == positionDetail.getPosSide() ? BUY_CLOSE_SHORT_ISOLATED : SELL_CLOSE_LONG_ISOLATED);
    }

    boolean isMock = false;
    long timeoutMinutes = 60 * 24 * 3;


    private BigDecimal mockhandlerBigDecimal(String key, long time, BigDecimal bigDecimal) {
        String format = String.format(key + "_%d", time);

        if (isMock) {
            bigDecimal = redisCacheService.getCache(format, BigDecimal.class);
            switch (key) {
                case "stop":
                    log.info("[历史]止损已被触发{}", bigDecimal);
                    break;
                case "percentages":
                    log.info("[历史]percentages{}", NumberUtil.decimalFormat("#.######", bigDecimal.doubleValue()));
                    break;
                case "percentages1":
                    log.info("[历史]percentages1{}", NumberUtil.decimalFormat("#.######", bigDecimal.doubleValue()));
                    break;
            }
            return bigDecimal;
        } else {
            redisCacheService.setCache(format, bigDecimal.toString(), timeoutMinutes);
        }
        return bigDecimal;
    }

    private PositionDetail mockhandlerPositionDetail(String key, long time, PositionDetail positionDetail) {
        if (null == positionDetail) {
            return null;
        }

        String format = String.format(key + "_%d", time);
        if (isMock) {
            if (redisCacheService.hasKey(format)) {
                return redisCacheService.getCache(format, PositionDetail.class);
            }
            return null;
        } else {
            redisCacheService.setCache(format, JSONUtil.toJsonStr(positionDetail), timeoutMinutes);
        }
        return positionDetail;
    }

    void mockhandler(String key, long time) {
        String format = String.format(key + "_%d", time);

        if (isMock) {
            Boolean b = redisCacheService.hasKey(format);
            if (b) {
                switch (key) {
                    case "openLongPosition":
                        log.info("开多仓");
                        break;
                    case "openShortPosition":
                        log.info("开空仓");
                        break;
                    case "shortPosition":
                        log.info("无持仓");
                        break;
                }
            }

        } else {
            redisCacheService.setCache(format, true, timeoutMinutes);
        }
    }

}
