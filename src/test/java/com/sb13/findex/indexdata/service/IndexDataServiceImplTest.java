package com.sb13.findex.indexdata.service;

import com.sb13.findex.indexdata.repository.IndexDataRepository;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

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
}
