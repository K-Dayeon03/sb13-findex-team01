package com.sb13.findex.sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.entity.SourceType;
import com.sb13.findex.indexinfo.service.IndexInfoService;
import com.sb13.findex.sync.dto.command.IndexDataSyncJobTarget;
import com.sb13.findex.sync.dto.command.IndexInfoKey;
import com.sb13.findex.sync.repository.SyncJobRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.sb13.findex.sync.service.impl.SyncJobServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SyncJobServiceImplTest {

    @Mock
    SyncJobRepository syncJobRepository;

    @Mock
    IndexDataService indexDataService;

    @Mock
    IndexInfoService indexInfoService;

    @InjectMocks
    SyncJobServiceImpl syncJobService;

    @Test
    void indexInfoSaveAll_savesOpenApiIndexInfosAndCreatesSyncJobs() {
        // given
        String worker = "127.0.0.1";
        IndexInfoCreateCommand kospi = new IndexInfoCreateCommand(
                "KOSPI",
                "코스피",
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                false
        );
        IndexInfoCreateCommand kosdaq = new IndexInfoCreateCommand(
                "KOSDAQ",
                "코스닥",
                50,
                LocalDate.of(1996, 7, 1),
                BigDecimal.valueOf(1000),
                false
        );
        given(syncJobRepository.findBySyncExecutionId(any(UUID.class))).willReturn(List.of());

        // when
        syncJobService.indexInfoSaveAll(List.of(kospi, kosdaq), worker);

        // then
        verify(indexInfoService).saveOrUpdateOpenApiInfo(kospi);
        verify(indexInfoService).saveOrUpdateOpenApiInfo(kosdaq);

        ArgumentCaptor<List<IndexInfoKey>> keysCaptor = ArgumentCaptor.captor();
        verify(syncJobRepository).saveInfoAll(
                eq(worker),
                keysCaptor.capture(),
                any(UUID.class)
        );
        assertThat(keysCaptor.getValue())
                .containsExactly(
                        new IndexInfoKey("KOSPI", "코스피"),
                        new IndexInfoKey("KOSDAQ", "코스닥")
                );
    }

    @Test
    void indexDataSaveAll_savesOpenApiIndexDataAndCreatesSyncJobs() {
        // given
        String worker = "127.0.0.1";
        IndexInfo indexInfo = IndexInfo.create(
                "KOSPI",
                "코스피",
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                SourceType.OPEN_API,
                false
        );
        ReflectionTestUtils.setField(indexInfo, "id", 1L);

        IndexDataOpenApiCommand command = new IndexDataOpenApiCommand(
                indexInfo,
                LocalDate.of(2024, 7, 31),
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

        // when
        UUID executionId = UUID.randomUUID();
        syncJobService.indexDataSaveAll(List.of(command), worker, executionId);

        // then
        verify(indexDataService).saveOrUpdateOpenApiData(command);

        ArgumentCaptor<List<IndexDataSyncJobTarget>> targetsCaptor = ArgumentCaptor.captor();
        verify(syncJobRepository).saveDataAll(
                eq(worker),
                targetsCaptor.capture(),
                eq(executionId)
        );
        verify(syncJobRepository, never()).findBySyncExecutionId(any(UUID.class));
        assertThat(targetsCaptor.getValue())
                .containsExactly(new IndexDataSyncJobTarget(
                        1L,
                        LocalDate.of(2024, 7, 31),
                        "KOSPI",
                        "코스피"
                ));
    }
}
