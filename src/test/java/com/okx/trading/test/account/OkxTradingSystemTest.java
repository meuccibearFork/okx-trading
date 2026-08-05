package com.okx.trading.test.account;

import com.okex.open.api.bean.account.result.AccountInfo;
import com.okex.open.api.bean.trade.result.TradeResponse;
import com.okex.open.api.component.calculator.dto.PositionDetail;
import com.okex.open.api.component.calculatorTracker.dto.PositionCalculationResult;
import com.okex.open.api.component.constant.MarginMode;
import com.okex.open.api.component.constant.OkxTradeType;
import com.okex.open.api.component.constant.PositionSide;
import com.okex.open.api.component.tracker.DynamicStopLossTracker;
import com.okex.open.api.service.trading.TradingService;
import com.okex.open.api.service.trading.impl.TradingServiceImpl;
import com.okx.trading.BaseTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 主应用程序
 */
@Slf4j
public class OkxTradingSystemTest extends BaseTests {
    private TradingService tradingManager;

    @Before
    public void before() {
        this.config = this.config();
        log.info("========== OKX交易系统初始化 ==========");
        tradingManager = new TradingServiceImpl(this.config);
        log.info("交易系统初始化完成");
        log.info("当前模式: {}", tradingManager.isSimulator() ? "模拟交易" : "真实交易");
    }

//            String instrumentId = "BTC-USDT-SWAP";// 合约ID
    String instrumentId = "XRP-USDT-SWAP";

    /**
     * 开多结果
     */
    @Test
    public void openLongPosition() {

        OkxTradeType tradeType = OkxTradeType.BUY_OPEN_LONG_ISOLATED;//逐仓 SELL_CLOSE_LONG_ISOLATED
//        OkxTradeType tradeType = OkxTradeType.BUY_OPEN_LONG_CROSS;//全仓

        BigDecimal amount = new BigDecimal("10");

        Integer leverage = 3;

        TradeResponse response = tradingManager.tradeByUsdtValue(instrumentId, tradeType, amount, "market", leverage);
        System.out.printf("开多结果: %s\n", response.isSuccess() ? "成功" : "失败");
        if (response.isSuccess()) {
            System.out.printf("订单ID: %s\n", response.getData().getOrderId());
        } else {
            System.out.printf("错误: %s\n", response.getMessage());
        }
    }

    /**
     * 开空结果
     */
    @Test
    public void openShortPosition() {
        System.out.print("请输入USDT金额: ");
        BigDecimal amount = new BigDecimal("0.01");

        System.out.print("请输入杠杆倍数 (默认20): ");
        Integer leverage = 3;

        System.out.print("选择保证金模式 (1.全仓 2.逐仓): ");
        OkxTradeType tradeType = OkxTradeType.SELL_OPEN_SHORT_ISOLATED;//逐仓
//        OkxTradeType tradeType = OkxTradeType.SELL_OPEN_SHORT_CROSS;//全仓

        TradeResponse response = tradingManager.tradeByUsdtValue(instrumentId, tradeType, amount, "market", leverage);
        System.out.printf("开空结果: %s\n", response.isSuccess() ? "成功" : "失败");
        if (response.isSuccess()) {
            System.out.printf("订单ID: %s\n", response.getData().getOrderId());
        } else {
            System.out.printf("错误: %s\n", response.getMessage());
        }
    }

    /**
     * 平仓
     */
    @Test
    public void closePosition() {
        System.out.print("请输入平仓方向 (1.平多 2.平空): ");

        OkxTradeType tradeType = OkxTradeType.BUY_CLOSE_SHORT_ISOLATED;//平空
//        OkxTradeType tradeType = OkxTradeType.SELL_CLOSE_LONG_ISOLATED;//平多

        TradeResponse response = tradingManager.closePosition(instrumentId, tradeType);
        System.out.printf("平仓结果: %s\n", response.isSuccess() ? "成功" : "失败");
    }

    /**
     * 设置杠杆结果
     */
    @Test
    public void setLeverage() {
        System.out.print("请输入杠杆倍数: ");
        int leverage = 20;

        System.out.print("请输入保证金模式 (cross/isolated): ");
//        MarginMode marginMode = OMarginMode.CROSS;
        MarginMode marginMode = MarginMode.ISOLATED;

        System.out.print("请输入持仓方向 (long/short): ");
//        String positionSide = PositionSide.LONG;
        PositionSide positionSide = PositionSide.SHORT;

        boolean success = tradingManager.setLeverage(instrumentId, leverage, marginMode, positionSide);
        System.out.printf("设置杠杆结果: %s\n", success ? "成功" : "失败");
    }

    @Test
    public void showPositions1() {
        DynamicStopLossTracker tracker = DynamicStopLossTracker.builder()
                .entryPrice(BigDecimal.valueOf(100))
                .initialStopLossPercent(BigDecimal.valueOf(-1))
                .incrementPercent(BigDecimal.valueOf(5))
                .build();

        final var positions = tradingManager.getPositions(instrumentId);
        positions.printEnhancedPositions();
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        PositionDetail positionDetail = positions.getPositionDetailOne(instrumentId);
        PositionCalculationResult positionCalculationResult = positionDetail.calculateAll();
        log.info(positionCalculationResult.printSummary());

        // 每次更新后显示状态
        tracker.logStatus();

        if (tracker.isStopLossTriggered()) {
            log.info("\n⚠️ 止损已被触发！交易结束。");
        }
    }

    /**
     * 账户信息
     */
    @Test
    public void showPositions() {
        String instrumentId = "";
        AccountInfo accountInfo = tradingManager.getAccountInfo(instrumentId);
        if (accountInfo != null) {
            log.info("账户信息 - 总权益: {} USDT, 可用余额: {} USDT",
                    accountInfo.getTotalEquity(),
                    accountInfo.getAvailableBalance());
        }
    }

    /**
     * 交易类型列表
     */
    @Test
    public void showTradeTypes() {
        System.out.println("\n========== 交易类型列表 ==========");
        for (OkxTradeType type : OkxTradeType.values()) {
            System.out.printf("%-25s: side=%-5s posSide=%-7s mode=%-9s 描述=%-15s\n",
                    type.name(),
                    type.getSide(),
                    type.getPosSide(),
                    type.getTdMode(),
                    type.getDescription());
        }
        System.out.println("===================================");
    }

    /**
     * 执行策略
     */
    @Test
    public void executeStrategy() {
        System.out.println("可用策略: grid, dca, martingale");
        System.out.print("请选择策略: ");
        String strategy = "";

        // 这里可以添加策略参数输入
        executeStrategy(strategy, null);
    }

    /**
     * 示例交易代码
     */
    @Test
    public void runExample() {
        System.out.println("========== 示例交易 ==========");

        try {
            // 1. 开多仓
            System.out.println("\n1. 开多仓示例");
            var response1 = tradingManager.tradeByUsdtValue(
                    "BTC-USDT-SWAP",
                    OkxTradeType.BUY_OPEN_LONG_CROSS,
                    new BigDecimal("300"),
                    "market", 20
            );
            System.out.printf("开多结果: %s\n", response1.isSuccess() ? "成功" : "失败");

            // 2. 开空仓
            System.out.println("\n2. 开空仓示例");
            var response2 = tradingManager.tradeByUsdtValue(
                    "ETH-USDT-SWAP",
                    OkxTradeType.SELL_OPEN_SHORT_ISOLATED,
                    new BigDecimal("500"),
                    "market", 10
            );
            System.out.printf("开空结果: %s\n", response2.isSuccess() ? "成功" : "失败");

            // 3. 设置杠杆
            System.out.println("\n3. 设置杠杆示例");
            boolean leverageResult = tradingManager.setLeverage(
                    "BTC-USDT-SWAP",
                    50,
                    MarginMode.getByApiValue("cross"),
                    PositionSide.LONG
            );
            System.out.printf("设置杠杆结果: %s\n", leverageResult ? "成功" : "失败");

            // 4. 查看账户信息
            System.out.println("\n4. 账户信息");
            showPositions();

            // 5. 查看持仓
            System.out.println("\n5. 持仓信息");
            var positionsResponse = tradingManager.getPositions(instrumentId);
            positionsResponse.printEnhancedPositions();
//            log.info("持仓信息 - 合约: {}, 持仓数量: {}", instrumentId, positions.size());
//
//            for (var position : positionsResponse.getData()) {
//                log.info("  {}: {} 张, 均价: {}, 盈亏: {} USDT",
//                        position.getPositionSide(),
//                        position.getPosition(),
//                        position.getAveragePrice(),
//                        position.getUnrealizedPnl());
//            }

            // 6. 查看交易类型
            System.out.println("\n6. 交易类型");
            showTradeTypes();

        } catch (Exception e) {
            log.error("示例交易失败", e);
        }
    }


    /**
     * 切换交易模式
     */
    public void switchMode(boolean useSimulator) {
//        if (this.useSimulator != useSimulator) {
//            this.useSimulator = useSimulator;
//            this.currentService = factory.switchMode(useSimulator);
//            log.info("交易模式已切换: {}", useSimulator ? "模拟" : "真实");
//        }
    }

    public void switchMode() {
//        boolean currentMode = tradingManager.isSimulator();
//        boolean newMode = !currentMode;
//
//        switchMode(newMode);
//        System.out.printf("交易模式已切换: %s\n", newMode ? "模拟交易" : "真实交易");
    }


    /**
     * 执行交易策略
     */
    public void executeStrategy(String strategyName, Map<String, Object> params) {
        log.info("执行策略: {}", strategyName);

        switch (strategyName) {
            case "grid":
                executeGridStrategy(params);
                break;
            case "dca":
                executeDCAStrategy(params);
                break;
            case "martingale":
                executeMartingaleStrategy(params);
                break;
            default:
                log.warn("未知策略: {}", strategyName);
        }
    }

    /**
     * 网格策略
     *
     * @param params 参数
     */
    private void executeGridStrategy(Map<String, Object> params) {
        // 网格策略实现
        String instrumentId = (String) params.get("instrumentId");
        BigDecimal lowerPrice = (BigDecimal) params.get("lowerPrice");
        BigDecimal upperPrice = (BigDecimal) params.get("upperPrice");
        int gridCount = (int) params.get("gridCount");
        BigDecimal usdtPerGrid = (BigDecimal) params.get("usdtPerGrid");

        log.info("执行网格策略 - 区间: {}~{}, 网格数: {}, 每格金额: {}",
                lowerPrice, upperPrice, gridCount, usdtPerGrid);

        // 这里实现具体的网格逻辑
    }

    /**
     * 定投策略
     *
     * @param params 参数
     */
    private void executeDCAStrategy(Map<String, Object> params) {
        // 定投策略实现
        log.info("执行定投策略");
    }

    /**
     * 马丁格尔策略
     *
     * @param params 参数
     */
    private void executeMartingaleStrategy(Map<String, Object> params) {
        // 马丁格尔策略实现
        log.info("执行马丁格尔策略");
    }
}
