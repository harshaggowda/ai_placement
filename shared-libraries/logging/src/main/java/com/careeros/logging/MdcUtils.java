package com.careeros.logging;

import org.slf4j.MDC;

/**
 * Thin, null-safe helpers over the SLF4J {@link MDC} (mapped diagnostic context).
 *
 * <p>Centralizes the MDC key names and guards against storing {@code null} values. The correlation
 * id is owned by {@link CorrelationIdFilter}; this class exposes convenient read/write access to it
 * (and any future structured-logging keys) without scattering raw {@code MDC} calls across the code.
 */
public final class MdcUtils {

    /** MDC key holding the per-request correlation (trace) id. */
    public static final String TRACE_ID = CorrelationIdFilter.TRACE_ID_KEY;

    private MdcUtils() {
        throw new AssertionError("No com.careeros.logging.MdcUtils instances for you!");
    }

    /** Stores a value under {@code key}; no-op if {@code value} is {@code null}. */
    public static void put(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
    }

    /** Returns the value for {@code key}, or {@code null} if absent. */
    public static String get(String key) {
        return MDC.get(key);
    }

    /** Removes {@code key} from the context. */
    public static void remove(String key) {
        MDC.remove(key);
    }

    /** The current request's correlation (trace) id, or {@code null} if not yet set. */
    public static String traceId() {
        return MDC.get(TRACE_ID);
    }
}
