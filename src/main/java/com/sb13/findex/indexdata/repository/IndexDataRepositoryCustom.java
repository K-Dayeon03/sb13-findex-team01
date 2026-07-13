package com.sb13.findex.indexdata.repository;

import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;
import com.sb13.findex.indexdata.entity.IndexData;

import java.util.List;

public interface IndexDataRepositoryCustom {
    //search()는 목록 조회용이라 size + 1개를 조회해서 hasNext 판단
    //searchForExport()는 CSV용이라 페이지네이션 없이 전체 조회.
    //요청 size가 10이면 실제로는 11개를 조회해 조회 결과가 11개면 true 아니면 false
    //다음 페이지가 있는지 확인
    List<IndexData> search(IndexDataSearchCondition condition);

    long count(IndexDataSearchCondition condition);

    List<IndexData> searchForExport(IndexDataSearchCondition condition);
}
