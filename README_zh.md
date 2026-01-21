# OKX 智能交易策略回测系统

## 🎯 项目概述

OKX Trading是一款基于Java Spring Boot开发的智能加密货币交易策略回测系统，集成了策略实盘交易、AI策略生成、历史数据回测、策略评价等功能。
> 使用[cryptoquantx](https://github.com/ralph-wren/cryptoquantx)作为前端平台。

> 个人博客地址 [Ralph's Blog](https://pothos.dpdns.org/)
## 🚀 核心功能

### 📈 实盘自动交易系统
![img.png](imgs/img.png)
![img_7.png](imgs/img_7.png)
- **实时策略执行引擎**：支持策略在实盘环境中24/7自动执行交易。
- **智能信号识别**：实时监控K线数据，自动识别买卖信号并执行交易。
- **多交易方式支持**：支持现货交易和合约交易，灵活的下单方式。
- **风险控制机制**：内置止损止盈、仓位管理、频率控制等风险管理功能。
- **策略组合运行**：支持多个策略同时运行，实现投资组合自动化。
- **交易状态监控**：实时监控策略运行状态、持仓情况、盈亏统计。


### 🔔 多渠道交易提醒系统
![img_1.png](imgs/img_1.png)
- **实时交易通知**：买卖信号触发时即时推送交易提醒。
- **多种通知方式**：支持Server酱、企业微信、邮件三种通知渠道。
- **详细交易信息**：包含策略名称、交易对、价格、数量、利润等完整信息。
- **错误预警通知**：策略执行异常时自动发送错误通知。
- **个性化配置**：支持按需开启/关闭不同类型的通知。


### 🤖 AI智能策略生成
![img_2.png](imgs/img_2.png)
- **自然语言策略生成**：基于DeepSeek API，通过自然语言描述自动生成Ta4j交易策略
- **智能策略理解**：支持理解和解析复杂的策略描述，如"基于双均线和RSI组合的交易策略"
- **动态编译加载**：使用Janino和Java Compiler API进行实时策略代码编译和动态加载
- **策略热重载**：无需重启服务即可加载新策略，支持策略的实时更新和删除
- **多重编译方式**：支持Janino、Java Compiler API和三种编译方式的智能选择
- **策略管理**：完整的策略CRUD操作，支持策略版本控制和历史追踪


### 📊 丰富的策略库 (250+ 策略)
![img_3.png](imgs/img_3.png)
#### 移动平均策略 (15种)
- **经典移动平均**：SMA、EMA、WMA、HMA等基础移动平均
- **高级移动平均**：KAMA自适应、ZLEMA零滞后、DEMA/TEMA多重指数平滑
- **特殊移动平均**：VWAP成交量加权、TRIMA三角平滑、T3高级平滑
- **自适应移动平均**：MAMA自适应、VIDYA变动态、Wilders平滑

#### 振荡器策略 (17种)
- **经典指标**：RSI、随机指标、威廉指标、CCI
- **复合指标**：随机RSI、CMO、ROC、PPO
- **高级指标**：TRIX、Fisher变换、EOM易动性
- **专业指标**：CHOP噪音指数、KVO克林格成交量振荡器、RVGI相对活力

#### 趋势指标 (14种)
- **趋势确认**：MACD、ADX、Aroon、DMI
- **趋势跟踪**：Supertrend、抛物线SAR、一目均衡表
- **高级趋势**：涡流指标、QStick、威廉鳄鱼
- **数学趋势**：希尔伯特变换系列

#### 波动率指标 (12种)
- **通道指标**：布林带、肯特纳通道、唐奇安通道
- **波动率测量**：ATR、溃疡指数、标准差、波动率
- **高级波动率**：质量指数、挤压、BBW布林带宽度
- **特殊波动率**：吊灯止损、NATR标准化ATR

#### 成交量指标 (12种)
- **经典成交量**：OBV能量潮、A/D累积分布线、质量指数
- **高级成交量**：KDJ、AD/ADOSC振荡器、PVI/NVI正负成交量指数
- **成交量分析**：VWMA成交量加权、VOSC成交量振荡器、MarketFI市场促进

#### K线形态策略 (16种)
- **反转形态**：十字星、锤子线、流星线
- **吞没形态**：看涨/看跌吞没
- **组合形态**：早晨之星/黄昏之星、穿刺线
- **特殊形态**：三白兵/三黑鸦

#### 统计函数策略 (8种)
- **相关性分析**：Beta系数、皮尔逊相关系数
- **回归分析**：线性回归、线性回归角度/斜率/截距
- **统计指标**：方差、时间序列预测、标准差

#### 希尔伯特变换策略 (6种)
- **周期分析**：主导周期、主导相位、趋势模式
- **信号处理**：正弦波、相量分量、MESA正弦波

#### 组合策略 (20种)
- **经典组合**：双重推力、海龟交易
- **趋势组合**：金叉/死叉、趋势跟踪
- **复合策略**：双均线+RSI、MACD+布林带、三重筛选
- **创新组合**：一目均衡表突破、Elder Ray力度分析

#### 高级策略库 (50种)
- **自适应策略**：自适应布林带、多时间框架MACD、自适应RSI
- **高级成交量**：克林格振荡器、佳庆振荡器、力度指数
- **高级移动平均**：分形自适应、零滞后EMA、高斯/巴特沃斯滤波器
- **专业指标**：火箭RSI、康纳斯RSI、终极振荡器

#### 创新策略集 (40种)
- **机器学习启发**：神经网络、遗传算法、随机森林、SVM
- **量化因子**：动量因子、价值因子、质量因子、低波动因子
- **高频策略**：微观结构失衡、日内均值回归、统计套利
- **风险管理**：凯利准则、VaR风险管理、最大回撤控制

### 🔬 高级回测系统

#### 多维度回测分析
- **Ta4j集成**：基于专业Ta4j技术分析库，提供标准化回测框架
- **多时间周期**：支持1分钟到1个月各种K线周期的回测
- **批量回测**：支持所有策略的批量回测，便于策略筛选和对比
- **并行回测**：支持多线程并行回测，提高效率

#### 丰富的性能指标
![img_5.png](imgs/img_5.png)
- **收益指标**：总收益、年化收益、绝对收益、超额收益
- **风险指标**：最大回撤、夏普比率、波动率、下行偏差
- **交易指标**：胜率、盈亏比、平均持仓时间、交易频率
- **高级指标**：卡尔玛比率、索提诺比率、信息比率、跟踪误差

#### 详细交易记录
![img_6.png](imgs/img_6.png)
- **完整记录**：保存每笔交易的买入卖出价格、时间和盈亏
- **交易分析**：提供交易分布统计、盈亏分析、持仓周期分析
- **可视化支持**：支持交易记录的图表可视化分析

## 📈 风险指标系统
![img_4.png](imgs/img_4.png)
### 风险指标 (33个)
- **峰度 (kurtosis)**：衡量收益率分布的尾部风险
- **条件风险价值 (cvar)**：极端损失的期望值
- **风险价值 (var95, var99)**：95%和99%置信度下的风险价值
- **信息比率 (informationRatio)**：超额收益相对于跟踪误差的比率
- **跟踪误差 (trackingError)**：策略与基准收益率的标准差
- **Sterling比率 (sterlingRatio)**：年化收益与平均最大回撤的比率
- **Burke比率 (burkeRatio)**：年化收益与平方根回撤的比率
- **修正夏普比率 (modifiedSharpeRatio)**：考虑偏度和峰度的夏普比率
- **下行偏差 (downsideDeviation)**：只考虑负收益的标准差
- **上涨捕获率 (uptrendCapture)**：基准上涨时策略的表现
- **下跌捕获率 (downtrendCapture)**：基准下跌时策略的表现
- **最大回撤持续期 (maxDrawdownDuration)**：从峰值到恢复的最长时间
- **痛苦指数 (painIndex)**：回撤深度与持续时间的综合指标
- **风险调整收益 (riskAdjustedReturn)**：综合多种风险因素的收益评估

### 综合评分系统
- **科学评分体系**：0-10分的科学评分体系
- **四个维度评估**：收益表现、风险控制、交易质量、稳定性
- **权重分配**：收益表现40%、风险控制30%、交易质量20%、稳定性10%

## 🌐 动态指标分布评分系统

### 系统特点
- **数据驱动**：基于6000个真实回测样本的分布情况
- **动态阈值**：避免固定阈值的主观性和局限性
- **分位数评分**：采用分位数划分区间，确保评分的均匀分布
- **多维度评估**：收益、风险、质量、稳定性四个维度综合评估


### 💾 数据管理与存储

#### 历史数据管理
- **OKX API集成**：自动从OKX交易所获取历史K线数据
- **多币种支持**：支持OKX交易所所有交易对数据
- **数据缓存**：Redis缓存优化，提高数据访问性能
- **数据清理**：自动化数据清理和维护机制

#### 数据库架构
- **MySQL存储**：使用MySQL存储策略信息、回测结果和交易记录
- **完整架构**：包含策略信息表、回测摘要表、交易详情表
- **数据迁移**：Liquibase管理数据库版本和迁移
- **性能优化**：合理的索引设计和查询优化

### 🔧 技术特性

#### 系统架构
- **微服务设计**：模块化设计，功能模块独立且可扩展
- **异步处理**：支持异步回测和数据处理，提高系统响应性
- **容器化部署**：Docker + Docker Compose支持，便于部署和扩展
- **配置管理**：灵活的配置管理，支持多环境部署

#### 开发特性
- **代码质量**：完整的单元测试覆盖，规范的代码结构
- **完整文档**：Swagger API文档，详细的代码注释
- **热重载**：JRebel支持，开发期间代码即时更改
- **日志管理**：完善的日志系统，分级记录和日志分析

## 🛠 技术栈

### 后端框架
- **Spring Boot 3.2.5**：核心框架
- **Java 21**：开发语言
- **Maven**：项目管理和构建工具

### 数据存储
- **MySQL 8.0**：主要数据库
- **Redis 6.0+**：缓存数据库
- **Liquibase**：数据库版本管理

### 技术分析
- **Ta4j 0.18**：专业技术分析库
- **Janino**：动态代码编译
- **Java Compiler API**：高级代码编译

### AI集成
- **DeepSeek API**：AI策略生成
- **自然语言处理**：策略描述解析
- **智能编译**：多种编译方式的自动选择

### 网络通信
- **OkHttp3 4.9.3**：HTTP客户端
- **WebSocket**：实时数据获取
- **RESTful API**：标准API接口

### 部署运维
- **Docker**：容器化部署
- **Docker Compose**：多容器编排
- **Nginx**：反向代理(可选)

## 🚀 快速开始

### 环境要求
- Java 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+
- Docker & Docker Compose (可选)

### 本地开发部署

1. **克隆项目**
```bash
git clone https://github.com/your-repo/okx-trading.git
cd okx-trading
```

2. **配置数据库**
```bash
# 启动MySQL和Redis
docker-compose up -d mysql redis

# 或使用现有数据库，修改application.properties
```

3. **配置参数**
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/okx_trading
spring.datasource.username=root
spring.datasource.password=your_password

spring.redis.host=localhost
spring.redis.port=6379

# OKX API配置
okx.api.key=your_api_key
okx.api.secret=your_api_secret
okx.api.passphrase=your_passphrase
```

4. **启动应用**
```bash
mvn spring-boot:run
```

5. **访问系统**
- API文档：http://localhost:8088/swagger-ui.html
- 健康检查：http://localhost:8088/actuator/health

### Docker部署

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app
```

## 📚 API文档

### 策略管理
- `GET /api/strategy/list` - 获取策略列表
- `POST /api/strategy/generate` - AI生成策略
- `PUT /api/strategy/update` - 更新策略
- `DELETE /api/strategy/{id}` - 删除策略

### 回测分析
- `POST /api/backtest/ta4j/run` - 单策略回测
- `POST /api/backtest/ta4j/run-all` - 批量回测
- `GET /api/backtest/results/{id}` - 获取回测结果
- `GET /api/backtest/ta4j/indicator-distribution-details` - 指标分布统计

### 市场数据
- `GET /api/market/candlestick` - 获取K线数据
- `POST /api/market/subscribe` - 订阅实时数据
- `DELETE /api/market/unsubscribe` - 取消订阅

### 动态评分
- `POST /api/backtest/ta4j/calculate-dynamic-score` - 计算动态评分

## 🧪 测试说明

### 新增风险指标测试
1. **单个策略回测测试**
```bash
curl -X POST "http://localhost:8088/api/backtest/ta4j/run?endTime=2025-01-01%2000%3A00%3A00&initialAmount=10000&interval=1D&saveResult=true&startTime=2024-01-01%2000%3A00%3A00&strategyType=SMA&symbol=BTC-USDT"
```

2. **批量策略回测测试**
```bash
curl -X POST "http://localhost:8088/api/backtest/ta4j/run-all?startTime=2024-01-01%2000%3A00%3A00&endTime=2024-12-01%2000%3A00%3A00&initialAmount=10000&symbol=BTC-USDT&interval=1D&saveResult=true&feeRatio=0.001"
```

### 数据库迁移
```bash
mysql -u root -p okx_trading < src/main/resources/migration_add_risk_indicators.sql
```

## 📊 性能监控

### 日志配置
- 应用日志：`logs/all/`
- API日志：`logs/api/`
- 错误日志：`logs/error/`

### 监控指标
- WebSocket连接状态
- 回测执行性能
- 数据库查询性能
- Redis缓存命中率
- API调用频率

## 🔒 安全配置

### API安全
- OKX API密钥加密存储
- 请求签名验证
- 频率限制控制

### 数据安全
- 数据库连接加密
- 敏感信息脱敏
- 访问权限控制

## ⚡ 实盘自动交易与交易提醒详解

### 1. 实盘自动交易原理
- 系统支持将任意策略一键应用于实盘，自动监听实时K线数据。
- 策略触发买卖信号后，自动调用交易接口下单，支持现货和合约。
- 每个策略独立运行，支持多策略并行，自动管理持仓和资金。
- 交易执行过程自动记录订单、盈亏、手续费等详细信息。
- 支持自动止盈止损、仓位管理、频率限制等风控措施。

### 2. 交易提醒机制
- 每次实盘买入/卖出都会自动推送交易提醒。
- 支持Server酱、企业微信、邮件等多种通知方式，灵活配置。
- 通知内容包括策略名称、交易对、方向、价格、数量、利润、时间等。
- 策略运行异常、下单失败等也会自动推送错误预警。
- 通知可按需开启/关闭，支持多用户接收。

### 3. 配置方法
- 在`application.properties`或`.env`中配置：
  - `notification.type=server_chan|wechat_cp|email` 选择通知方式
  - `notification.trade.enabled=true` 开启交易提醒
  - `notification.error.enabled=true` 开启错误提醒
  - 相关渠道的API Key、邮箱等参数
- 策略实盘参数可通过前端页面或API接口设置，包括交易对、周期、资金、风控参数等。

### 4. 常见问题与排查
- **Q: 实盘策略没有自动下单？**
  - 检查API密钥、资金余额、策略参数是否正确
  - 查看日志有无异常，确认WebSocket行情正常
- **Q: 没有收到交易提醒？**
  - 检查通知配置是否正确，API Key/邮箱是否有效
  - 查看日志确认通知服务是否被调用
- **Q: 策略异常停止？**
  - 查看错误通知内容，排查策略逻辑或行情数据问题
  - 可在前端或数据库中重启策略

### 5. 推荐实盘操作流程
1. 回测验证策略有效性，优化参数
2. 小资金实盘试运行，观察交易和通知
3. 逐步加大资金，开启多策略组合
4. 持续关注通知和日志，及时调整策略


## 🤝 贡献指南

### 开发流程
1. Fork项目
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

### 代码规范
- 遵循Java编码规范
- 添加必要的单元测试
- 更新相关文档
- 保持代码质量

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 🙋‍♂️ 支持与反馈

如有问题或建议，请：
1. 提交GitHub Issue
2. 查看项目Wiki
3. 联系项目维护者ralph_jungle@163.com

---

如需更详细的实盘交易和通知配置说明，请参考项目Wiki或联系维护者。

---

**OKX智能交易策略回测系统** - 让量化交易更智能，让策略开发更简单！





MYSQL_USERNAME=root
MYSQL_PASSWORD=Password123?
SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/okx_trading?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false
SPRING_REDIS_HOST=host.docker.internal
SPRING_REDIS_PORT=6379
JAVA_OPTS=-Xmx1g -Xms512m
OKX_API_KEY=${OKX_API_KEY:-}
OKX_SECRET_KEY=${OKX_SECRET_KEY:-}
OKX_PASSPHRASE=${OKX_PASSPHRASE:-}
PROXY_ENABLED=${PROXY_ENABLED:-false}
PROXY_HOST=${PROXY_HOST:-localhost}
PROXY_PORT=${PROXY_PORT:-10809}

MYSQL_USERNAME=root
MYSQL_PASSWORD=Hg19951030@  # 修复MySQL密码
OKX_API_KEY=ca940079-e4ea-4839-b47e-97bbe4cc3267
OKX_SECRET_KEY=88022A3C11CEB4E2561985777C88ACD6
OKX_PASSPHRASE=Hg19951030@
DEEPSEEK_API_KEY=sk-a1d79a988f694e52a06d3b0ef97d1742





```md

TradingView





https://github.com/Drakkar-Software/OctoBot
免费开源加密货币交易机器人，可在 Binance、Hyperliquid 和 15+ 个交易所自动执行 AI、Grid、DCA 和 TradingView 策略，界面简洁易用。


https://github.com/QuantForgeOrg/PineTS   (https://quantforgeorg.github.io/PineTS/
PineTS 是一个独立项目，与 TradingView 或 Pine Script™ 无任何关联，亦未获得其认可或支持。文中提及的所有商标和注册商标均属于其各自所有者。
PineTS 是一个运行时库和转译器，它将 Pine Script（或 PineTS 语法）转换为可执行的 JavaScript。它在保留原始功能和行为的同时，还能稳健地处理时间序列数据、技术分析计算以及 Pine Script 独特的作用域机制。


https://github.com/roman-collinge/TradingBridge
TradingBridge 提供了一个模块化的系统化交易自动化流程。
TradingView 收到警报后，会通过 webhook 调用 AWS Lambda，该函数会根据您配置的策略验证信号，确定交易方向和规模，并通过您的经纪商集成执行订单。




https://github.com/danieltonad/capital-hook
Capital Hook 是一款功能强大的自托管 FastAPI 应用，专为算法交易者打造。它连接TradingView 的提醒功能（信号、筛选器、Pine Script 策略）和 Capital.com 的 CFD/外汇执行 API，使您能够根据自身策略自动执行交易。告别手动交易，利用自动化即时把握市场机遇！


https://github.com/TradeFlamo/flamotrade
✔ 单账户 Binance 永续合约U本位自动下单

TradingView → Webhook → FlamoTrade Lite → Binance 市价成交。
✔ 买入 / 卖出 / 平仓

支持最常用的轻量交易指令。
✔ Telegram 推送

每笔订单的执行状态、成功/失败会自动推送至 Telegram。
✔ 极轻量架构

无数据库 无后台管理 仅一个配置文件 最适合部署在 5 美元的 VPS 上运行。
✔ 纯本地运行，安全

你自己的 API Key 只保留在你自己 VPS 的配置文件中，永不外传泄露。



1. 具备 vps+域名（如 abc.xyz）并设置 api.abc.xyz 指向此 vps 的 IP
2. 在 caddy 配置文件中设置 api.abc.xpz 反向代理到 http://127.0.0.1:7000
3. 下载二进制:https://github.com/TradeFlamo/flamotrade/releases
4. 编辑同目录下的 flamoconfig.json: 填入：
    * tradeTunnel （TradingView警报里填的值的要与此处相同！）
    * Binance API KEY / SECRET
    * Telegram BOT Token / Chat ID
5. 启动 webhook 服务: ./flamotrade-lite
6. 将 vps的 IP 地址写入 Binance 的 API 容许访问列表中
7. 浏览器查看系统自动生成的接口文档 本地运行服务输入http://127.0.0.1:7000/docs vps 运行服务输入https://api.abc.xyz/docs
8. TradingView 警报配置: Webhook URL: https://api.abc.xyz/buySell（或另一接口 closePosition） 警报内容:两种json格式: <键名要与下面完全相同。值全为字符串，大小写均可，但tradeTunnel除外，其值大小写敏感>


警报买卖json
{
  "symbol": "ETHUSDT",             # 可以是Binance永续合约上线的其它加密币
  "side": "BUY",                   # 可用:buy/sell
  "amount": "1.5",                 # 买卖数量
  "usdt": "100",                   # 买卖所用的usdt。amount为0时使用
  "multiple": "0.3*5",             # 账户可用usdt的比例*当前杠杆。amount与usdt均为0时使用
  "price": "0",                    # 买卖价格。市价单不用此值
  "orderType": "market",           # 也可limit。但Lite免费版不支持limit，pro版支持
  "cancelLast": "false",           # 也可true。是否取消此前的所有买卖挂单(非止盈止损单)
  "closeLast": "reverse",          # 可用:true/false/reverse。true为下此单前市价平掉所有持仓，reverse为下此单前只市价平相反方向的持仓
  "reduceOnly": "false",           # 也可true。是否只对持仓减仓。用它与closeLast的reverse实现同方向多次下单只执行第一次下单
  "tradeTunnel": "tunnel password" # 为TradingView警报json传输安全而设计。应与配置文件中完全相同，否则不接受下单
}

警报平仓json
{
  "symbol": "ETHUSDT",
  "side": "CLOSEBUY",              # 可用:closeBuy/closeSell/x。X为平掉任何方向的持仓
  "amount": "0",
  "ratio": "1.0",                  # 当前持仓的比例。amount为0时使用
  "price": "{{close}}*1.01",
  "orderType": "limit",            # 也可market，Lite免费版都支持
  "cancelLast": "true",
  "tradeTunnel": "tunnel password"
}

# 📦 配置文件flamoconfig.json示例
{
  "tradeTunnel":"tunnel password",
  "binance": {
    "api_key": "YOUR_KEY",
    "api_secret": "YOUR_SECRET"
  },
  "telegram": {
    "token": "YOUR_TELEGRAM_BOT_TOKEN",
    "chat_id": "123456"
  }
}






Oks
Password M.L7LFpveLs

Email     joanne688@deepmails.org
Link https://www.linshiyouxiang.net/mail/view/29855df420f4626ef48f897094097496

Sms
Phone Number in Poland +48573583279
Link https://smstome.com/poland/phone/48573583279/sms/13549

Api keys
9WVku96XdasrC9u.
{"apiKey":"b81e589c-864a-4401-bedf-819608d0c51f","secretKey":"F4B5AD175F7AE9457048ED8E39BDB91D","API name":"test","IP":"0.0.0.0","Permissions":"只读, 提现, 交易"}




```




```json
{
  "arg": {
    "channel": "tickers",
    "instId": "BTC-USDT"
  },
  "data": [
    {
      "instType": "SPOT",
      "instId": "BTC-USDT",
      "last": "88522.2",
      "lastSz": "0.00005003",
      "askPx": "88522.5",
      "askSz": "0.80132329",
      "bidPx": "88522.1",
      "bidSz": "0.40229882",
      "open24h": "87626.3",
      "high24h": "89400",
      "low24h": "86825.6",
      "sodUtc0": "87232.1",
      "sodUtc8": "89000.6",
      "volCcy24h": "365905262.8290889",
      "vol24h": "4160.79359168",
      "ts": "1767115323510"
    }
  ]
}
```




```shell

IP: 118.107.26.215
用户名:root
密码:slPDvjjs3EaB
端口:21974

#OKX_API_KEY=c2af6981-07ca-4dc1-8996-8ef4553a310f;OKX_PASSPHRASE=.9WVku96XdasrC9u;OKX_SECRET_KEY=9643C5511388B03F1BEF06115B709F30

#OKX_API_KEY=af02b980-793b-4a6c-9bdd-09750daff54f
#OKX_PASSPHRASE=9WVku96XdasrC9u.
#OKX_SECRET_KEY=E132E77F7190192DD6B3D85882A72BCA

```
```md

2026-01-11 16:32:39.879 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90766.6","90620","90766.6","17.87117828","1620799.403282585","1620799.403282585","0"]]}
2026-01-11 16:32:39.879 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90766.6,"high":90766.6,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1620799.403282585,"state":0,"symbol":"BTC-USDT","volCcy":1620799.403282585,"volume":17.87117828}
2026-01-11 16:32:42.623 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90766.6","90620","90766.5","17.87158003","1620835.86872396","1620835.86872396","0"]]}
2026-01-11 16:32:42.624 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90766.5,"high":90766.6,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1620835.86872396,"state":0,"symbol":"BTC-USDT","volCcy":1620835.86872396,"volume":17.87158003}
2026-01-11 16:32:44.187 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90766.6","90620","90766.6","17.89146703","1622640.94409816","1622640.94409816","0"]]}
2026-01-11 16:32:44.187 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90766.6,"high":90766.6,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1622640.94409816,"state":0,"symbol":"BTC-USDT","volCcy":1622640.94409816,"volume":17.89146703}
2026-01-11 16:32:45.172 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90766.6","90620","90766.6","18.00984094","1633385.341437566","1633385.341437566","0"]]}
2026-01-11 16:32:45.173 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90766.6,"high":90766.6,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1633385.341437566,"state":0,"symbol":"BTC-USDT","volCcy":1633385.341437566,"volume":18.00984094}
2026-01-11 16:32:46.130 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90766.6","90620","90766.6","18.01635494","1633976.595069966","1633976.595069966","0"]]}
2026-01-11 16:32:46.132 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90766.6,"high":90766.6,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1633976.595069966,"state":0,"symbol":"BTC-USDT","volCcy":1633976.595069966,"volume":18.01635494}
2026-01-11 16:32:47.032 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90770","90620","90770","18.27128679","1657116.008694213","1657116.008694213","0"]]}
2026-01-11 16:32:47.032 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90770,"high":90770,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1657116.008694213,"state":0,"symbol":"BTC-USDT","volCcy":1657116.008694213,"volume":18.27128679}
2026-01-11 16:32:48.009 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90776.9","90620","90776.9","18.31245","1660852.540031586","1660852.540031586","0"]]}
2026-01-11 16:32:48.009 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90776.9,"high":90776.9,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1660852.540031586,"state":0,"symbol":"BTC-USDT","volCcy":1660852.540031586,"volume":18.31245}
2026-01-11 16:32:49.206 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90777","90620","90777","18.41346277","1670022.176152599","1670022.176152599","0"]]}
2026-01-11 16:32:49.206 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90777,"high":90777,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1670022.176152599,"state":0,"symbol":"BTC-USDT","volCcy":1670022.176152599,"volume":18.41346277}
2026-01-11 16:32:51.456 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90777","90620","90776.9","18.42773029","1671317.337404639","1671317.337404639","0"]]}
2026-01-11 16:32:51.457 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90776.9,"high":90777,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1671317.337404639,"state":0,"symbol":"BTC-USDT","volCcy":1671317.337404639,"volume":18.42773029}
2026-01-11 16:32:52.275 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90777","90620","90776.9","18.43490029","1671968.207777639","1671968.207777639","0"]]}
2026-01-11 16:32:52.277 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90776.9,"high":90777,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1671968.207777639,"state":0,"symbol":"BTC-USDT","volCcy":1671968.207777639,"volume":18.43490029}
2026-01-11 16:32:53.195 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90777","90620","90776.9","18.43668645","1672130.349959218","1672130.349959218","0"]]}
2026-01-11 16:32:53.195 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90776.9,"high":90777,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1672130.349959218,"state":0,"symbol":"BTC-USDT","volCcy":1672130.349959218,"volume":18.43668645}
2026-01-11 16:32:54.465 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90777","90620","90777","18.43673051","1672134.349593838","1672134.349593838","0"]]}
2026-01-11 16:32:54.465 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90777,"high":90777,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1672134.349593838,"state":0,"symbol":"BTC-USDT","volCcy":1672134.349593838,"volume":18.43673051}
2026-01-11 16:32:55.142 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: {"arg":{"channel":"candle4H","instId":"BTC-USDT"},"data":[["1768118400000","90712.9","90777","90620","90776.9","18.43743628","1672198.417206551","1672198.417206551","0"]]}
2026-01-11 16:32:55.143 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:{"channel":"candle4H","close":90776.9,"high":90777,"intervalVal":"4H","low":90620,"open":90712.9,"openTime":"2026-01-11 16:00:00","quoteVolume":1672198.417206551,"state":0,"symbol":"BTC-USDT","volCcy":1672198.417206551,"volume":18.43743628}

```
```md
2026-01-11 16:32:48.009 [OkHttp https://ws.okx.com:8443/...] INFO  wss.msg - <message.BUSINESS>: 
2026-01-11 16:32:48.009 [OkHttp https://ws.okx.com:8443/...] INFO  com.okx.trading.strategy.RealTimeStrategyManager - handleNewKlineData symbol:BTC-USDT interval:4H candlestick:

```

openTime open close low high volume quoteVolume quoteVolume volCcy

```json
[
  {
    "arg": {
      "channel": "candle4H",
      "instId": "BTC-USDT"
    },
    "data": [
      [
        "1768118400000",
        "90712.9",
        "90776.9",
        "90620",
        "90776.9",
        "18.31245",
        "1660852.540031586",
        "1660852.540031586",
        "0"
      ]
    ]
  },
  {
    "channel": "candle4H",
    "close": 90776.9,
    "high": 90776.9,
    "intervalVal": "4H",
    "low": 90620,
    "open": 90712.9,
    "openTime": "2026-01-11 16:00:00",
    "quoteVolume": 1660852.540031586,
    "state": 0,
    "symbol": "BTC-USDT",
    "volCcy": 1660852.540031586,
    "volume": 18.31245
  }
]

```





