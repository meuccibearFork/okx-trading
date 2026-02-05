package com.okx.trading.strategy;

import com.okx.trading.constant.CommonConfig;
import com.okx.trading.controller.TradeController;
import com.okx.trading.model.entity.RealTimeOrderEntity;
import com.okx.trading.model.entity.RealTimeStrategyEntity;
import com.okx.trading.model.market.Candlestick;
import com.okx.trading.model.trade.Order;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.okx.trading.constant.IndicatorInfo.*;

/**
 * <p>
 *
 * </p>
 *
 * @author lv.mr
 * @since 2026/1/26 00:36
 */
@Service
public class CommonService {
    private final TradeController tradeController;

    public CommonService(TradeController tradeController) {
        this.tradeController = tradeController;
    }

}
