package com.sb13.findex.indexinfo.dto.response;

import com.sb13.findex.indexinfo.entity.SourceType;

import java.math.*;
import java.time.*;

// 지수 정보 목록 조회
public record IndexInfoResponse(
        Long id,
        String indexClassification,
        String indexName,
        int employedItemsCount,
        LocalDate basePointInTime,
        BigDecimal baseIndex,
        SourceType sourceType,
        boolean favorite
) {
}
