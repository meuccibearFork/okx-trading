package com.okx.trading.strategy.cust;

import cn.hutool.core.util.ObjectUtil;
import com.okex.open.api.bean.account.result.PositionDetail;
import com.okex.open.api.bean.account.result.tracker.DynamicStopLossTracker;
import com.okex.open.api.bean.account.result.tracker.TradeStatistics;
import com.okex.open.api.bean.calculator.PositionCalculationResult;
import com.okex.open.api.calculator.OKXProfitCalculator;
import com.okex.open.api.constant.PositionSide;
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

import static com.okex.open.api.constant.OkxTradeType.*;

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
            log.info("<TEST>Current Bar: isNewBar:{} endIndex:{} currentBar:{} previousBar:{}", isNewBar, endIndex, currentBar, previousBar);

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
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理K线数据失败", e);
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
            log.info("\n⚠️ 止损已被触发！交易结束。");

            tracker.logStatus();
            // 显示调整历史
            tracker.logAdjustmentHistory();

            closePosition("止损触发", currentBar.getClosePrice(), state, candlestick, positionDetail);
            tracker = null;
            positionDetail = null;
            // 显示最终统计
            TradeStatistics stats = tracker.getStatistics();
            log.info("""
                            
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
    private void openLongPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        Num entryPrice = currentBar.getClosePrice();
        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 + triggerPoints / 100)));
        log.info("<TEST>【开多仓】 入场价: {}, 止损价: {}, 时间: {} symbol:{}", entryPrice, stopLoss, currentBar.getEndTime(), state.getSymbolSwap());
        tradingService.tradeByUsdtValue(state.getSymbol(), BUY_OPEN_LONG_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);
    }

    /**
     * 开空仓
     */
    private void openShortPosition(Bar currentBar, Bar previousBar, RealTimeStrategyEntity state, Candlestick candlestick) {
        Num entryPrice = currentBar.getClosePrice();
        Num stopLoss = DecimalNum.valueOf(entryPrice.bigDecimalValue().multiply(BigDecimal.valueOf(1 - triggerPoints / 100)));
        log.info("<TEST>【开空仓】 入场价: {}, 止损价: {}, 时间: {} symbol:{}", entryPrice, stopLoss, currentBar.getEndTime(), state.getSymbolSwap());
        tradingService.tradeByUsdtValue(state.getSymbolSwap(), SELL_OPEN_SHORT_ISOLATED, BigDecimal.valueOf(state.getTradeAmount()), "market", leverage);
    }

    /**
     * 平仓
     */
    private void closePosition(String reason, Num exitPrice, RealTimeStrategyEntity state, Candlestick candlestick, PositionDetail positionDetail) {
        tradingService.closePosition(state.getSymbolSwap(), PositionSide.SHORT == positionDetail.getPosSide() ? BUY_CLOSE_SHORT_ISOLATED : SELL_CLOSE_LONG_ISOLATED);
    }

}
