package com.sb13.findex.indexdata.dto.command;

import com.sb13.findex.indexinfo.entity.IndexInfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataOpenApiCommand(
        //openAPI 저장용 command
        IndexInfo indexInfo,
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
){}
