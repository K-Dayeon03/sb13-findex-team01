package com.sb13.findex.sync.repository;

import com.sb13.findex.sync.entity.SyncJob;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>, SyncJobRepositoryCustom, SyncJobBulkRepository {

    @EntityGraph(attributePaths = {"indexInfo"})
    List<SyncJob> findBySyncExecutionId(UUID syncExecutionId);

    @Modifying
    @Query("""
                UPDATE SyncJob sj
                SET sj.indexInfo.id = null
                WHERE sj.indexInfo.id = :indexInfoId
            """)
    int clearIndexInfoReferenceByIndexInfoId(@Param("indexInfoId") Long indexInfoId);

}
