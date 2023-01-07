package com.turing.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @Project: SignWe
 * @Author: Paddi-Yan
 * @CreatedTime: 2023年01月07日 01:28:16
 */
public class TimeUtils {

    public static long getTimeInMillis(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }
}
