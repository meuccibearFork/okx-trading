package com.okx.trading.util;

import cn.hutool.core.date.DateUtil;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 * 用于处理Java 8到Java 21之间的日期时间API差异
 */
public class DateTimeUtil {

    /**
     * 判断两个时间是否大于指定分钟数
     * @param date1 时间1
     * @param date2 时间2
     * @param minutes 分钟数
     * @param includeEqual 是否包含等于的情况
     * @return 是否大于（或大于等于）指定分钟数
     */
    public static boolean isGreaterThanMinutes(Date date1, Date date2,
                                               int minutes, boolean includeEqual) {
        if (date1 == null || date2 == null) {
            return false;
        }

        long betweenMs = DateUtil.betweenMs(date1, date2);
        long absMs = Math.abs(betweenMs);
        long thresholdMs = minutes * 60L * 1000L;  // 转换为毫秒

        if (includeEqual) {
            return absMs >= thresholdMs;
        } else {
            return absMs > thresholdMs;
        }
    }

    /**
     * 将Instant转换为LocalDateTime
     * 替代Java 8中的Instant.toLocalDateTime()方法
     *
     * @param instant 时间戳
     * @return LocalDateTime对象
     */
    public static LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.of("UTC+8")).toLocalDateTime();
    }

    /**
     * 将LocalDateTime转换为Instant
     *
     * @param localDateTime 本地日期时间
     * @return Instant对象
     */
    public static Instant localDateTimeToInstant(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.of("UTC+8")).toInstant();
    }

    /**
     * 将ZonedDateTime转换为Instant
     *
     * @param zonedDateTime 带时区的日期时间
     * @return Instant对象
     */
    public static Instant zonedDateTimeToInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant();
    }

    /**
     * 将Instant转换为ZonedDateTime
     *
     * @param instant 时间戳
     * @return ZonedDateTime对象
     */
    public static ZonedDateTime instantToZonedDateTime(Instant instant) {
        return instant.atZone(ZoneId.of("UTC+8"));
    }

    /**
     * 将毫秒时间戳转换为ZonedDateTime
     *
     * @param timestamp 毫秒时间戳
     * @return ZonedDateTime对象
     */
    public static ZonedDateTime timestampToZonedDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("UTC+8"));
    }

    /**
     * 将毫秒时间戳转换为LocalDateTime
     *
     * @param timestamp 毫秒时间戳
     * @return LocalDateTime对象
     */
    public static LocalDateTime timestampToLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("UTC+8")).toLocalDateTime();
    }

    /**
     * 格式化ZonedDateTime为字符串
     *
     * @param zonedDateTime 带时区的日期时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatZonedDateTime(ZonedDateTime zonedDateTime, String pattern) {
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取系统默认时区
     *
     * @return 系统默认时区
     */
    public static ZoneId getSystemDefaultZoneId() {
        return ZoneId.of("UTC+8");
    }
}
