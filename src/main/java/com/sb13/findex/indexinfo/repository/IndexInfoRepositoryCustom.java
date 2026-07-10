package com.sb13.findex.indexinfo.repository;

import com.sb13.findex.indexinfo.entity.*;
import com.sb13.findex.indexinfo.dto.*;
import com.sb13.findex.indexinfo.entity.*;

import java.util.*;

public interface IndexInfoRepositoryCustom {

    // 검색 조건: 지수 분류명(부분 일치), 지수명(부분 일치), 즐겨찾기 여부(true/false)
    // 정렬 조건: 지수 분류명, 지수명, 채용 종목 수 중 하나를 선택해 정렬 할 수 있습니다.
    List<IndexInfo> searchIndexInfo(IndexInfoSearchRequest request);







}
