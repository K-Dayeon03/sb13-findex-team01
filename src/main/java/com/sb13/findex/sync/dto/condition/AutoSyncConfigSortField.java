package com.sb13.findex.sync.dto.condition;

import com.sb13.findex.sync.exception.InvalidSortFieldException;

import java.util.Arrays;

public enum AutoSyncConfigSortField {
    INDEX_INFO_ID("indexInfoId"),
    ENABLED("enabled");

    private final String value;

    AutoSyncConfigSortField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AutoSyncConfigSortField from(String value) {
        if (value == null || value.isBlank()) {
            return INDEX_INFO_ID;
        }
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(field -> field.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new InvalidSortFieldException(value));
    }
}