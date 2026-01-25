package com.okx.trading.constant;

import java.util.Arrays;
import java.util.List;


/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/1/24 01:29
 */
public class CommonConfig {

    static List<String> notBuild = Arrays.asList(IndicatorInfo.STRATEGY_CUSTOMIZE);

    public static boolean isNotBuild(String information) {
        return notBuild.contains(information);
    }
}
