package com.sb13.findex.autosyncconfig.repository;

import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.autosyncconfig.dto.projection.AutoSyncTargetProjection;
import com.sb13.findex.autosyncconfig.entity.AutoSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select a.indexInfo.id as indexInfoId, max(d.baseDate) as latestBaseDate " +
            "from AutoSyncConfig a " +
            "left join IndexData d on d.indexInfo = a.indexInfo " +
            "where a.enabled = true " +
            "group by a.indexInfo.id")
    List<AutoSyncTargetProjection> findEnabledTargetsWithLatestBaseDate();
}