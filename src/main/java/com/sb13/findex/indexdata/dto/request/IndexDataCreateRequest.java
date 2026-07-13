package com.sb13.findex.indexdata.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCreateRequest(
    @NotNull(message = "지수 정보 ID는 필수입니다.")
    Long indexInfoId,

    @NotNull(message = "기준 일자는 필수입니다.")
    LocalDate baseDate,
    
    @NotNull BigDecimal marketPrice,
    @NotNull BigDecimal closingPrice,
    @NotNull BigDecimal highPrice,
    @NotNull BigDecimal lowPrice,
    @NotNull BigDecimal versus,
    @NotNull BigDecimal fluctuationRate,
    @NotNull Long tradingQuantity,
    @NotNull Long tradingPrice,
    @NotNull Long marketTotalAmount
) {
}