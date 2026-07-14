package com.sb13.findex.indexdata.dto.response;

import java.math.BigDecimal;
//성과 카드/랭킹에서 쓰는 지수 성과 정보
public record IndexPerformanceResponse(
        Long indexInfoId,
        String indexClassification,
        String indexName,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        BigDecimal currentPrice,
        BigDecimal beforePrice
) {
}
