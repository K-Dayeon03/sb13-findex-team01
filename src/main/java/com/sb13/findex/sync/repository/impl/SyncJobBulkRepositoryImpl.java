package com.sb13.findex.sync.repository.impl;

import com.sb13.findex.sync.dto.command.IndexDataSyncJobTarget;
import com.sb13.findex.sync.dto.command.IndexInfoKey;
import com.sb13.findex.sync.repository.SyncJobBulkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SyncJobBulkRepositoryImpl implements SyncJobBulkRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public int saveInfoAll(String worker, List<IndexInfoKey> indexInfoKeys, UUID uuid) {

        String sql = """
                INSERT INTO sync_job (
                                      index_info_id, 
                                      index_classification_snapshot,
                                      index_name_snapshot,
                                      job_type, 
                                      worker, 
                                      job_time, 
                                      result,
                                      sync_execution_id,
                                      created_at,
                                      updated_at
                                      ) 
                SELECT 
                info.id,
                req.index_classification,
                req.index_name,
                'INDEX_INFO',
                ?,
                COALESCE(info.updated_at, now()),
                CASE
                    WHEN info.id IS NULL THEN 'FAILED'
                    ELSE 'SUCCESS'
                END,
                ?,
                now(),
                now()
                FROM(
                    VALUES (?, ?)
                ) AS req(index_classification, index_name)
                LEFT JOIN index_info info ON info.index_classification = req.index_classification 
                    AND info.index_name = req.index_name
                """;

        int[][] updateResults = jdbcTemplate.batchUpdate(
                sql,
                indexInfoKeys,
                1000,
                (ps, key) -> {
                    ps.setString(1, worker);
                    ps.setObject(2, uuid);
                    ps.setString(3, key.indexClassification());
                    ps.setString(4, key.indexName());
                }
        );

        return Arrays.stream(updateResults)
                .flatMapToInt(Arrays::stream)
                .filter(count -> count > 0)
                .sum();

    }

    @Override
    public int saveDataAll(String worker, List<IndexDataSyncJobTarget> indexDataSyncJobTargets, UUID uuid) {

        String sql = """
                INSERT INTO sync_job (
                                      index_info_id,
                                      index_classification_snapshot,
                                      index_name_snapshot,
                                      job_type,
                                      worker,
                                      job_time,
                                      result,
                                      sync_execution_id,
                                      target_date,
                                      created_at,
                                      updated_at
                                      )
                SELECT
                    req.index_info_id,
                    req.index_classification,
                    req.index_name,
                    'INDEX_DATA',
                    ?,
                   COALESCE(data.updated_at, now()),
                    CASE
                        WHEN data.id IS NULL THEN 'FAILED'
                        ELSE 'SUCCESS'
                    END,
                    ?,
                    req.base_date,
                    now(),
                    now()
                FROM(
                    VALUES (?, CAST(? AS DATE), ?, ?)
                ) AS req(index_info_id, base_date, index_classification, index_name)
                LEFT JOIN index_data data ON data.index_info_id = req.index_info_id
                AND data.base_date = req.base_date
                """;

        int[][] result = jdbcTemplate.batchUpdate(
                sql,
                indexDataSyncJobTargets,
                1000, (ps, target) -> {
                    ps.setString(1, worker);
                    ps.setObject(2, uuid);
                    ps.setLong(3, target.indexInfoId());
                    ps.setDate(4, Date.valueOf(target.baseDate()));
                    ps.setString(5, target.indexClassification());
                    ps.setString(6, target.indexName());
                });

        return Arrays.stream(result)
                .flatMapToInt(Arrays::stream)
                .filter(count -> count > 0)
                .sum();
    }
}
