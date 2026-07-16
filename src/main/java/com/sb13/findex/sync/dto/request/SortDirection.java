package com.sb13.findex.sync.dto.request;

import com.sb13.findex.global.exception.InvalidRequestException;

public enum SortDirection{
    ASC,
    DESC;

    public static SortDirection from(String value) {
        if (value == null || value.isBlank()) {
            return ASC;
        }
        String trimmed = value.trim();

        for (SortDirection direction : values()) {
            if (direction.name().equalsIgnoreCase(trimmed)) {
                return direction;
            }
        }
        throw new InvalidRequestException("유효하지 않은 sortDirection 값입니다: " + value);
    }
}
