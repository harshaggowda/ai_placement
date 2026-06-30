package com.careeros.auth.service;

import com.careeros.auth.dto.UserSummaryDto;
import com.careeros.common.pagination.PaginationResponse;
import org.springframework.data.domain.Pageable;

/**
 * Read-side queries over users, used by administrative endpoints.
 */
public interface UserQueryService {

    /** Page through users as lightweight summaries. */
    PaginationResponse<UserSummaryDto> listUsers(Pageable pageable);
}
