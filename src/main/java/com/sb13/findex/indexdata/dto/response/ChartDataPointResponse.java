package com.sb13.findex.indexdata.dto.response;

import java.time.LocalDate;
import java.math.BigDecimal;

public record ChartDataPointResponse(
        LocalDate date,
        BigDecimal value
) { }
