package com.sb13.findex.indexinfo.service;

import com.sb13.findex.indexinfo.dto.command.*;
import com.sb13.findex.indexinfo.dto.response.*;

import java.util.*;

public interface IndexInfoService {

    // 지수 정보 목록 조회
    CursorPageResponse<IndexInfoResponse> search(
            IndexInfoSearchCondition condition
    );

    // 지수 정보 등록
    IndexInfoResponse create(
            IndexInfoCreateCommand command
    );

    // 지수 정보 상세 조회
    IndexInfoResponse findById(
            Long id
    );

    // 지수 정보 수정
    IndexInfoResponse update(
            Long id,
            IndexInfoUpdateCommand command
    );

    // 지수 정보 삭제
    void delete(
            Long id
    );

    // 지수 정보 요약 목록 조회
    List<IndexInfoSummaryResponse> findSummaries();

    // Open API 전용 저장/ 수정
    void saveOrUpdateOpenApiInfo(
            IndexInfoCreateCommand command
    );

}
