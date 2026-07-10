package com.sb13.findex.indexinfo.dto;

public record IndexInfoSearchRequest(
        String indexClassification,
        String indexName,
        Boolean favorite,
        String cursor,
        Long idAfter,
        Integer size,
        String sortField,
        String sortDirection

) {
}
