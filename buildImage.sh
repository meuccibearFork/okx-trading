#!/bin/bash
# ============================================================
# 跨平台（macOS/Ubuntu）Docker 多架构镜像构建脚本
# 所有配置均通过环境变量设置，支持从外部配置文件加载
# 默认配置文件路径：/deploy/config/buildConfig.sh
# 使用方法：
#   export VAR_NAME=value   # 直接设置
#   或
#   source /deploy/config/buildConfig.sh  # 手动加载配置
#   ./build.sh
# ============================================================

set -e  # 遇到错误立即退出

# ---------- 加载外部配置文件（如果存在） ----------
: "${CONFIG_FILE:=/deploy/config/buildConfig.sh}"  # 默认配置文件路径
if [ -f "$CONFIG_FILE" ]; then
    echo "加载配置文件: $CONFIG_FILE"
    source "$CONFIG_FILE"
fi

# ---------- 配置项（全部从环境变量读取，提供默认值）----------
# Dockerfile 路径
: "${DOCKERFILE_PATH:="./Dockerfile"}"
# buildx 构建器名称
: "${BUILDER_NAME:="trading-api-builder"}"
# socat 监听端口（仅 macOS）
: "${SOCAT_PORT:="2375"}"

# 构建器清理开关 (true/false)
: "${CLEAN_BUILDER:="true"}"

# 目标平台列表，多个平台用逗号分隔
: "${PLATFORMS:="linux/amd64"}"
#: "${PLATFORMS:="linux/amd64,linux/arm64"}"

# 项目名称
: "${PROJECT_NAME:="okx-api"}"

# 基础版本号
: "${VERSION_BASE:="v3.8.13"}"
# 是否添加时间戳 (true/false)
: "${ADD_TIMESTAMP:="true"}"
# 时间戳格式
: "${TIMESTAMP_FORMAT:="%Y%m%d%H%M%S"}"

# 镜像仓库前缀列表（多个值用空格或逗号分隔）
# 例如： "crpi-xxx/dous/ harbor.idousong.com/dousong/"
#: "${IMAGE_PREFIXES:="harbor.idousong.com/dousong/"}"
#: "${IMAGE_PREFIXES:="crpi-2ht4o3mf8mfcxio7.cn-shenzhen.personal.cr.aliyuncs.com/dous/"}"
: "${IMAGE_PREFIXES:="zhuozhuang/"}"

# 内部仓库替换开关
: "${INTERNAL_REGISTRY:="false"}"
# 内部 Harbor 域名（替换目标）
#: "${INTERNAL_HARBOR_DOMAIN:="harborinternal.idousong.com"}"
: "${INTERNAL_HARBOR_DOMAIN:="harbor.idousong.com"}"

# BuildKit 基础镜像配置
: "${INTERNAL_BUILDKIT_IMAGE:="${INTERNAL_HARBOR_DOMAIN}/library/buildkit:buildx-stable-1"}"
: "${TARGET_BUILDKIT_IMAGE:="moby/buildkit:buildx-stable-1"}"
: "${VERSION}"
# ---------- 配置项结束 ----------

# ---------- 生成带时间戳的版本号 ----------
if [ "$ADD_TIMESTAMP" = "true" ]; then
    VERSION="${VERSION_BASE}-$(date +${TIMESTAMP_FORMAT})"
    echo "已启用时间戳，最终版本号：${VERSION}"
else
    VERSION="${VERSION_BASE}"
    echo "未启用时间戳，使用基础版本号：${VERSION}"
fi

# ---------- 切换到脚本所在目录 ----------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
echo "当前工作目录已切换至脚本所在目录：$SCRIPT_DIR"

# ---------- 开关：内部仓库替换 ----------
INTERNAL_MODE=false
if [ "${INTERNAL_REGISTRY}" = "true" ] || [ "${INTERNAL_REGISTRY}" = "1" ]; then
    INTERNAL_MODE=true
    echo "检测到环境变量 INTERNAL_REGISTRY=true，启用内部仓库替换模式"
fi

# 定义备份文件路径
BACKUP_FILE="${DOCKERFILE_PATH}.bak"

# 定义清理函数：恢复原始 Dockerfile
restore_dockerfile() {
    if [ -f "$BACKUP_FILE" ]; then
        mv "$BACKUP_FILE" "$DOCKERFILE_PATH"
        echo "已恢复原始 Dockerfile"
    fi
}

# 如果开启内部模式，则执行替换并设置 trap 恢复
if [ "$INTERNAL_MODE" = true ]; then
    if [ ! -f "$DOCKERFILE_PATH" ]; then
        echo "❌ 错误：当前目录下未找到 Dockerfile（路径：$DOCKERFILE_PATH）" >&2
        exit 1
    fi

    echo "正在备份并修改 Dockerfile：将 harbor.idousong.com 替换为 ${INTERNAL_HARBOR_DOMAIN}"
    # 使用 sed 直接修改原文件，并创建备份（兼容 Linux 和 macOS）
    sed -i.bak "s/harbor\.idousong\.com/${INTERNAL_HARBOR_DOMAIN}/g" "$DOCKERFILE_PATH"
    # 设置 trap 在任何退出时恢复
    trap restore_dockerfile EXIT
    echo "Dockerfile 已修改，将在脚本退出时自动恢复"
fi

# ---------- 检测操作系统并配置 Docker 环境 ----------
OS="$(uname -s)"
case "$OS" in
    Darwin)
        echo "检测到 macOS 系统，准备配置 socat 代理..."
        # 检查 $SOCAT_PORT 端口是否已被监听（无论进程名）
        if lsof -i :${SOCAT_PORT} -t &>/dev/null; then
            echo "端口 ${SOCAT_PORT} 已被监听，socat 代理已在运行"
        else
            echo "启动 socat 代理（将 /var/run/docker.sock 映射到 tcp://127.0.0.1:${SOCAT_PORT}）..."
            nohup socat TCP-LISTEN:${SOCAT_PORT},range=127.0.0.1/32,reuseaddr,fork UNIX-CLIENT:/var/run/docker.sock &> /dev/null &
            # 等待一小段时间确保 socat 完成端口绑定
            sleep 2
            if lsof -i :${SOCAT_PORT} -t &>/dev/null; then
                echo "socat 代理启动成功"
            else
                echo "⚠️  [警告] socat 代理可能启动失败，请手动检查" >&2
            fi
        fi
        # 设置 Docker 主机为 TCP 端口
        export DOCKER_HOST=tcp://127.0.0.1:${SOCAT_PORT}
        echo "DOCKER_HOST 已设置为 $DOCKER_HOST"
        ;;
    Linux)
        echo "检测到 Linux 系统，使用默认 Docker 环境（无需代理）"
        # 在 Linux 下无需修改 DOCKER_HOST，保持默认 unix socket
        ;;
    *)
        echo "❌ 不支持的操作系统：$OS" >&2
        exit 1
        ;;
esac

# ---------- 检查 BuildKit 基础镜像 ----------
echo "检查 BuildKit 基础镜像：${TARGET_BUILDKIT_IMAGE}"
# 最终用于构建器的镜像名（优先使用本地存在的）
BUILDKIT_IMAGE_FOR_BUILDER=""

if docker image inspect "${TARGET_BUILDKIT_IMAGE}" &>/dev/null; then
    echo "镜像 ${TARGET_BUILDKIT_IMAGE} 已存在，将直接使用本地镜像"
    BUILDKIT_IMAGE_FOR_BUILDER="${TARGET_BUILDKIT_IMAGE}"
else
    echo "镜像 ${TARGET_BUILDKIT_IMAGE} 不存在，尝试从内部仓库拉取 ${INTERNAL_BUILDKIT_IMAGE} ..."
    if docker pull "${INTERNAL_BUILDKIT_IMAGE}"; then
        echo "拉取成功，正在标记为 ${TARGET_BUILDKIT_IMAGE}"
        docker tag "${INTERNAL_BUILDKIT_IMAGE}" "${TARGET_BUILDKIT_IMAGE}"
        BUILDKIT_IMAGE_FOR_BUILDER="${TARGET_BUILDKIT_IMAGE}"
    else
        echo "⚠️  拉取内部 BuildKit 镜像失败，将使用标准镜像名（构建器创建时会尝试联网拉取）" >&2
        BUILDKIT_IMAGE_FOR_BUILDER="${TARGET_BUILDKIT_IMAGE}"
    fi
fi

# ---------- 检查并创建 buildx 构建器（支持清理） ----------
echo "检查 buildx 构建器 ${BUILDER_NAME}..."

if [ "$CLEAN_BUILDER" = "true" ]; then
    # 清理模式：如果存在则删除，然后重新创建
    if docker buildx inspect ${BUILDER_NAME} &>/dev/null; then
        echo "清理模式：删除已存在的构建器 ${BUILDER_NAME}..."
        docker buildx rm -f ${BUILDER_NAME}
    fi
    echo "创建并使用新的 buildx 构建器 ${BUILDER_NAME} (driver=docker-container, image=${BUILDKIT_IMAGE_FOR_BUILDER})..."
    docker buildx create --use --name ${BUILDER_NAME} \
        --driver docker-container \
        --driver-opt image=${BUILDKIT_IMAGE_FOR_BUILDER}
else
    # 非清理模式：存在则使用，不存在则创建
    if docker buildx inspect ${BUILDER_NAME} &>/dev/null; then
        echo "构建器 ${BUILDER_NAME} 已存在，确保其为当前使用构建器"
        docker buildx use ${BUILDER_NAME}
    else
        echo "创建并使用新的 buildx 构建器 ${BUILDER_NAME} (driver=docker-container, image=${BUILDKIT_IMAGE_FOR_BUILDER})..."
        docker buildx create --use --name ${BUILDER_NAME} \
            --driver docker-container \
            --driver-opt image=${BUILDKIT_IMAGE_FOR_BUILDER}
    fi
fi

# ---------- 将 IMAGE_PREFIXES 字符串拆分为数组 ----------
# 支持逗号或空格作为分隔符
OLD_IFS="$IFS"
if [[ "$IMAGE_PREFIXES" == *","* ]]; then
    IFS=',' read -ra PREFIX_ARRAY <<< "$IMAGE_PREFIXES"
else
    IFS=' ' read -ra PREFIX_ARRAY <<< "$IMAGE_PREFIXES"
fi
IFS="$OLD_IFS"

# 构建完整的镜像标签列表
IMAGE_TAGS=()
for prefix in "${PREFIX_ARRAY[@]}"; do
    # 去除前后空格
    prefix="$(echo -e "${prefix}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
    [ -z "$prefix" ] && continue
    # 确保前缀以斜杠结尾
    if [[ "$prefix" != */ ]]; then
        prefix="${prefix}/"
    fi
    IMAGE_TAGS+=("${prefix}${PROJECT_NAME}:${VERSION}")
done

# ---------- 执行多架构镜像构建并推送 ----------
# 构建 docker buildx 命令基础部分
BUILD_CMD="docker buildx build"

# 如果 PLATFORMS 不为空，添加 --platform 参数
if [ -n "$PLATFORMS" ]; then
    BUILD_CMD+=" --platform ${PLATFORMS}"
    echo "目标平台：${PLATFORMS}"
else
    echo "未指定平台，将构建默认架构"
fi

# 添加所有镜像标签
for tag in "${IMAGE_TAGS[@]}"; do
    BUILD_CMD+=" -t ${tag}"
done

# 添加推送和构建上下文
BUILD_CMD+=" -f ./Dockerfile.prod --push ."

echo "开始执行构建命令："
echo "$BUILD_CMD"
# 执行命令
eval $BUILD_CMD

echo "清理模式：删除已存在的构建器 ${BUILDER_NAME}... docker buildx rm -f trading-api-builder"
docker buildx rm -f ${BUILDER_NAME}

echo "✅ 全部任务完成！"
echo "${VERSION}"
