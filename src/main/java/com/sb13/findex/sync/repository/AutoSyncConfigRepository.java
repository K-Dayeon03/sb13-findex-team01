package com.sb13.findex.sync.repository;

import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.sync.entity.AutoSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long>, AutoSyncConfigRepositoryCustom {
    // 배치 스케줄러가 자동 연동 대상(활성화된 설정)만 조회할 때 사용
    List<AutoSyncConfig> findByEnabled(boolean enabled);

    // 연관된 IndexInfo를 join fetch로 함께 조회 -> 이후 DTO 변환 시 추가 쿼리(N+1) 방지
    @Query("select a from AutoSyncConfig a join fetch a.indexInfo where a.id = :id")
    Optional<AutoSyncConfig> findByIdWithIndexInfo(@Param("id") Long id);

    // 중복 등록 방지
    boolean existsByIndexInfo(IndexInfo indexInfo);

    // 지수 삭제 시 연결된 자동 연동 설정도 함께 삭제 (유하정님 요청 - IndexInfo 삭제 서비스에서 호출)
    void deleteByIndexInfoId(Long indexInfoId);
}