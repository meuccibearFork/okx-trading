package com.okx.trading.strategy;

import com.okx.trading.strategy.test.PriceUpdate;
import com.okx.trading.strategy.test.StopLossAdjustment;
import com.okx.trading.strategy.test.TradeStatistics;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态止损收益率追踪器
 * 初始止损点为-5%，每当收益率超过设定的阈值，止损点自动提高5%
 */
@Slf4j
@Getter
@ToString(exclude = {"adjustmentHistory", "priceHistory"})
@EqualsAndHashCode(exclude = {"currentPrice", "currentStopLoss", "currentStopLossPrice",
                              "maxReturnAchieved", "currentStatus", "lastUpdateTime",
                              "adjustmentHistory", "priceHistory"})
public class DynamicStopLossTracker {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 止损和收益阈值配置
    private final BigDecimal initialStopLoss;      // 初始止损百分比（如-5%）
    private final BigDecimal stopLossIncrement;    // 止损点每次增加的值（如5%）
    private final BigDecimal thresholdIncrement;   // 触发止损调整的收益阈值增量（如5%）

    // 当前状态
    private BigDecimal currentPrice;               // 当前价格
    private final BigDecimal entryPrice;           // 入场价格
    private BigDecimal currentStopLoss;            // 当前止损点（百分比）
    private BigDecimal currentStopLossPrice;       // 当前止损价格
    private BigDecimal maxReturnAchieved;          // 达到过的最高收益率
    private Status currentStatus;                  // 当前状态
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime startTime;               // 开始时间
    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime lastUpdateTime;          // 最后更新时间

    // 追踪记录
    private final List<StopLossAdjustment> adjustmentHistory;
    private final List<PriceUpdate> priceHistory;

    /**
     * 状态枚举
     */
    @AllArgsConstructor
    @Getter
    public enum Status {
        ACTIVE("运行中"),
        STOP_LOSS_TRIGGERED("已触发止损"),
        STOP_LOSS_ADJUSTED("止损已调整"),
        COMPLETED("已完成");

        private final String description;
    }

    /**
     * 构造器
     */
    @Builder
    public DynamicStopLossTracker(
            @NonNull BigDecimal entryPrice,
            @NonNull BigDecimal initialStopLossPercent,
            @NonNull BigDecimal incrementPercent) {

        // 验证参数
        validateParameters(entryPrice, initialStopLossPercent, incrementPercent);

        this.entryPrice = entryPrice;
        this.currentPrice = entryPrice;
        this.initialStopLoss = initialStopLossPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        this.stopLossIncrement = incrementPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        this.thresholdIncrement = incrementPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        this.currentStopLoss = this.initialStopLoss;
        this.currentStopLossPrice = calculateStopLossPrice(entryPrice, currentStopLoss);
        this.maxReturnAchieved = BigDecimal.ZERO;
        this.currentStatus = Status.ACTIVE;
        this.startTime = LocalDateTime.now();
        this.lastUpdateTime = startTime;

        this.adjustmentHistory = new ArrayList<>();
        this.priceHistory = new ArrayList<>();

        // 记录初始状态
        adjustmentHistory.add(StopLossAdjustment.builder()
                .reason("初始设置")
                .returnAtAdjustment(BigDecimal.ZERO)
                .newStopLossPercent(currentStopLoss.multiply(BigDecimal.valueOf(100)))
                .newStopLossPrice(currentStopLossPrice)
                .adjustmentTime(LocalDateTime.now())
                .build());

        // 记录初始价格
        priceHistory.add(PriceUpdate.builder()
                .price(entryPrice)
                .returnPercentage(BigDecimal.ZERO)
                .updateTime(LocalDateTime.now())
                .build());

        // 统一日志输出
        log.info("\n" +
                "┌─────────────────────────────────────────────────────┐\n" +
                "│     动态止损追踪器已创建成功                         │\n" +
                "├─────────────────────────────────────────────────────┤\n" +
                "│ 入场价格: {}\n" +
                "│ 初始止损: {}%\n" +
                "│ 调整幅度: {}%\n" +
                "│ 初始止损价格: {}\n" +
                "│ 创建时间: {}\n" +
                "└─────────────────────────────────────────────────────┘",
                entryPrice.setScale(4, RoundingMode.HALF_UP),
                initialStopLossPercent.setScale(4, RoundingMode.HALF_UP),
                incrementPercent.setScale(4, RoundingMode.HALF_UP),
                currentStopLossPrice.setScale(4, RoundingMode.HALF_UP),
                TIME_FORMATTER.format(startTime)
        );
    }

    /**
     * 参数验证
     */
    private void validateParameters(BigDecimal entryPrice, BigDecimal initialStopLossPercent, BigDecimal incrementPercent) {
        if (entryPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("入场价格必须大于0");
        }
        if (initialStopLossPercent.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalArgumentException("初始止损必须是负数（如-5表示-5%）");
        }
        if (incrementPercent.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("调整幅度必须是正数");
        }
    }

    /**
     * 更新当前价格并检查是否需要调整止损点
     * @param newPrice 新价格
     * @return 如果止损点被调整，返回true
     */
    public synchronized boolean updatePrice(@NonNull BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("价格更新失败: 价格必须大于0, 输入值: {}", newPrice);
            throw new IllegalArgumentException("价格必须大于0");
        }

        this.currentPrice = newPrice;
        this.lastUpdateTime = LocalDateTime.now();

        // 计算当前收益率
        BigDecimal currentReturn = calculateReturn(entryPrice, newPrice);

        // 记录价格历史
        priceHistory.add(PriceUpdate.builder()
                .price(newPrice)
                .returnPercentage(currentReturn)
                .updateTime(lastUpdateTime)
                .build());

        // 更新达到过的最高收益率
        updateMaxReturn(currentReturn);

        // 检查是否触发止损
        if (checkStopLossTriggered(currentReturn)) {
            return false;
        }

        // 检查是否需要调整止损点
        return checkAndAdjustStopLoss(currentReturn);
    }

    /**
     * 更新最高收益率
     */
    private void updateMaxReturn(BigDecimal currentReturn) {
        if (currentReturn.compareTo(maxReturnAchieved) > 0) {
            BigDecimal previousMax = maxReturnAchieved;
            maxReturnAchieved = currentReturn;
        }
    }

    /**
     * 检查是否触发止损
     */
    private boolean checkStopLossTriggered(BigDecimal currentReturn) {
        if (isStopLossTriggered()) {
            currentStatus = Status.STOP_LOSS_TRIGGERED;

            // 统一格式的止损触发日志
            log.warn("\n" +
                    "╔══════════════════════════════════════════════════════════╗\n" +
                    "║                    止损已触发！                         ║\n" +
                    "╟──────────────────────────────────────────────────────────╢\n" +
                    "║ 当前价格: {}                                            ║\n" +
                    "║ 止损价格: {}                                            ║\n" +
                    "║ 当前收益率: {}%                                         ║\n" +
                    "║ 触发时间: {}                                  ║\n" +
                    "╚══════════════════════════════════════════════════════════╝",
                    currentPrice.setScale(4, RoundingMode.HALF_UP),
                    currentStopLossPrice.setScale(4, RoundingMode.HALF_UP),
                    currentReturn.setScale(4, RoundingMode.HALF_UP),
                    TIME_FORMATTER.format(lastUpdateTime)
            );
            return true;
        }
        return false;
    }

    /**
     * 检查并调整止损点
     * @param currentReturn 当前收益率
     * @return 是否进行了调整
     */
    private boolean checkAndAdjustStopLoss(BigDecimal currentReturn) {
        BigDecimal nextThreshold = calculateNextThreshold();

        if (shouldAdjustStopLoss(currentReturn, nextThreshold)) {
            return adjustStopLossPoint(currentReturn);
        }

        currentStatus = Status.ACTIVE;
        return false;
    }

    /**
     * 计算下一个触发调整的阈值
     */
    private BigDecimal calculateNextThreshold() {
        int adjustmentsMade = adjustmentHistory.size() - 1;
        return thresholdIncrement
                .multiply(BigDecimal.valueOf(adjustmentsMade + 1))
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 判断是否需要调整止损点
     */
    private boolean shouldAdjustStopLoss(BigDecimal currentReturn, BigDecimal nextThreshold) {
        return currentReturn.compareTo(nextThreshold) >= 0;
    }

    /**
     * 调整止损点
     */
    private boolean adjustStopLossPoint(BigDecimal currentReturn) {
        int adjustmentsMade = adjustmentHistory.size() - 1;
        BigDecimal newStopLossPercent = initialStopLoss
                .add(stopLossIncrement.multiply(BigDecimal.valueOf(adjustmentsMade + 1)));

        if (isValidStopLossAdjustment(newStopLossPercent, currentReturn)) {
            return applyStopLossAdjustment(newStopLossPercent, currentReturn);
        }
        return false;
    }

    /**
     * 验证止损调整是否有效
     */
    private boolean isValidStopLossAdjustment(BigDecimal newStopLossPercent, BigDecimal currentReturn) {
        BigDecimal currentReturnDecimal = currentReturn.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return newStopLossPercent.compareTo(currentReturnDecimal) < 0
                && newStopLossPercent.compareTo(currentStopLoss) > 0;
    }

    /**
     * 应用止损调整
     */
    private boolean applyStopLossAdjustment(BigDecimal newStopLossPercent, BigDecimal currentReturn) {
        BigDecimal previousStopLossPercent = currentStopLoss.multiply(BigDecimal.valueOf(100));
        BigDecimal previousStopLossPrice = currentStopLossPrice;

        currentStopLoss = newStopLossPercent;
        currentStopLossPrice = calculateStopLossPrice(entryPrice, currentStopLoss);

        adjustmentHistory.add(StopLossAdjustment.builder()
                .reason(String.format("收益率达到%s%%触发调整", currentReturn.setScale(4, RoundingMode.HALF_UP)))
                .returnAtAdjustment(currentReturn)
                .newStopLossPercent(currentStopLoss.multiply(BigDecimal.valueOf(100)))
                .newStopLossPrice(currentStopLossPrice)
                .adjustmentTime(LocalDateTime.now())
                .build());

        currentStatus = Status.STOP_LOSS_ADJUSTED;

        // 统一格式的止损调整日志
        log.info("\n" +
                "┌─────────────────────────────────────────────────────┐\n" +
                "│             止损点已调整                            │\n" +
                "├─────────────────────────────────────────────────────┤\n" +
                "│ 当前收益率: {}%\n" +
                "│ 原止损点: {}%\n" +
                "│ 新止损点: {}%\n" +
                "│ 原止损价格: {}\n" +
                "│ 新止损价格: {}\n" +
                "│ 调整时间: {}\n" +
                "└─────────────────────────────────────────────────────┘",
                currentReturn.setScale(4, RoundingMode.HALF_UP),
                previousStopLossPercent.setScale(4, RoundingMode.HALF_UP),
                getCurrentStopLossPercent().setScale(4, RoundingMode.HALF_UP),
                previousStopLossPrice.setScale(4, RoundingMode.HALF_UP),
                currentStopLossPrice.setScale(4, RoundingMode.HALF_UP),
                TIME_FORMATTER.format(LocalDateTime.now())
        );
        return true;
    }

    /**
     * 计算止损价格
     */
    private BigDecimal calculateStopLossPrice(BigDecimal basePrice, BigDecimal stopLossPercent) {
        return basePrice.multiply(BigDecimal.ONE.add(stopLossPercent));
    }

    /**
     * 计算收益率（百分比）
     */
    private BigDecimal calculateReturn(BigDecimal entryPrice, BigDecimal currentPrice) {
        return currentPrice.subtract(entryPrice)
                .divide(entryPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 检查是否触发止损
     * @return 如果触发止损返回true
     */
    public boolean isStopLossTriggered() {
        return currentPrice.compareTo(currentStopLossPrice) <= 0;
    }

    /**
     * 完成交易
     */
    public void completeTrade() {
        if (currentStatus != Status.STOP_LOSS_TRIGGERED) {
            currentStatus = Status.COMPLETED;
            log.info("\n" +
                    "┌─────────────────────────────────────────────────────┐\n" +
                    "│             交易完成                               │\n" +
                    "├─────────────────────────────────────────────────────┤\n" +
                    "│ 最终收益率: {}%\n" +
                    "│ 完成时间: {}\n" +
                    "└─────────────────────────────────────────────────────┘",
                    getCurrentReturn().setScale(4, RoundingMode.HALF_UP),
                    TIME_FORMATTER.format(LocalDateTime.now())
            );
        }
    }

    /**
     * 获取当前收益率（百分比）
     */
    public BigDecimal getCurrentReturn() {
        return calculateReturn(entryPrice, currentPrice);
    }

    /**
     * 获取当前止损点（百分比）
     */
    public BigDecimal getCurrentStopLossPercent() {
        return currentStopLoss.multiply(BigDecimal.valueOf(100));
    }

    /**
     * 获取调整历史
     */
    public List<StopLossAdjustment> getAdjustmentHistory() {
        return new ArrayList<>(adjustmentHistory);
    }

    /**
     * 获取价格历史
     */
    public List<PriceUpdate> getPriceHistory() {
        return new ArrayList<>(priceHistory);
    }

    /**
     * 获取统计信息
     */
    public TradeStatistics getStatistics() {
        return TradeStatistics.builder()
                .startTime(startTime)
                .lastUpdateTime(lastUpdateTime)
                .entryPrice(entryPrice)
                .currentPrice(currentPrice)
                .currentReturn(getCurrentReturn())
                .maxReturnAchieved(maxReturnAchieved)
                .currentStopLossPercent(getCurrentStopLossPercent())
                .stopLossPrice(currentStopLossPrice)
                .adjustmentCount(adjustmentHistory.size() - 1)
                .priceUpdateCount(priceHistory.size())
                .status(currentStatus)
                .build();
    }

    /**
     * 打印当前状态（美观的单条日志）
     */
    public void logStatus() {
        TradeStatistics stats = getStatistics();

        // 计算盈亏比 - 修复除以零错误
        BigDecimal riskRewardRatio = BigDecimal.ZERO;
        if (stats.getCurrentReturn().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal risk = stats.getCurrentStopLossPercent().abs();
            // 避免除以零错误
            if (risk.compareTo(BigDecimal.ZERO) != 0) {
                riskRewardRatio = stats.getCurrentReturn().divide(risk, 4, RoundingMode.HALF_UP);
            } else {
                // 如果风险为0（止损点为0%），则盈亏比为无穷大，用9999表示
                riskRewardRatio = BigDecimal.valueOf(9999.99);
            }
        }

        // 计算运行时间
        long runningSeconds = java.time.Duration.between(stats.getStartTime(), stats.getLastUpdateTime()).getSeconds();

        // 计算距止损空间
        BigDecimal stopLossDistance = calculateStopLossDistance();

        // 统一格式的状态日志
        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║                   交易状态概览                           ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ 入场价格: {}\n" +
                "║ 当前价格: {}\n" +
                "║ 当前收益率: {}%\n" +
                "║ 最高收益率: {}%\n" +
                "║ 当前止损点: {}%\n" +
                "║ 止损价格: {}\n" +
                "║ 距止损空间: {}%\n" +
                "║ 盈亏比: {}\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ 状态: {}\n" +
                "║ 运行时间: {}秒\n" +
                "║ 调整次数: {}\n" +
                "║ 价格更新次数: {}\n" +
                "║ 更新时间: {}\n" +
                "╚══════════════════════════════════════════════════════════╝",
                stats.getEntryPrice().setScale(4, RoundingMode.HALF_UP),
                stats.getCurrentPrice().setScale(4, RoundingMode.HALF_UP),
                stats.getCurrentReturn().setScale(4, RoundingMode.HALF_UP),
                stats.getMaxReturnAchieved().setScale(4, RoundingMode.HALF_UP),
                stats.getCurrentStopLossPercent().setScale(4, RoundingMode.HALF_UP),
                stats.getStopLossPrice().setScale(4, RoundingMode.HALF_UP),
                stopLossDistance.setScale(4, RoundingMode.HALF_UP),
                riskRewardRatio.setScale(4, RoundingMode.HALF_UP),
                stats.getStatus().getDescription(),
                runningSeconds,
                stats.getAdjustmentCount(),
                stats.getPriceUpdateCount(),
                TIME_FORMATTER.format(stats.getLastUpdateTime())
        );
    }

    /**
     * 计算距止损点的距离（百分比）
     */
    private BigDecimal calculateStopLossDistance() {
        try {
            BigDecimal distance = currentPrice.subtract(currentStopLossPrice)
                    .divide(currentPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            return distance.setScale(4, RoundingMode.HALF_UP);
        } catch (ArithmeticException e) {
            // 如果当前价格为0（理论上不会发生），返回0
            return BigDecimal.ZERO;
        }
    }

    /**
     * 打印调整历史（美观的格式）
     */
    public void logAdjustmentHistory() {
        if (adjustmentHistory.isEmpty()) {
            log.info("暂无调整历史");
            return;
        }

        StringBuilder historyLog = new StringBuilder();
        historyLog.append("\n╔══════════════════════════════════════════════════════════════════════════════════════════════╗\n");
        historyLog.append("║                                 止损点调整历史                                              ║\n");
        historyLog.append("╠══════════════════════════════════════════════════════════════════════════════════════════════╣\n");

        for (int i = 0; i < adjustmentHistory.size(); i++) {
            StopLossAdjustment adjustment = adjustmentHistory.get(i);
            String line = String.format("║ %-3d. %-20s | 收益率: %-8s | 止损点: %-8s | 止损价: %-8s | 时间: %s ║",
                    i + 1,
                    adjustment.getReason(),
                    adjustment.getReturnAtAdjustment().setScale(4, RoundingMode.HALF_UP) + "%",
                    adjustment.getNewStopLossPercent().setScale(4, RoundingMode.HALF_UP) + "%",
                    adjustment.getNewStopLossPrice().setScale(4, RoundingMode.HALF_UP),
                    TIME_FORMATTER.format(adjustment.getAdjustmentTime()));
            historyLog.append(line).append("\n");

            // 如果不是最后一项，添加分隔线
            if (i < adjustmentHistory.size() - 1) {
                historyLog.append("╟──────────────────────────────────────────────────────────────────────────────────────────────────╢\n");
            }
        }

        historyLog.append("╚══════════════════════════════════════════════════════════════════════════════════════════════╝");

        log.info(historyLog.toString());
    }
}

