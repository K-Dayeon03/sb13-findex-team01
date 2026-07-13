package com.sb13.findex.indexdata.dto.condition;

import java.time.LocalDate;
//“조회 조건을 한 묶음으로 담는 DTO”
public record IndexDataSearchCondition(
        Long indexInfoId,//지수 정보 ID
        LocalDate startDate, //시작 일자
        LocalDate endDate, //종료일자
        //커서 기반 페이지네이션에서 이전 페이지 마지막 데이터를 기준으로 다음 페이지를 조회하기 위해 사용
        String cursor,//다음 페이지 시작점
        Long idAfter,//이전 페이지 마지막 요소 ID
        Integer size, //페이지 크기 기본 설정 10
        String sortField, //정렬필드
        String sortDirection //정렬 방향 asc,desc
) {
}
