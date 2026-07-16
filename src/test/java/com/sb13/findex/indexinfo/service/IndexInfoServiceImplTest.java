package com.sb13.findex.indexinfo.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import com.sb13.findex.sync.entity.SourceType;
import com.sb13.findex.sync.service.AutoSyncConfigService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class IndexInfoServiceImplTest {

    @Mock
    IndexInfoRepository indexInfoRepository;

    @Mock
    AutoSyncConfigService autoSyncConfigService;

    @Mock
    IndexDataService indexDataService;

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
        verify(indexInfoRepository).delete(indexInfo);
    }
}