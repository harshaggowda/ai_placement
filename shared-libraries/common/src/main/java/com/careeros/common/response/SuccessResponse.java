package com.careeros.common.response;

/**
 * Factory for success-case {@link ApiResponse} envelopes.
 *
 * <p>The platform uses a single response model — {@link ApiResponse} — for both success and error
 * outcomes. This class is the readable, success-side counterpart to {@link ErrorResponse}: it lets
 * controllers express intent as {@code SuccessResponse.of(dto)} while delegating entirely to
 * {@link ApiResponse} so there is no parallel response hierarchy to keep in sync.
 */
public final class SuccessResponse {

    private SuccessResponse() {
        throw new AssertionError("No com.careeros.common.response.SuccessResponse instances for you!");
    }

    /** Success envelope carrying a payload and the default message. */
    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.success(data);
    }

    /** Success envelope carrying a payload and a custom message. */
    public static <T> ApiResponse<T> of(T data, String message) {
        return ApiResponse.success(data, message);
    }

    /** Success envelope with a message and no payload (e.g. for command-style endpoints). */
    public static ApiResponse<Void> message(String message) {
        return ApiResponse.ok(message);
    }
}
