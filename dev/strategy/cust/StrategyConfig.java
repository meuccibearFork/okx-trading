package com.okx.trading.strategy.cust;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "strategy.heima-a")
public class StrategyConfig {
    // 策略开关
    private boolean enabled = false;

    // 时间框架配置
    private Duration baseTimeframe = Duration.ofMinutes(5);
    private Duration higherTimeframe = Duration.ofHours(4);

    // 策略参数
    private double triggerPoints = 10.0;      // 触发移动止盈点数
    private double trailOffset = 10.0;        // 移动止盈偏移
    private double trailPoints = 10.0;        // 移动止盈点数

    // 风险管理
    private double positionPercent = 10.0;    // 仓位百分比
    private double stopLossPercent = 2.0;     // 止损百分比
    private double maxDailyLoss = 5.0;        // 最大日亏损
}
