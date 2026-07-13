package com.sb13.findex.indexdata.dto.response;

import com.sb13.findex.indexdata.entity.IndexType;

import java.math.BigDecimal;
import java.time.LocalDate;
/*
* 조회 결과에는 지수 데이터 값뿐 아니라 지수 분류명과 지수명도 필요하므로,
* IndexData와 연관된 IndexInfo의 일부 필드도 응답 DTO에 포함
* */
public record IndexDataResponse (
        Long id,
        Long indexInfoId,
        String indexClassification,
        String indexName,
        LocalDate baseDate,
        IndexType sourceType,
        BigDecimal marketPrice,
        BigDecimal closingPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        Long tradingQuantity,
        Long tradingPrice,
        Long marketTotalAmount
){}
