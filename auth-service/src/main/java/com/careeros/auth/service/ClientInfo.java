package com.careeros.auth.service;

/**
 * Transport-agnostic client metadata (captured by the controller from the HTTP request) used for
 * session tracking and login auditing. Keeps the service layer free of servlet types.
 */
public record ClientInfo(String ipAddress, String userAgent) {

    public static ClientInfo unknown() {
        return new ClientInfo(null, null);
    }
}
