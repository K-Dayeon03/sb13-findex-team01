package com.sb13.findex.indexdata.dto.response;

import com.sb13.findex.indexdata.entity.IndexType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCsvRow(
        Long id,
        Long indexInfoId,
        String indexClassification,
        String indexName,
        LocalDate baseDate,
        IndexType indexType,
        BigDecimal marketPrice,
        BigDecimal closingPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        Long tradingQuantity,
        Long tradingPrice,
        Long marketTotalAmount
) {
}
