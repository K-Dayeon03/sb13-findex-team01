package com.sb13.findex.sync.service;


import com.sb13.findex.indexdata.dto.CursorPageResponse;
import com.sb13.findex.sync.dto.request.SyncJobSearchCommand;
import com.sb13.findex.sync.dto.response.SyncJobDto;

public interface SyncJobService {
    CursorPageResponse<SyncJobDto> search(SyncJobSearchCommand command);
}
