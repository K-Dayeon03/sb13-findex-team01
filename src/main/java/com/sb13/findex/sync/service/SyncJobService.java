package com.sb13.findex.sync.service;


import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.sync.dto.request.SyncJobSearchCommand;
import com.sb13.findex.sync.dto.response.SyncJobDto;

import java.util.List;

public interface SyncJobService {
    CursorPageResponse<SyncJobDto> search(SyncJobSearchCommand command);

    List<SyncJobDto> indexDataSaveAll(List<IndexDataOpenApiCommand> dataOpenApiCommands, String worker);

    List<SyncJobDto> indexInfoSaveAll(List<IndexInfoCreateCommand> infoCreateCommands, String worker);

    int detachIndexInfo(Long indexInfoId);

}
