package com.sb13.findex.indexdata.repository;

import com.sb13.findex.indexdata.entity.IndexData;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
//기본적인 저장/조회는 JpaRepository를 사용하고,
//조건 검색·정렬·커서 페이지네이션처럼 복잡한 조회는 Custom Repository로 분리

// 특정 지수의 해당 날짜 데이터가 이미 존재하는지 확인
@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long>, IndexDataRepositoryCustom{
 //index_data테이블에서 지수 데이터를 찾기 위한 메서드
  Optional<IndexData> findByIndexInfo_IdAndBaseDate(Long indexInfoId, LocalDate baseDate);

  // 특정 IndexInfo에 종속된 모든 지수 데이터를 DB에서 일괄 삭제
  @Modifying
  @Query("DELETE FROM IndexData i WHERE i.indexInfo.id = :indexInfoId")
  void deleteAllByIndexInfo_Id(@Param("indexInfoId") Long indexInfoId);

  boolean existsByIndexInfo_IdAndBaseDate(Long indexInfoId, LocalDate baseDate);
}
