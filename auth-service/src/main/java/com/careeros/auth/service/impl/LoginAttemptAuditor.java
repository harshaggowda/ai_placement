package com.careeros.auth.service.impl;

import com.careeros.auth.entity.LoginHistory;
import com.careeros.auth.repository.LoginHistoryRepository;
import com.careeros.auth.repository.UserRepository;
import com.careeros.auth.service.ClientInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists login attempts in their own transaction.
 *
 * <p>Uses {@code REQUIRES_NEW} so a failed-login audit row is committed even though the surrounding
 * login transaction rolls back when the {@code UnauthorizedException} is thrown. Kept in a separate
 * bean so the new-transaction semantics actually apply (self-invocation would bypass the proxy).
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptAuditor {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String email, ClientInfo client, boolean successful, String failureReason) {
        LoginHistory history = new LoginHistory();
        userRepository.findByEmail(email).ifPresent(history::setUser);
        history.setEmail(email);
        history.setIpAddress(client.ipAddress());
        history.setUserAgent(client.userAgent());
        history.setSuccessful(successful);
        history.setFailureReason(failureReason);
        loginHistoryRepository.save(history);
    }
}
