package com.okx.trading.constant.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.List;

/**
 * @author xiahuiqiang
 * @since 2024/08/16
 */
public class AdminLogFilter extends AbstractMatcherFilter<ILoggingEvent> {

    Level level;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        List<String> ignoreList = LoggerName.LOG_NAME;

        if (ignoreList.contains(event.getLoggerName())) {
            return FilterReply.DENY;
        }

        if (event.getLevel().equals(level)) {
            return onMatch;
        } else {
            return onMismatch;
        }
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void start() {
        if (this.level != null) {
            super.start();
        }
    }
}
