package com.okx.trading.strategy.cust.demo;

import com.okx.trading.model.entity.RealTimeStrategyEntity;
import com.okx.trading.model.market.Candlestick;
import org.ta4j.core.BarSeries;

import java.util.Map;

/**
 * 实时策略基类
 */
public abstract class BaseRealTimeStrategy {

    protected final RealTimeStrategyEntity strategyEntity;

    protected BaseRealTimeStrategy(RealTimeStrategyEntity strategyEntity) {
        this.strategyEntity = strategyEntity;
    }

    /**
     * 处理新的K线数据
     */
    public abstract Map<String, Object> processCandle(Candlestick candle);

    /**
     * 获取策略代码
     */
    public abstract String getStrategyCode();

    /**
     * 获取策略名称
     */
    public abstract String getStrategyName();

    /**
     * 获取策略描述
     */
    public abstract String getStrategyDescription();

    /**
     * 获取策略参数
     */
    public abstract Map<String, Object> getStrategyParameters();

    /**
     * 获取策略实体
     */
    public RealTimeStrategyEntity getStrategyEntity() {
        return strategyEntity;
    }

    /**
     * 初始化策略（可选）
     */
    public void initialize(BarSeries barSeries) {
        // 子类可重写此方法进行初始化
    }

    /**
     * 清理资源（可选）
     */
    public void cleanup() {
        // 子类可重写此方法进行清理
    }
}
