package com.sb13.findex.sync.repository;

import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.sync.dto.AutoSyncTarget;
import com.sb13.findex.sync.entity.AutoSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long>, AutoSyncConfigRepositoryCustom {
    List<AutoSyncConfig> findByEnabled(boolean enabled);

    // 연관된 IndexInfo를 join fetch로 함께 조회 -> 이후 DTO 변환 시 추가 쿼리(N+1) 방지
    @Query("select a from AutoSyncConfig a join fetch a.indexInfo where a.id = :id")
    Optional<AutoSyncConfig> findByIdWithIndexInfo(@Param("id") Long id);

    // 중복 등록 방지
    boolean existsByIndexInfo(IndexInfo indexInfo);

    // 지수 삭제 시 연결된 자동 연동 설정도 함께 삭제 (유하정님 요청 - IndexInfo 삭제 서비스에서 호출)
    void deleteByIndexInfoId(Long indexInfoId);

    // 동시 요청 경쟁 상태 방지, DB 레벨 원자적 upsert
    @Modifying
    @Query(value = """
            INSERT INTO auto_sync_config (
                index_info_id,
                enabled,
                created_at,
                updated_at
            )
            VALUES (
                :indexInfoId,
                :enabled,
                now(),
                now()
            )
            ON CONFLICT (index_info_id)
            DO NOTHING
            """, nativeQuery = true)
    void upsertIfAbsent(@Param("indexInfoId") Long indexInfoId, @Param("enabled") boolean enabled);

    @Query("select new com.sb13.findex.sync.dto.AutoSyncTarget(a.indexInfo.id, a.lastSyncedDate) " +
            "from AutoSyncConfig a where a.enabled = :enabled")
    List<AutoSyncTarget> findEnabledSyncTargets(@Param("enabled") boolean enabled);

    @Modifying
    @Query("update AutoSyncConfig a set a.lastSyncedDate = :date where a.indexInfo.id in :indexInfoIds")
    void updateLastSyncedDate(@Param("indexInfoIds") List<Long> indexInfoIds, @Param("date") LocalDate date);

    // 활성화된 자동 연동 설정과, 연결된 지수에 저장된 IndexData의 최신 baseDate를 함께 조회합니다.
// LEFT JOIN이라 저장된 IndexData가 없는 지수도 결과에 포함되며, 이 경우 latestBaseDate는 null입니다.
// (배치 스케줄러가 지수별 마지막 저장 날짜를 기준으로 연동 시작일을 계산할 때 사용 — IndexData 도메인 변경 없이 자체 쿼리로 처리)
    @Query("select a.indexInfo.id as indexInfoId, max(d.baseDate) as latestBaseDate " +
            "from AutoSyncConfig a " +
            "left join IndexData d on d.indexInfo = a.indexInfo " +
            "where a.enabled = true " +
            "group by a.indexInfo.id")
    List<AutoSyncTargetProjection> findEnabledTargetsWithLatestBaseDate();
}