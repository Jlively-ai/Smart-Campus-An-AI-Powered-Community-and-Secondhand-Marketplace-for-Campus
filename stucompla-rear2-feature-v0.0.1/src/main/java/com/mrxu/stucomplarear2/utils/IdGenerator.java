package com.mrxu.stucomplarear2.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID生成器
 * 格式: 模块前缀(3) + YYYYMMDD(8) + 5位序号 = 16字符
 * 示例: USR2026052400001, PST2026052400001, ORD2026052400001
 */
public class IdGenerator {

    /** 模块前缀常量 */
    public static final String USER = "USR";
    public static final String ADMIN = "ADM";
    public static final String FOLLOW = "FLW";
    public static final String POST = "PST";
    public static final String POST_LIKE = "PLK";
    public static final String COLLECT = "COL";
    public static final String COMMENT = "CMT";
    public static final String GOODS = "GDS";
    public static final String GOODS_COMMENT = "GCM";
    public static final String CART = "CRT";
    public static final String ORDER = "ORD";
    public static final String LOGISTICS = "LOG";
    public static final String ORDER_REVIEW = "ORV";
    public static final String WALL = "WAL";
    public static final String ANNOUNCEMENT = "ANC";
    public static final String LETTER = "LTR";
    public static final String REPORT = "RPT";
    public static final String PUNISHMENT = "PSH";
    public static final String VIOLATION_DELETE = "VLT";
    public static final String IMAGE = "IMG";
    public static final String RECYCLE_BIN = "RCB";
    public static final String DAILY_STATS = "DST";
    public static final String WALL_LIKE = "WLK";
    public static final String CONTENT_SHARE = "CSH";

    /** 每个前缀独立的序号计数器 */
    private static final ConcurrentHashMap<String, PrefixCounter> counters = new ConcurrentHashMap<>();

    /**
     * 生成带模块前缀的ID
     * @param prefix 模块前缀，如 "USR", "PST", "ORD"
     * @return 格式: 前缀(3) + YYYYMMDD(8) + 序号(5) = 16字符
     */
    public static String generateId(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String key = prefix + dateStr;
        PrefixCounter counter = counters.computeIfAbsent(key, k -> new PrefixCounter());
        int seq = counter.incrementAndGet();
        return prefix + dateStr + String.format("%05d", seq);
    }

    /**
     * 兼容旧的无前缀调用，默认使用 "GEN" 前缀
     * @deprecated 请使用 {@link #generateId(String)} 并传入模块前缀
     */
    @Deprecated
    public static String generateId() {
        return generateId("GEN");
    }

    /** 每个前缀+日期组合的计数器 */
    private static class PrefixCounter {
        private final AtomicInteger sequence = new AtomicInteger(0);

        int incrementAndGet() {
            int seq = sequence.incrementAndGet();
            if (seq > 99999) {
                sequence.set(1);
                return 1;
            }
            return seq;
        }
    }
}
