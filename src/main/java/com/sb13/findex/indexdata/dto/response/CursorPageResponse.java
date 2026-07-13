package com.sb13.findex.indexdata.dto.response;

import java.util.List;
//커서 페이지 응답을 감싸는 DTO
public record CursorPageResponse<T>(
        List<T> content, // 실제 데이터 목록
        String nextCursor, //다음 페이지 요청에 사용할 cursor
        Long nextIdAfter, //다음 페이지 요청에 사용할 idAfter
        int size,// 이번 응답 데이터 개수
        long totalElements,  //현재 필터 조건에 맞는 전체 데이터 개수
        boolean hasNext //다음 페이지 존재 여부
) {
}
