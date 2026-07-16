package com.sb13.findex.indexdata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sb13.findex.indexdata.dto.response.IndexChartResponse;
import com.sb13.findex.indexdata.dto.response.IndexPerformanceResponse;
import com.sb13.findex.indexdata.dto.response.RankedIndexPerformanceResponse;
import com.sb13.findex.indexdata.entity.ChartPeriodType;
import com.sb13.findex.indexdata.entity.IndexData;
import com.sb13.findex.indexdata.entity.IndexType;
import com.sb13.findex.indexdata.entity.UnitPeriodType;
import com.sb13.findex.indexdata.repository.IndexDataRepository;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.sync.entity.SourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DashboardIndexDataServiceTest {

    @Mock
    IndexDataRepository indexDataRepository;

    @InjectMocks
    DashboardIndexDataService dashboardIndexDataService;

    @Test
    void getFavoritePerformance_returnsPerformanceForFavoriteIndexes() {
        // given
        IndexInfo kospi = indexInfo(1L, "KOSPI", "코스피", true);
        IndexData currentData = indexData(kospi, LocalDate.of(2024, 7, 31), "110");
        IndexData beforeData = indexData(kospi, LocalDate.of(2024, 7, 30), "100");

        given(indexDataRepository.findLatestDataForFavoriteIndexes()).willReturn(List.of(currentData));
        given(indexDataRepository.findNearestDataOnOrBeforeByIndexInfoIds(
                Map.of(1L, LocalDate.of(2024, 7, 30))
        )).willReturn(List.of(beforeData));

        // when
        List<IndexPerformanceResponse> response =
                dashboardIndexDataService.getFavoritePerformance(UnitPeriodType.DAILY);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).indexInfoId()).isEqualTo(1L);
        assertThat(response.get(0).indexName()).isEqualTo("코스피");
        assertThat(response.get(0).versus()).isEqualByComparingTo("10");
        assertThat(response.get(0).fluctuationRate()).isEqualByComparingTo("10.0000");
    }

    @Test
    void getIndexChart_returnsDataPointsAndMovingAverages() {
        // given
        IndexInfo kospi = indexInfo(1L, "KOSPI", "코스피", true);
        LocalDate endDate = LocalDate.of(2024, 7, 31);
        IndexData latestData = indexData(kospi, endDate, "120");
        List<IndexData> chartData = java.util.stream.IntStream.rangeClosed(0, 44)
                .mapToObj(i -> indexData(
                        kospi,
                        endDate.minusDays(44L - i),
                        String.valueOf(100 + i)
                ))
                .toList();

        given(indexDataRepository.findNearestDataOnOrBefore(eq(1L), any(LocalDate.class)))
                .willReturn(Optional.of(latestData));
        given(indexDataRepository.findDataByIndexInfoIdAndBaseDateBetween(
                eq(1L),
                eq(endDate.minusMonths(1).minusDays(19)),
                eq(endDate)
        )).willReturn(chartData);

        // when
        IndexChartResponse response =
                dashboardIndexDataService.getIndexChart(1L, ChartPeriodType.MONTHLY);

        // then
        assertThat(response.indexInfoId()).isEqualTo(1L);
        assertThat(response.periodType()).isEqualTo("MONTHLY");
        assertThat(response.dataPoints()).isNotEmpty();
        assertThat(response.ma5DataPoints()).isNotEmpty();
        assertThat(response.ma20DataPoints()).isNotEmpty();
        assertThat(response.dataPoints().get(0).date()).isAfterOrEqualTo(endDate.minusMonths(1));
    }

    @Test
    void getIndexChart_calculatesMovingAverageValuesFromClosingPrices() {
        // given
        IndexInfo kospi = indexInfo(1L, "KOSPI", "코스피", true);
        LocalDate endDate = LocalDate.of(2024, 7, 31);
        LocalDate startDate = endDate.minusMonths(1);
        LocalDate movingAverageStartDate = startDate.minusDays(19);
        IndexData latestData = indexData(kospi, endDate, "51");

        List<IndexData> chartData = java.util.stream.IntStream.rangeClosed(0, 50)
                .mapToObj(i -> indexData(
                        kospi,
                        movingAverageStartDate.plusDays(i),
                        String.valueOf(i + 1)
                ))
                .toList();

        given(indexDataRepository.findNearestDataOnOrBefore(eq(1L), any(LocalDate.class)))
                .willReturn(Optional.of(latestData));
        given(indexDataRepository.findDataByIndexInfoIdAndBaseDateBetween(
                eq(1L),
                eq(movingAverageStartDate),
                eq(endDate)
        )).willReturn(chartData);

        // when
        IndexChartResponse response =
                dashboardIndexDataService.getIndexChart(1L, ChartPeriodType.MONTHLY);

        // then
        assertThat(response.dataPoints()).hasSize(32);
        assertThat(response.ma5DataPoints()).hasSize(32);
        assertThat(response.ma20DataPoints()).hasSize(32);

        assertThat(response.dataPoints().get(0).date()).isEqualTo(startDate);
        assertThat(response.dataPoints().get(0).value()).isEqualByComparingTo("20");

        assertThat(response.ma5DataPoints().get(0).date()).isEqualTo(startDate);
        assertThat(response.ma5DataPoints().get(0).value()).isEqualByComparingTo("18.0000");
        assertThat(response.ma20DataPoints().get(0).date()).isEqualTo(startDate);
        assertThat(response.ma20DataPoints().get(0).value()).isEqualByComparingTo("10.5000");

        assertThat(response.ma5DataPoints().get(31).date()).isEqualTo(endDate);
        assertThat(response.ma5DataPoints().get(31).value()).isEqualByComparingTo("49.0000");
        assertThat(response.ma20DataPoints().get(31).date()).isEqualTo(endDate);
        assertThat(response.ma20DataPoints().get(31).value()).isEqualByComparingTo("41.5000");
    }

    @Test
    void getIndexChart_calculatesMovingAveragesByDateRangeWhenDatesAreMissing() {
        // given
        IndexInfo kospi = indexInfo(1L, "KOSPI", "코스피", true);
        LocalDate endDate = LocalDate.of(2024, 7, 5);
        LocalDate startDate = endDate.minusMonths(1);
        LocalDate movingAverageStartDate = startDate.minusDays(19);
        IndexData latestData = indexData(kospi, endDate, "140");

        List<IndexData> chartData = List.of(
                indexData(kospi, LocalDate.of(2024, 7, 1), "100"),
                indexData(kospi, LocalDate.of(2024, 7, 3), "120"),
                indexData(kospi, LocalDate.of(2024, 7, 5), "140")
        );

        given(indexDataRepository.findNearestDataOnOrBefore(eq(1L), any(LocalDate.class)))
                .willReturn(Optional.of(latestData));
        given(indexDataRepository.findDataByIndexInfoIdAndBaseDateBetween(
                eq(1L),
                eq(movingAverageStartDate),
                eq(endDate)
        )).willReturn(chartData);

        // when
        IndexChartResponse response =
                dashboardIndexDataService.getIndexChart(1L, ChartPeriodType.MONTHLY);

        // then
        assertThat(response.dataPoints()).hasSize(3);
        assertThat(response.ma5DataPoints()).hasSize(3);
        assertThat(response.ma20DataPoints()).hasSize(3);

        assertThat(response.ma5DataPoints().get(0).date()).isEqualTo(LocalDate.of(2024, 7, 1));
        assertThat(response.ma5DataPoints().get(0).value()).isEqualByComparingTo("100.0000");
        assertThat(response.ma5DataPoints().get(1).date()).isEqualTo(LocalDate.of(2024, 7, 3));
        assertThat(response.ma5DataPoints().get(1).value()).isEqualByComparingTo("110.0000");
        assertThat(response.ma5DataPoints().get(2).date()).isEqualTo(LocalDate.of(2024, 7, 5));
        assertThat(response.ma5DataPoints().get(2).value()).isEqualByComparingTo("120.0000");

        assertThat(response.ma20DataPoints().get(2).date()).isEqualTo(LocalDate.of(2024, 7, 5));
        assertThat(response.ma20DataPoints().get(2).value()).isEqualByComparingTo("120.0000");
    }

    @Test
    void getPerformanceRank_returnsRankedPerformancesByFluctuationRate() {
        // given
        IndexInfo kospi = indexInfo(1L, "KOSPI", "코스피", true);
        IndexInfo kosdaq = indexInfo(2L, "KOSDAQ", "코스닥", true);

        IndexData kospiCurrent = indexData(kospi, LocalDate.of(2024, 7, 31), "110");
        IndexData kosdaqCurrent = indexData(kosdaq, LocalDate.of(2024, 7, 31), "130");
        IndexData kospiBefore = indexData(kospi, LocalDate.of(2024, 7, 30), "100");
        IndexData kosdaqBefore = indexData(kosdaq, LocalDate.of(2024, 7, 30), "100");

        given(indexDataRepository.findLatestDataForRanking(null))
                .willReturn(List.of(kospiCurrent, kosdaqCurrent));
        given(indexDataRepository.findNearestDataOnOrBeforeByIndexInfoIds(
                Map.of(
                        1L, LocalDate.of(2024, 7, 30),
                        2L, LocalDate.of(2024, 7, 30)
                )
        )).willReturn(List.of(kospiBefore, kosdaqBefore));

        // when
        List<RankedIndexPerformanceResponse> response =
                dashboardIndexDataService.getPerformanceRank(null, UnitPeriodType.DAILY, 2);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).rank()).isEqualTo(1);
        assertThat(response.get(0).performance().indexInfoId()).isEqualTo(2L);
        assertThat(response.get(1).rank()).isEqualTo(2);
        assertThat(response.get(1).performance().indexInfoId()).isEqualTo(1L);
    }

    private IndexInfo indexInfo(Long id, String classification, String name, boolean favorite) {
        IndexInfo indexInfo = IndexInfo.create(
                classification,
                name,
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                SourceType.OPEN_API,
                favorite
        );
        ReflectionTestUtils.setField(indexInfo, "id", id);
        return indexInfo;
    }

    private IndexData indexData(IndexInfo indexInfo, LocalDate baseDate, String closingPrice) {
        return IndexData.builder()
                .indexInfo(indexInfo)
                .baseDate(baseDate)
                .indexType(IndexType.OPEN_API)
                .marketPrice(new BigDecimal(closingPrice))
                .closingPrice(new BigDecimal(closingPrice))
                .highPrice(new BigDecimal(closingPrice))
                .lowPrice(new BigDecimal(closingPrice))
                .versus(BigDecimal.ZERO)
                .fluctuationRate(BigDecimal.ZERO)
                .tradingQuantity(1L)
                .tradingPrice(1L)
                .marketTotalAmount(1L)
                .build();
    }
}
