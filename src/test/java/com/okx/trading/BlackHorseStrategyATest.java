package com.okx.trading;

import com.okx.trading.blackhorse.Bar;
import com.okx.trading.blackhorse.BlackHorseStrategyA;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

class BlackHorseStrategyATest {
    private BlackHorseStrategyA strategy;

    @BeforeEach
    void setUp() {
        strategy = new BlackHorseStrategyA(10.0, 10.0, 10.0);
    }

    @Test
    void testHtfAggregation() {
        long base = 1609459200000L; // 2021-01-01 00:00:00
        strategy.onBar(new Bar(100, 105, 99, 102, base + 3600_000));
        Assertions.assertFalse(strategy.isNewHtfBar());

        strategy.onBar(new Bar(103, 108, 101, 107, base + 4 * 3600_000));
        Assertions.assertTrue(strategy.isNewHtfBar());
    }

    @Test
    void testLongSignalAndStopLoss() {
        long base = 1609459200000L;
        strategy.onBar(new Bar(100, 100, 100, 100, base));
        strategy.onBar(new Bar(110, 115, 105, 112, base + 4 * 3600_000));
        assertEquals(1, strategy.getPosition());
        assertEquals(100.0, strategy.getStopLossPrice());
    }

    @Test
    void testTrailingStopActivation() {
        strategy.onBar(new Bar(100, 100, 100, 100, 0L));
        // 手动开多（因为开多逻辑需要前置4H数据，这里直接调用私有方法不便，通过模拟触发）
        // 或者用反射，但为了简洁，我们通过策略的onBar模拟完整流程
        // 这里简化：直接用开多方法（通过反射或修改为public，但测试中我们用更简单的方式）
        // 直接调用 openLong 不可能，所以我们采用公共接口触发
        // 我们构造一个场景：先让策略开多，再拉高价格激活移动止损
        // 由于开多需要 previousHtfBar，我们模拟两个4H bar
        long base = System.currentTimeMillis() - 10 * 3600_000L;
        strategy.onBar(new Bar(100, 100, 100, 100, base));
        strategy.onBar(new Bar(110, 115, 105, 112, base + 4 * 3600_000)); // 开多
        // 现在持仓，价格涨到115
        strategy.onBar(new Bar(115, 115, 115, 115, System.currentTimeMillis()));
        assertTrue(strategy.isTrailingActive());
        // 止损应为 115 - 10 = 105
        assertEquals(105.0, strategy.getStopLossPrice());
    }

    @Test
    void testStopLossHit() {
        long base = System.currentTimeMillis() - 10 * 3600_000L;
        strategy.onBar(new Bar(100, 100, 100, 100, base));
        strategy.onBar(new Bar(110, 115, 105, 112, base + 4 * 3600_000));
        // 价格下跌至止损100
        strategy.onBar(new Bar(95, 95, 95, 95, System.currentTimeMillis()));
        assertEquals(0, strategy.getPosition());
    }
}
