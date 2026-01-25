package com.okx.trading.cust.service;

/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/1/25 01:10
 */
public interface HeimaStrategyService {


    void initialize(String symbol);

    void startStrategy();

    void stopStrategy();
}
