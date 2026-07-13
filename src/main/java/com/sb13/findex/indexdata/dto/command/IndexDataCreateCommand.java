package com.sb13.findex.indexdata.dto.command;

import com.sb13.findex.indexdata.dto.request.IndexDataCreateRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCreateCommand(
    Long indexInfoId,
    LocalDate baseDate,
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
