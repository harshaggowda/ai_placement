package com.careeros.common.util;

import com.careeros.common.constants.AppConstants;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Small, dependency-free helpers for working with time in a single canonical zone (UTC).
 *
 * <p>Centralizing time access (rather than scattering {@code Instant.now()} calls) keeps timestamps
 * consistent and makes time mockable in tests via an injected {@link Clock}.
 */
public final class DateTimeUtils {

    public static final ZoneId UTC = ZoneId.of(AppConstants.DEFAULT_ZONE);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private DateTimeUtils() {
        throw new AssertionError("No com.careeros.common.util.DateTimeUtils instances for you!");
    }

    public static Instant now() {
        return Instant.now();
    }

    public static Instant now(Clock clock) {
        return Instant.now(clock);
    }

    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(UTC);
    }

    public static String toIso(Instant instant) {
        return instant == null ? null : ISO.format(instant);
    }

    public static Instant parseIso(String value) {
        return value == null ? null : Instant.parse(value);
    }
}
