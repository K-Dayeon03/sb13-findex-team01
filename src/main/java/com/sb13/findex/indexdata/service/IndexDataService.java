package com.sb13.findex.indexdata.service;

import com.sb13.findex.indexdata.dto.CursorPageResponse;
import com.sb13.findex.indexdata.dto.IndexDataCreateRequest;
import com.sb13.findex.indexdata.dto.IndexDataResponse;
import com.sb13.findex.indexdata.dto.IndexDataSearchCondition;

public interface IndexDataService {

    Long createIndexData(IndexDataCreateRequest request);

    CursorPageResponse<IndexDataResponse> search(IndexDataSearchCondition condition);
    byte[] exportCsv(IndexDataSearchCondition condition);

}