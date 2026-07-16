package com.sb13.findex.indexinfo.repository;

import com.sb13.findex.global.exception.InvalidRequestException;
import java.util.Arrays;

public enum IndexInfoSortField {

    INDEX_CLASSIFICATION("indexClassification"),
    INDEX_NAME("indexName"),
    EMPLOYED_ITEMS_COUNT("employedItemsCount");

    private final String value;

    IndexInfoSortField(String value) {
        this.value = value;
    }

    public static IndexInfoSortField from(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return INDEX_NAME;
        }

        String cleanValue = value.strip();

        return Arrays.stream(values())
                .filter(sortField ->
                        sortField.value.equalsIgnoreCase(cleanValue)
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException(
                                "지원하지 않는 정렬 필드입니다: " + value
                        )
                );
    }
}
