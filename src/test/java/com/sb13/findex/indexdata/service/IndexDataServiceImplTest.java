package com.sb13.findex.indexdata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;
import com.sb13.findex.indexdata.dto.response.IndexDataCsvRow;
import com.sb13.findex.indexdata.entity.IndexData;
import com.sb13.findex.indexdata.entity.IndexType;
import com.sb13.findex.indexdata.repository.IndexDataRepository;
import com.sb13.findex.indexdata.service.impl.IndexDataServiceImpl;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import com.sb13.findex.indexinfo.entity.SourceType;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IndexDataServiceImplTest {
    @Mock
    IndexDataRepository indexDataRepository;

    @Mock
    IndexInfoRepository indexInfoRepository;

    @InjectMocks
    IndexDataServiceImpl indexDataService;

    @Test
    void deleteByIndexInfoId_deletesAllIndexDataByIndexInfoId() {
        // given
        Long indexInfoId = 1L;

        // when
        indexDataService.deleteByIndexInfoId(indexInfoId);

        // then
        verify(indexDataRepository).deleteAllByIndexInfo_Id(indexInfoId);
    }

    @Test
    void saveOrUpdateOpenApiData_savesNewOpenApiIndexData() {
        // given
        IndexInfo indexInfo = indexInfo(1L);
        IndexDataOpenApiCommand command = openApiCommand(indexInfo, LocalDate.of(2024, 7, 31), "2770.69");
        given(indexDataRepository.findByIndexInfo_IdAndBaseDate(1L, LocalDate.of(2024, 7, 31)))
                .willReturn(Optional.empty());

        // when
        indexDataService.saveOrUpdateOpenApiData(command);

        // then
        ArgumentCaptor<IndexData> indexDataCaptor = ArgumentCaptor.forClass(IndexData.class);
        verify(indexDataRepository).save(indexDataCaptor.capture());

        IndexData savedData = indexDataCaptor.getValue();
        assertThat(savedData.getIndexInfo()).isSameAs(indexInfo);
        assertThat(savedData.getBaseDate()).isEqualTo(LocalDate.of(2024, 7, 31));
        assertThat(savedData.getIndexType()).isEqualTo(IndexType.OPEN_API);
        assertThat(savedData.getClosingPrice()).isEqualByComparingTo("2770.69");
    }

    @Test
    void saveOrUpdateOpenApiData_updatesExistingOpenApiIndexData() {
        // given
        IndexInfo indexInfo = indexInfo(1L);
        IndexData existingData = IndexData.createOpenApiData(
                indexInfo,
                LocalDate.of(2024, 7, 31),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                1L,
                1L,
                1L
        );
        IndexDataOpenApiCommand command = openApiCommand(indexInfo, LocalDate.of(2024, 7, 31), "2770.69");
        given(indexDataRepository.findByIndexInfo_IdAndBaseDate(1L, LocalDate.of(2024, 7, 31)))
                .willReturn(Optional.of(existingData));

        // when
        indexDataService.saveOrUpdateOpenApiData(command);

        // then
        assertThat(existingData.getMarketPrice()).isEqualByComparingTo("2745.58");
        assertThat(existingData.getClosingPrice()).isEqualByComparingTo("2770.69");
        assertThat(existingData.getHighPrice()).isEqualByComparingTo("2770.70");
        assertThat(existingData.getLowPrice()).isEqualByComparingTo("2733.63");
        assertThat(existingData.getVersus()).isEqualByComparingTo("32.5");
        assertThat(existingData.getFluctuationRate()).isEqualByComparingTo("1.19");
        assertThat(existingData.getTradingQuantity()).isEqualTo(557090057L);
        assertThat(existingData.getTradingPrice()).isEqualTo(12197991898146L);
        assertThat(existingData.getMarketTotalAmount()).isEqualTo(2262832341048634L);
        verify(indexDataRepository, never()).save(org.mockito.ArgumentMatchers.any(IndexData.class));
    }

    @Test
    void exportCsvStreamsRowsAndEscapesUnsafeSpreadsheetCells() {
        // given
        IndexDataSearchCondition condition = new IndexDataSearchCondition(
                null,
                null,
                null,
                null,
                null,
                null,
                "baseDate",
                "desc"
        );
        IndexInfo indexInfo = indexInfo(1L, "=HYPERLINK", "@SUM\rKOSPI");
        IndexDataCsvRow indexData = indexDataCsvRow(indexInfo);
        StringWriter writer = new StringWriter();

        given(indexDataRepository.streamForExport(condition))
                .willReturn(Stream.of(indexData));

        // when
        indexDataService.exportCsv(condition, writer);

        // then
        assertThat(writer.toString())
                .contains("ID,지수정보ID,지수분류명,지수명")
                .contains(",'=HYPERLINK,\"'@SUM\rKOSPI\",");
    }

    private IndexInfo indexInfo(Long id) {
        return indexInfo(id, "KOSPI", "코스피");
    }

    private IndexInfo indexInfo(Long id, String indexClassification, String indexName) {
        IndexInfo indexInfo = IndexInfo.create(
                indexClassification,
                indexName,
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                SourceType.OPEN_API,
                false
        );
        ReflectionTestUtils.setField(indexInfo, "id", id);
        return indexInfo;
    }

    private IndexDataCsvRow indexDataCsvRow(IndexInfo indexInfo) {
        return new IndexDataCsvRow(
                1L,
                indexInfo.getId(),
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName(),
                LocalDate.of(2024, 7, 31),
                IndexType.OPEN_API,
                BigDecimal.valueOf(2745.58),
                BigDecimal.valueOf(2770.69),
                BigDecimal.valueOf(2770.70),
                BigDecimal.valueOf(2733.63),
                BigDecimal.valueOf(32.5),
                BigDecimal.valueOf(1.19),
                557090057L,
                12197991898146L,
                2262832341048634L
        );
    }

    private IndexDataOpenApiCommand openApiCommand(IndexInfo indexInfo, LocalDate baseDate, String closingPrice) {
        return new IndexDataOpenApiCommand(
                indexInfo,
                baseDate,
                BigDecimal.valueOf(2745.58),
                new BigDecimal(closingPrice),
                BigDecimal.valueOf(2770.70),
                BigDecimal.valueOf(2733.63),
                BigDecimal.valueOf(32.5),
                BigDecimal.valueOf(1.19),
                557090057L,
                12197991898146L,
                2262832341048634L
        );
    }
}
