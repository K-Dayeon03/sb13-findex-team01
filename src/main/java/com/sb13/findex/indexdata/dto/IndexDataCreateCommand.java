package com.sb13.findex.indexdata.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCreateCommand(
    Long indexInfoId,
    LocalDate baseDate,
    String sourceType,
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
  // Request -> Command
  public static IndexDataCreateCommand from(IndexDataCreateRequest request) {
    return new IndexDataCreateCommand(
        request.indexInfoId(),
        request.baseDate(),
        request.sourceType(),
        request.marketPrice(),
        request.closingPrice(),
        request.highPrice(),
        request.lowPrice(),
        request.versus(),
        request.fluctuationRate(),
        request.tradingQuantity(),
        request.tradingPrice(),
        request.marketTotalAmount()
    );
  }
}
