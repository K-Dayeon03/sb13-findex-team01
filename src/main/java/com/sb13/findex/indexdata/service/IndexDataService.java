package com.sb13.findex.indexdata.service;

import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexdata.dto.command.IndexDataCreateCommand;
import com.sb13.findex.indexdata.dto.response.IndexDataResponse;
import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;

public interface IndexDataService {

    IndexDataResponse createIndexData(IndexDataCreateCommand command);

    CursorPageResponse<IndexDataResponse> search(IndexDataSearchCondition condition);
    byte[] exportCsv(IndexDataSearchCondition condition);

    //서비스는 open-api 저장/갱신 기능도 제공
    void saveOrUpdateOpenApiData(IndexDataOpenApiCommand command);

}
