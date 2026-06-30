package com.careeros.common.util;

import com.careeros.common.constants.AppConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

/**
 * Builds safe {@link Pageable} instances from raw client input.
 *
 * <p>Clamps page size to {@link AppConstants#MAX_PAGE_SIZE} and normalizes negative indices so that
 * untrusted pagination parameters can never trigger unbounded or invalid queries.
 */
public final class PageRequestFactory {

    private PageRequestFactory() {
        throw new AssertionError("No com.careeros.common.util.PageRequestFactory instances for you!");
    }

    public static Pageable of(Integer page, Integer size) {
        return of(page, size, null, null);
    }

    public static Pageable of(Integer page, Integer size, String sortBy, Sort.Direction direction) {
        int safePage = (page == null || page < 0) ? AppConstants.DEFAULT_PAGE_NUMBER : page;
        int requestedSize = (size == null || size <= 0) ? AppConstants.DEFAULT_PAGE_SIZE : size;
        int safeSize = Math.min(requestedSize, AppConstants.MAX_PAGE_SIZE);

        if (StringUtils.hasText(sortBy)) {
            Sort.Direction dir = direction == null ? Sort.Direction.ASC : direction;
            return PageRequest.of(safePage, safeSize, Sort.by(dir, sortBy));
        }
        return PageRequest.of(safePage, safeSize);
    }
}
