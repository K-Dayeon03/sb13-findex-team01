package com.sb13.findex.indexdata.dto.response;

import java.util.List;
//지수 차트 전체 응답
public record IndexChartResponse(
        Long indexInfoId,
        String indexClassification,
        String indexName,
        String periodType,
        List<ChartDataPointResponse> dataPoints, //그 날짜의 실제 종가
        List<ChartDataPointResponse> ma5DataPoints, //그 날짜 포함 5일 이동평균선 데이터
        List<ChartDataPointResponse> ma20DataPoints //그 날짜 포함 20일 이동평균선 데이터
) {
}
