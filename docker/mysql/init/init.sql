-- 初始化数据库脚本
CREATE DATABASE IF NOT EXISTS okx_trading DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE okx_trading;

-- 创建K线数据表
CREATE TABLE IF NOT EXISTS `candlestick_history` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `symbol` VARCHAR(20) NOT NULL COMMENT '交易对，如BTC-USDT',
  `interval_val` VARCHAR(10) NOT NULL COMMENT 'K线间隔，如1m, 5m, 15m, 30m, 1H, 2H, 4H, 6H, 12H, 1D, 1W, 1M',
  `open_time` DATETIME NOT NULL COMMENT '开盘时间',
  `close_time` DATETIME COMMENT '收盘时间',
  `open` DECIMAL(30, 15) COMMENT '开盘价',
  `high` DECIMAL(30, 15) COMMENT '最高价',
  `low` DECIMAL(30, 15) COMMENT '最低价',
  `close` DECIMAL(30, 15) COMMENT '收盘价',
  `volume` DECIMAL(30, 15) COMMENT '成交量',
  `quote_volume` DECIMAL(30, 15) COMMENT '成交额',
  `trades` BIGINT(20) COMMENT '成交笔数',
  `fetch_time` DATETIME COMMENT '数据获取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_symbol_interval_opentime` (`symbol`, `interval_val`, `open_time`),
  INDEX `idx_symbol_interval` (`symbol`, `interval_val`),
  INDEX `idx_opentime` (`open_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='K线历史数据';

-- 创建实时策略表
-- 实时策略表
CREATE TABLE IF NOT EXISTS `real_time_strategy` ( `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键ID', `strategy_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '实时策略唯一代码', `symbol` VARCHAR(20) NOT NULL COMMENT '交易对符号，如BTC-USDT', `interval_val` VARCHAR(10) NOT NULL COMMENT 'K线周期，如1m, 5m, 1h等', `start_time` DATETIME NOT NULL COMMENT '策略运行开始时间', `trade_amount` DOUBLE COMMENT '交易金额', `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否有效/启用', `status` VARCHAR(20) DEFAULT 'STOPPED' COMMENT '策略运行状态：RUNNING(运行中), STOPPED(已停止), COMPLETED(已完成), ERROR(错误)', `error_message` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '错误信息', `create_time` DATETIME NOT NULL COMMENT '创建时间', `update_time` DATETIME NOT NULL COMMENT '更新时间', INDEX `idx_strategy_code` (`strategy_code`), INDEX `idx_symbol` (`symbol`), INDEX `idx_status` (`status`), INDEX `idx_is_active` (`is_active`), INDEX `idx_create_time` (`create_time`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实时运行策略表';



# candlestick_history
# alter table candlestick_history modify symbol varchar(20) not null comment '交易对，如BTC-USDT ';
# alter table candlestick_history modify interval_val varchar(10) not null comment 'K线间隔，如1m, 5m, 15m, 30m, 1H, 2H, 4H, 6H, 12H, 1D, 1W, 1M ';
# alter table candlestick_history modify open_time datetime not null comment '开盘时间 ';
# alter table candlestick_history modify close_time datetime null comment '收盘时间 ';
# alter table candlestick_history modify open decimal(30, 15) null comment '开盘价 ';
# alter table candlestick_history modify high decimal(30, 15) null comment '最高价 ';
# alter table candlestick_history modify low decimal(30, 15) null comment '最低价 ';
# alter table candlestick_history modify close decimal(30, 15) null comment '收盘价 ';
# alter table candlestick_history modify volume decimal(30, 15) null comment '成交量 ';
# alter table candlestick_history modify quote_volume decimal(30, 15) null comment '成交额 ';
# alter table candlestick_history modify trades bigint null comment '成交笔数 ';
# alter table candlestick_history modify fetch_time datetime null comment '数据获取时间 ';

# real_time_strategy
# alter table real_time_strategy modify id bigint auto_increment comment '自增主键ID ';
# alter table real_time_strategy modify strategy_code varchar(50) not null comment '实时策略唯一代码 ';
# alter table real_time_strategy modify symbol varchar(20) not null comment '交易对符号，如BTC-USDT ';
# alter table real_time_strategy modify interval_val varchar(10) not null comment 'K线周期，如1m, 5m, 1h等 ';
# alter table real_time_strategy modify start_time datetime not null comment '策略运行开始时间 ';
# alter table real_time_strategy modify trade_amount double null comment '交易金额 ';
# alter table real_time_strategy modify is_active tinyint(1) default 1 not null comment '是否有效/启用 ';
# alter table real_time_strategy modify status varchar(20) default 'STOPPED' null comment '策略运行状态：RUNNING(运行中), STOPPED(已停止), COMPLETED(已完成), ERROR(错误) ';
# alter table real_time_strategy modify error_message text collate utf8mb4_unicode_ci null comment '错误信息 ';
# alter table real_time_strategy modify create_time datetime not null comment '创建时间 ';
# alter table real_time_strategy modify update_time datetime not null comment '更新时间 ';

