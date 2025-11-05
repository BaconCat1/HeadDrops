/*
 * HeadDrops Plugin
 * Created by @BaconCat1
 * Copyright (C) 2025 BaconCat1
 * Licensed under the GNU General Public License v3.0
 */

package org.bacon.headDrops.utils;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static ZoneId resolveZoneId(String zoneId, Logger logger) {
        if (zoneId == null || zoneId.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(zoneId);
        } catch (DateTimeException ex) {
            if (logger != null) {
                logger.warning("Invalid timezone '" + zoneId + "'. Falling back to system default.");
            }
            return ZoneId.systemDefault();
        }
    }

    public static DateTimeFormatter buildFormatter(String pattern, ZoneId zoneId, String fallbackPattern, Logger logger) {
        String effectivePattern = (pattern == null || pattern.isBlank()) ? fallbackPattern : pattern;
        try {
            return DateTimeFormatter.ofPattern(effectivePattern).withZone(zoneId);
        } catch (IllegalArgumentException ex) {
            if (logger != null) {
                logger.warning("Invalid timestamp format '" + effectivePattern + "'. Falling back to '" + fallbackPattern + "'.");
            }
            return DateTimeFormatter.ofPattern(fallbackPattern).withZone(zoneId);
        }
    }
}
