package com.careeros.common.constants;

/**
 * Platform-wide constants that are not environment-specific.
 *
 * <p>Environment-dependent values (URLs, credentials, TTLs) belong in configuration, not here.
 */
public final class AppConstants {

    private AppConstants() {
        throw new AssertionError("No com.careeros.common.constants.AppConstants instances for you!");
    }

    /** Base path prefix for all versioned REST APIs. */
    public static final String API_V1 = "/api/v1";

    /** Default page index used when a request omits pagination. */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    /** Default page size used when a request omits pagination. */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** Hard upper bound on page size to protect the database from unbounded reads. */
    public static final int MAX_PAGE_SIZE = 100;

    /** UTC is the single canonical timezone for all stored and computed timestamps. */
    public static final String DEFAULT_ZONE = "UTC";
}
