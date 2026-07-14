package com.sb13.findex.indexdata.repository;

import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;
import com.sb13.findex.indexdata.entity.IndexData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IndexDataRepositoryCustom {
    //search()는 목록 조회용이라 size + 1개를 조회해서 hasNext 판단
    //searchForExport()는 CSV용이라 페이지네이션 없이 전체 조회.
    //요청 size가 10이면 실제로는 11개를 조회해 조회 결과가 11개면 true 아니면 false
    //다음 페이지가 있는지 확인
    List<IndexData> search(IndexDataSearchCondition condition);



    long count(IndexDataSearchCondition condition);

    List<IndexData> searchForExport(IndexDataSearchCondition condition);

    //대시보드 조회 메서드

    //랭킹은 전체 지수 기준이면 최신 데이터가 있는 지수들을 가져옴
    //indexinfoid가 있으면 해당 지수만 필터링
    List<IndexData> findLatestDataForFavoriteIndexes();
    List<IndexData> findDataByIndexInfoIdAndBaseDateBetween(
            Long indexInfoId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<IndexData> findNearestDataOnOrBefore(
            Long indexInfoId,
            LocalDate targetDate);

    List<IndexData> findNearestDataOnOrBeforeByIndexInfoIds(
            Map<Long, LocalDate> targetDatesByIndexInfoId
    );

    //전체지수 기준으로 지수성과 분석 량킹
    List<IndexData> findLatestDataForRanking(Long indexInfoId);
}
