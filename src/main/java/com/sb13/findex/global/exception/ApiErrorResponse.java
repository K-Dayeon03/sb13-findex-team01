package com.sb13.findex.global.exception;

import java.time.*;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String details
) {
}
