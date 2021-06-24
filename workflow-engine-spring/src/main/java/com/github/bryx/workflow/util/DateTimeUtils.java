package com.github.bryx.workflow.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils extends org.apache.commons.lang3.time.DateUtils{

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd）
     */
    public static String getDate() {
        return getDate("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    /**
     * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, String... pattern) {
        String formatDate = null;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    /**
     * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前时间字符串 格式（HH:mm:ss）
     */
    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    /**
     * 得到当前日期和时间字符串 格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前年份字符串 格式（yyyy）
     */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /**
     * 得到当前月份字符串 格式（MM）
     */
    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    /**
     * 得到当天字符串 格式（dd）
     */
    public static String getDay() {
        return formatDate(new Date(), "dd");
    }

    /**
     * 得到当前星期字符串 格式（E）星期几
     */
    public static String getWeek() {
        return formatDate(new Date(), "E");
    }

    /**
     *
     * 两个时间的比较，可指定时间单位
     * Author: James WU
     * @param endTime
     * @param startTime
     * @param timeUnit
     * return X = endTime - startTime (timeunit)
     * sample: 2021-1-2 12:12:12 - 2021-1-1 2:11:01 (TimeUnit.DAYS) = 1L
     */
    public static long timeBetween(Date endTime, Date startTime, TimeUnit timeUnit) {
        Instant startInstant = Instant.ofEpochMilli(startTime.getTime());
        Instant endInstant = Instant.ofEpochMilli(endTime.getTime());
        Duration duration = Duration.between(startInstant, endInstant);
        switch (timeUnit) {
            case NANOSECONDS:
                return duration.toNanos();
            case MILLISECONDS:
                return duration.toMillis();
            case SECONDS:
                return duration.getSeconds();
            case MINUTES:
                return duration.toMinutes();
            case HOURS:
                return duration.toHours();
            case DAYS:
                return duration.toDays();
            default:
                break;
        }
        return 0l;

    }
}
