package com.wpits.customerbill.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaginationUtil {

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;

    /**
     * Converts TMF-style offset/limit query params into a Spring Data Pageable.
     * offset = starting record index (not page number), limit = page size.
     */
    public Pageable toPageable(Integer offset, Integer limit) {
        int safeLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        int safeOffset = (offset == null || offset < 0) ? DEFAULT_OFFSET : offset;

        int pageNumber = safeOffset / safeLimit;

        log.debug("Resolved pagination -> offset=[{}], limit=[{}] => page=[{}], size=[{}]",
                offset, limit, pageNumber, safeLimit);

        return PageRequest.of(pageNumber, safeLimit);
    }
}
