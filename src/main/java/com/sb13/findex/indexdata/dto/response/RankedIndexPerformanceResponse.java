package com.sb13.findex.indexdata.dto.response;
//성과 정보에 순위까지 붙인 랭킹 응답
public record RankedIndexPerformanceResponse(
        int rank,
        IndexPerformanceResponse performance
) {
}
