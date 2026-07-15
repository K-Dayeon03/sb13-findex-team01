package com.sb13.findex.sync.dto.condition;

public record AutoSyncConfigSearchCondition(
        Long indexInfoId,
        Boolean enabled,
        String cursor,
        Long idAfter,
        Integer size,
        String sortField,
        String sortDirection
) {
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    // size 미지정 시 기본값, 상한 초과 시 상한값으로 보정
    public int resolvedSize() {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}