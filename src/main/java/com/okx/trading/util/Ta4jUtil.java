package com.okx.trading.util;

import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public class Ta4jUtil {

    /**
     * 创建BarSeries，注意TA4J 0.18的API变化
     */
    public static BarSeries createBarSeries(String name) {
        // 使用DoubleNum类型，注意0.18版本中默认使用DecimalNum
        return new BaseBarSeriesBuilder()
                .withName(name)
                .build();
    }

    /**
     * 计算4小时平均价
     * 注意：TA4J 0.18中移除了BaseBar的getOpenPrice等方法，使用getOpenPrice返回Num
     */
    public static Num calculateAveragePrice(Bar bar) {
        if (bar == null) return DoubleNum.valueOf(0);

        Num open = bar.getOpenPrice();
        Num high = bar.getHighPrice();
        Num low = bar.getLowPrice();
        Num close = bar.getClosePrice();

        return open.plus(high).plus(low).plus(close).dividedBy(DecimalNum.valueOf(4));
    }

    /**
     * 检查是否是新K线
     */
    public static boolean isNewBar(Bar currentBar, Bar previousBar) {
        if (currentBar == null || previousBar == null) return true;
        return !currentBar.getEndTime().equals(previousBar.getEndTime());
    }
}
