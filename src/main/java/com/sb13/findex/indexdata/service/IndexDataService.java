package com.sb13.findex.indexdata.service;

import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.dto.command.IndexDataUpdateCommand;
import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexdata.dto.command.IndexDataCreateCommand;
import com.sb13.findex.indexdata.dto.response.IndexDataResponse;
import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;

import java.io.Writer;

public interface IndexDataService {

    IndexDataResponse createIndexData(IndexDataCreateCommand command);

    IndexDataResponse updateIndexData(Long id, IndexDataUpdateCommand command);

    void deleteIndexData(Long id);

    // IndexInfo 삭제 시 동반되는 Cascade물리 삭제 처리, IndexInfo에서 사용
    void deleteByIndexInfoId(Long indexInfoId);

    CursorPageResponse<IndexDataResponse> search(IndexDataSearchCondition condition);
    void exportCsv(IndexDataSearchCondition condition, Writer writer);
    //서비스는 open-api 저장/갱신 기능도 제공
    void saveOrUpdateOpenApiData(IndexDataOpenApiCommand command);

}
