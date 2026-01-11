# 使用Java 8作为基础镜像
#FROM --platform=linux/amd64 openjdk:8-jdk-alpine
#FROM openjdk:26-ea-21-jdk-oraclelinux8
FROM zhuozhuang/java:1.8-glibc

# 设置工作目录
WORKDIR /app

# 添加构建参数
ARG JAR_FILE=maven/*.jar
ARG PROXY_HOST=localhost
ARG PROXY_PORT=10809

# 安装必要的工具
RUN apk add --no-cache curl

# 创建存放日志的目录
RUN mkdir -p /var/log/okx-trading

# 设置环境变量
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m" \
    HTTP_PROXY_HOST=${PROXY_HOST} \
    HTTP_PROXY_PORT=${PROXY_PORT}

# 复制打包好的Jar文件到容器中
COPY ${JAR_FILE} app.jar

# 暴露应用程序端口
EXPOSE 8080

# 设置启动命令
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
