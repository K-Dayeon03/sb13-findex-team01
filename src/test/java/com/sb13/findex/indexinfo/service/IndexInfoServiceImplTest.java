package com.sb13.findex.indexinfo.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import com.sb13.findex.sync.entity.SourceType;
import com.sb13.findex.sync.service.AutoSyncConfigService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.sb13.findex.sync.service.SyncJobReferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndexInfoServiceImplTest {

    @Mock
    AutoSyncConfigService autoSyncConfigService;

    @Mock
    IndexInfoRepository indexInfoRepository;

    @Mock
    IndexDataService indexDataService;

    @Mock
    SyncJobReferenceService syncJobReferenceService;

    @InjectMocks
    IndexInfoServiceImpl indexInfoService;

    @Test
    void delete_deletesIndexDataByIndexInfoId() {
        // given
        Long indexInfoId = 1L;

        IndexInfo indexInfo = IndexInfo.create(
                "KOSPI",
                "코스피",
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                SourceType.USER,
                false
        );

        given(indexInfoRepository.findById(indexInfoId))
                .willReturn(Optional.of(indexInfo));

        // when
        indexInfoService.delete(indexInfoId);

        // then
        verify(indexDataService).deleteByIndexInfoId(indexInfoId);
        verify(autoSyncConfigService).deleteByIndexInfoId(indexInfoId);
        verify(syncJobReferenceService).detachIndexInfo(indexInfoId);
        verify(indexInfoRepository).delete(indexInfo);
    }

    @Test
    void saveOrUpdateOpenApiInfo_upsertsOpenApiIndexInfoAndFindsSavedInfo() {
        // given
        IndexInfoCreateCommand command = new IndexInfoCreateCommand(
                " KOSPI ",
                " 코스피 ",
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                true
        );
        IndexInfo savedIndexInfo = IndexInfo.create(
                "KOSPI",
                "코스피",
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                SourceType.OPEN_API,
                false
        );
        given(indexInfoRepository.upsertOpenApiIndexInfo(
                "KOSPI",
                "코스피",
                100,
                LocalDate.of(1980, 1, 4),
                BigDecimal.valueOf(100),
                SourceType.OPEN_API.name()
        )).willReturn(1);
        given(indexInfoRepository.findByIndexClassificationAndIndexName("KOSPI", "코스피"))
                .willReturn(Optional.of(savedIndexInfo));

        // when
        indexInfoService.saveOrUpdateOpenApiInfo(command);

        // then
        verify(indexInfoRepository).upsertOpenApiIndexInfo(
                eq("KOSPI"),
                eq("코스피"),
                eq(100),
                eq(LocalDate.of(1980, 1, 4)),
                eq(BigDecimal.valueOf(100)),
                eq(SourceType.OPEN_API.name())
        );
        verify(indexInfoRepository).findByIndexClassificationAndIndexName("KOSPI", "코스피");
    }
}
