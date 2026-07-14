package com.sb13.findex.sync.dto.condition;

public record AutoSyncConfigSearchCondition(
        Long indexInfoId,   // 지수 조건
        Boolean enabled,    // 활성화 조건
        String cursor,
        Long idAfter,
        Integer size,
        String sortField,
        String sortDirection
) {
}