package com.sb13.findex.sync.repository.impl;

import com.sb13.findex.sync.dto.command.IndexDataSyncJobTarget;
import com.sb13.findex.sync.dto.command.IndexInfoKey;
import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;
import com.sb13.findex.indexinfo.entity.SourceType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
// Docker를 사용할 수 없는 환경에서는 해당 테스트를 건너뜁니다.
// TODO: Docker에 의존하지 않는 테스트 DB 환경으로 분리할 예정입니다.
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SyncJobBulkRepositoryImplTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SyncJobBulkRepositoryImpl syncJobBulkRepository;

    @BeforeEach
    void setUp() {
        syncJobBulkRepository = new SyncJobBulkRepositoryImpl(jdbcTemplate);
    }

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("postgres:16-alpine")
            )
                    .withDatabaseName("findex_test")
                    .withUsername("test")
                    .withPassword("test");

    @Test
    void saveInfoAll_지수정보가_존재하면_SUCCESS_이력을_저장한다() {
        // given
        String suffix = getShortUUID();

        String indexClassification = "TEST-" + suffix;
        String indexName = "테스트지수-" + suffix;
        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long indexInfoId = saveIndexInfo(indexClassification, indexName);

        IndexInfoKey indexInfoKey = new IndexInfoKey(indexClassification, indexName);

        // when
        int savedCount = syncJobBulkRepository.saveInfoAll(worker, List.of(indexInfoKey), executionId);

        //then
        SavedSyncJob savedSyncJob = jdbcTemplate.queryForObject(
                """
                         select
                                  id,
                                  index_info_id,
                                  index_classification_snapshot,
                                  index_name_snapshot,
                                  job_type,
                                  worker,
                                  result,
                                  sync_execution_id
                        from sync_job
                        where sync_execution_id = ?
                        """,
                (rs, rn) -> new SavedSyncJob(
                        rs.getObject("index_info_id", Long.class),
                        rs.getString("index_classification_snapshot"),
                        rs.getString("index_name_snapshot"),
                        rs.getString("job_type"),
                        rs.getString("worker"),
                        rs.getString("result"),
                        rs.getObject("sync_execution_id", UUID.class)
                ),
                executionId
        );

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertNotNull(savedSyncJob),
                () -> assertEquals(indexInfoId, savedSyncJob.indexInfoId),
                () -> assertEquals(indexClassification, savedSyncJob.indexClassificationSnapshot),
                () -> assertEquals(indexName, savedSyncJob.indexNameSnapshot),
                () -> assertEquals(JobType.INDEX_INFO.name(), savedSyncJob.jobType),
                () -> assertEquals(worker, savedSyncJob.worker),
                () -> assertEquals(JobResult.SUCCESS.name(), savedSyncJob.result),
                () -> assertEquals(executionId, savedSyncJob.executionId)
        );

    }

    @Test
    void saveInfoAll_지수정보가_존재하지_않으면_FAILED_이력을_저장한다() {
        //  given
        String suffix = getShortUUID();
        String indexClassification = "NOT-EXIST-" + suffix;
        String indexName = "존재하지않는지수-" + suffix;
        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        IndexInfoKey indexInfoKey = new IndexInfoKey(indexClassification, indexName);

        Integer existingIndexCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from index_info
                        where index_classification =?
                        and index_name = ?
                        """,
                Integer.class,
                indexClassification,
                indexName
        );

        assertEquals(0, existingIndexCount);

        // when
        int savedCount = syncJobBulkRepository.saveInfoAll(worker, List.of(indexInfoKey), executionId);

        //then

        SavedSyncJob savedSyncJob = jdbcTemplate.queryForObject(
                """
                        select
                            index_info_id,
                            index_classification_snapshot,
                            index_name_snapshot,
                            job_type,
                            worker,
                            result,
                            sync_execution_id
                        from sync_job
                        where sync_execution_id =?
                        """,
                (rs, rn) -> new SavedSyncJob(
                        rs.getObject("index_info_id", Long.class),
                        rs.getString("index_classification_snapshot"),
                        rs.getString("index_name_snapshot"),
                        rs.getString("job_type"),
                        rs.getString("worker"),
                        rs.getString("result"),
                        rs.getObject("sync_execution_id", UUID.class)
                ),
                executionId
        );

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertNotNull(savedSyncJob),
                () -> assertEquals(indexClassification, savedSyncJob.indexClassificationSnapshot),
                () -> assertEquals(indexName, savedSyncJob.indexNameSnapshot),
                () -> assertEquals(JobType.INDEX_INFO.name(), savedSyncJob.jobType),
                () -> assertEquals(worker, savedSyncJob.worker),
                () -> assertEquals(JobResult.FAILED.name(), savedSyncJob.result),
                () -> assertEquals(executionId, savedSyncJob.executionId)
        );
    }

    @Test
    void saveInfoAll_요청한_지수정보의_스냅샷을_저장한다() {
        //given
        String suffix = getShortUUID();
        String requestedClassification = "KOSPI-" + suffix;
        String requestedName = "코스피-" + suffix;
        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long indexInfoId = saveIndexInfo(requestedClassification, requestedName);

        IndexInfoKey indexInfoKey = new IndexInfoKey(requestedClassification, requestedName);

        // when
        int savedCount = syncJobBulkRepository.saveInfoAll(worker, List.of(indexInfoKey), executionId);

        /*
         * 이력 저장 후 원본 지수 정보를 변경합니다.
         * sync_job의 스냅샷은 기존 요청 값을 그대로 유지해야 합니다.
         */
        indexInfoUpdate(suffix, indexInfoId);

        IndexInfoSnapshot indexInfoSnapshot = getIndexInfoSnapshot(executionId);

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertNotNull(indexInfoSnapshot),
                () -> assertEquals(requestedClassification, indexInfoSnapshot.indexClassification),
                () -> assertEquals(requestedName, indexInfoSnapshot.indexName)
        );

    }


    @Test
    void saveInfoAll_여러_지수정보의_연동이력을_한번에_저장한다() {
        String suffix = getShortUUID();
        String firstClassification = "KOSPI-" + suffix;
        String firstName = "코스피-" + suffix;

        String secondClassification = "KOSDAQ-" + suffix;
        String secondName = "코스닥-" + suffix;

        String notExistsClassification = "NOT-EXISTS-" + suffix;
        String notExistsName = "존재하지않는지수-" + suffix;

        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long firstIndexInfoId = saveIndexInfo(firstClassification, firstName);
        Long secondIndexInfoId = saveIndexInfo(secondClassification, secondName);

        List<IndexInfoKey> indexInfoKeys = List.of(
                new IndexInfoKey(firstClassification, firstName),
                new IndexInfoKey(secondClassification, secondName),
                new IndexInfoKey(notExistsClassification, notExistsName)
        );

        // when
        int savedCount = syncJobBulkRepository.saveInfoAll(worker, indexInfoKeys, executionId);

        //then
        List<SavedSyncJob> savedSyncJobs = jdbcTemplate.query(
                """
                        select
                            index_info_id,
                            index_classification_snapshot,
                            index_name_snapshot,
                            job_type,
                            worker,
                            result,
                            sync_execution_id
                        from sync_job
                        where sync_execution_id=?
                        """,
                (rs, rn) -> new SavedSyncJob(
                        rs.getObject("index_info_id", Long.class),
                        rs.getString("index_classification_snapshot"),
                        rs.getString("index_name_snapshot"),
                        rs.getString("job_type"),
                        rs.getString("worker"),
                        rs.getString("result"),
                        rs.getObject("sync_execution_id", UUID.class)
                ),
                executionId
        );

        Map<String, SavedSyncJob> savedJobByIndexName = savedSyncJobs.stream()
                .collect(Collectors.toMap(
                        s -> s.indexNameSnapshot,
                        Function.identity()
                ));

        SavedSyncJob firstSavedJob = savedJobByIndexName.get(firstName);
        SavedSyncJob secondSavedJob = savedJobByIndexName.get(secondName);
        SavedSyncJob notExistsSavedJob = savedJobByIndexName.get(notExistsName);

        assertAll(
                () -> assertEquals(3, savedCount),
                () -> assertEquals(3, savedSyncJobs.size()),

                // 첫번째 존재하는 지수 정보
                () -> assertNotNull(firstSavedJob),
                () -> assertEquals(firstIndexInfoId, firstSavedJob.indexInfoId),
                () -> assertEquals(firstClassification, firstSavedJob.indexClassificationSnapshot),
                () -> assertEquals(JobResult.SUCCESS.name(), firstSavedJob.result),

                // 두 번째 존재하는 지수 정보
                () -> assertNotNull(secondSavedJob),
                () -> assertEquals(secondIndexInfoId, secondSavedJob.indexInfoId),
                () -> assertEquals(secondClassification, secondSavedJob.indexClassificationSnapshot),
                () -> assertEquals(JobResult.SUCCESS.name(), secondSavedJob.result),

                // 존재하지 않는 지수 정보
                () -> assertNotNull(notExistsSavedJob),
                () -> assertNull(notExistsSavedJob.indexInfoId),
                () -> assertEquals(notExistsClassification, notExistsSavedJob.indexClassificationSnapshot),
                () -> assertEquals(JobResult.FAILED.name(), notExistsSavedJob.result),

                // 공통 연동 정보
                () -> assertTrue(
                        savedSyncJobs.stream()
                                .allMatch(job -> JobType.INDEX_INFO.name().equals(job.jobType))
                ),
                () -> assertTrue(
                        savedSyncJobs.stream()
                                .allMatch(job -> worker.equals(job.worker))
                ),
                () -> assertTrue(
                        savedSyncJobs.stream()
                                .allMatch(job -> executionId.equals(job.executionId))
                )
        );

    }

    @Test
    void saveInfoAll_저장된_연동이력_건수를_반환한다() {
        //given
        String suffix = getShortUUID();
        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        List<IndexInfoKey> indexInfoKeys = List.of(
                new IndexInfoKey(
                        "NOT-EXISTS-1-" + suffix,
                        "존재하지않는지수1-" + suffix
                ), new IndexInfoKey(
                        "NOT-EXISTS-2-" + suffix,
                        "존재하지않는지수2-" + suffix
                ), new IndexInfoKey(
                        "NOT-EXISTS-3-" + suffix,
                        "존재하지않는지수3-" + suffix
                )
        );

        // when
        int savedCount = syncJobBulkRepository.saveInfoAll(worker, indexInfoKeys, executionId);

        // then
        Long actualSavedCount = getActualSavedCount(executionId, JobType.INDEX_INFO);

        assertAll(
                () -> assertEquals(
                        indexInfoKeys.size(),
                        savedCount
                ),
                () -> assertEquals(
                        indexInfoKeys.size(),
                        actualSavedCount
                ),
                () -> assertEquals(
                        actualSavedCount.longValue(),
                        savedCount
                )
        );
    }

    private @Nullable Long getActualSavedCount(UUID executionId, JobType jobType) {
        return jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from sync_job
                        where sync_execution_id =?
                        and job_type = ?
                        """,
                Long.class,
                executionId,
                jobType.name()
        );
    }

    @Test
    void saveDataAll_지수데이터가_존재하면_SUCCESS_이력을_저장한다() {
        // given
        String suffix = getShortUUID();

        String indexClassification = "KOSPI-" + suffix;
        String indexName = "코스피-" + suffix;
        LocalDate baseDate = LocalDate.of(2026, 7, 15);

        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long indexInfoId = saveIndexInfo(indexClassification, indexName);

        LocalDateTime dataUpdatedAt = LocalDateTime.of(2026, 7, 15, 10, 30);

        saveIndexData(
                indexInfoId,
                baseDate,
                dataUpdatedAt
        );

        IndexDataSyncJobTarget target = new IndexDataSyncJobTarget(
                indexInfoId,
                baseDate,
                indexClassification,
                indexName
        );

        // when
        int savedCount = syncJobBulkRepository.saveDataAll(worker, List.of(target), executionId);

        //then
        SavedDataSyncJob savedSyncJob = getSavedDataSyncJob(executionId);

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertEquals(indexInfoId, savedSyncJob.indexInfoId),
                () -> assertEquals(indexClassification, savedSyncJob.indexClassificationSnapshot),
                () -> assertEquals(indexName, savedSyncJob.indexNameSnapshot),
                () -> assertEquals(JobType.INDEX_DATA.name(), savedSyncJob.jobType),
                () -> assertEquals(worker, savedSyncJob.worker),
                () -> assertEquals(JobResult.SUCCESS.name(), savedSyncJob.result),
                () -> assertEquals(executionId, savedSyncJob.syncExecutionId),
                () -> assertEquals(baseDate, savedSyncJob.targetDate),
                () -> assertEquals(dataUpdatedAt, savedSyncJob.jobTime)
        );

    }

    @Test
    void saveDataAll_지수데이터가_존재하지_않으면_FAILED_이력을_저장한다() {
        // given
        String suffix = getShortUUID();

        String indexClassification = "KOSPI-" + suffix;
        String indexName = "코스피-" + suffix;
        LocalDate baseDate = LocalDate.of(2026, 7, 15);

        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        /*
         * 지수 정보는 존재하지만,
         * 해당 지수와 기준일에 해당하는 index_data 는 저장하지 않습니다.
         */
        Long indexInfoId = saveIndexInfo(indexClassification, indexName);

        IndexDataSyncJobTarget target = new IndexDataSyncJobTarget(
                indexInfoId,
                baseDate,
                indexClassification,
                indexName
        );

        Long existingDataCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from index_data
                        where index_info_id = ?
                            and base_date =?
                        """,
                Long.class,
                indexInfoId,
                Date.valueOf(baseDate)
        );

        assertEquals(0, existingDataCount);

        // when
        int savedCount = syncJobBulkRepository.saveDataAll(worker, List.of(target), executionId);

        //then
        SavedDataSyncJob savedSyncJob = getSavedDataSyncJob(executionId);

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertNotNull(savedSyncJob),
                () -> assertEquals(indexInfoId, savedSyncJob.indexInfoId),
                () -> assertEquals(indexClassification, savedSyncJob.indexClassificationSnapshot),
                () -> assertEquals(indexName, savedSyncJob.indexNameSnapshot),
                () -> assertEquals(JobType.INDEX_DATA.name(), savedSyncJob.jobType),
                () -> assertEquals(worker, savedSyncJob.worker),
                () -> assertEquals(JobResult.FAILED.name(), savedSyncJob.result),
                () -> assertEquals(executionId, savedSyncJob.syncExecutionId),
                () -> assertEquals(baseDate, savedSyncJob.targetDate),
                () -> assertNotNull(savedSyncJob.jobTime)
        );

    }

    @Test
    void saveDataAll_기준일을_targetDate로_저장한다() {

        //given
        String suffix = getShortUUID();

        String indexClassification = "KOSPI-" + suffix;
        String indexName = "코스피-" + suffix;
        LocalDate requestedBaseDate = LocalDate.of(2026, 7, 15);

        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long indexInfoId = saveIndexInfo(indexClassification, indexName);

        IndexDataSyncJobTarget target = new IndexDataSyncJobTarget(
                indexInfoId,
                requestedBaseDate,
                indexClassification,
                indexName
        );

        //when
        int savedCount = syncJobBulkRepository.saveDataAll(worker, List.of(target), executionId);

        //then
        LocalDate savedTargetDate = jdbcTemplate.queryForObject(
                """
                        select target_date
                        from sync_job
                        where sync_execution_id = ?
                            and job_type = 'INDEX_DATA'
                        """,
                (rs, rn) -> rs.getDate("target_date").toLocalDate(),
                executionId
        );

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertEquals(requestedBaseDate, savedTargetDate)
        );
    }

    @Test
    void saveDataAll_요청한_지수정보의_스냅샷을_저장한다() {
        // given
        String suffix = getShortUUID();

        String requestedClassification = "KOSPI-" + suffix;
        String requestedName = "코스피-" + suffix;
        LocalDate requestedBaseDate = LocalDate.of(2026, 7, 15);
        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long indexInfoId = saveIndexInfo(requestedClassification, requestedName);

        IndexDataSyncJobTarget target = new IndexDataSyncJobTarget(
                indexInfoId,
                requestedBaseDate,
                requestedClassification,
                requestedName
        );

        //when
        int savedCount = syncJobBulkRepository.saveDataAll(worker, List.of(target), executionId);

        /*
         * 이력 저장 후 원본 지수 정보를 변경합니다.
         * 저장된 sync_job의 스냅샷에는 영향을 주지 않아야 합니다.
         */
        indexInfoUpdate(suffix, indexInfoId);

        // then
        IndexInfoSnapshot savedSnapshot = getIndexInfoSnapshot(executionId);

        assertAll(
                () -> assertEquals(1, savedCount),
                () -> assertNotNull(savedSnapshot),
                () -> assertEquals(requestedClassification, savedSnapshot.indexClassification),
                () -> assertEquals(requestedName, savedSnapshot.indexName)
        );

    }

    @Test
    void saveDataAll_여러_지수데이터의_연동이력을_한번에_저장한다() {
        // given
        String suffix = getShortUUID();

        String firstClassification = "KOSPI" + suffix;
        String firstName = "코스피-" + suffix;

        String secondClassification = "KOSDAQ-" +suffix;
        String secondName = "코스닥-" + suffix;

        LocalDate firstBaseDate = LocalDate.of(2026, 7, 14);
        LocalDate secondBaseDate = LocalDate.of(2026, 7, 15);
        LocalDate notExistsBaseDate = LocalDate.of(2026, 7, 16);

        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long firstIndexInfoId = saveIndexInfo(firstClassification, firstName);
        Long secondIndexInfoId = saveIndexInfo(secondClassification, secondName);

        /*
         * 아래 2건은 index_data가 존재하므로 SUCCESS가 됩니다.
         */
        saveIndexData(
                firstIndexInfoId,
                firstBaseDate,
                LocalDateTime.of(2026, 7, 14, 10, 0)
        );

        saveIndexData(
                secondIndexInfoId,
                secondBaseDate,
                LocalDateTime.of(2026, 7, 15, 11, 0)
        );

        /*
         * notExistsBaseDate에 해당하는 index_data는 저장하지 않습니다.
         * 따라서 해당 요청은 FAILED가 됩니다.
         */

        List<IndexDataSyncJobTarget> targets = List.of(
                new IndexDataSyncJobTarget(firstIndexInfoId, firstBaseDate, firstClassification, firstName),
                new IndexDataSyncJobTarget(secondIndexInfoId, secondBaseDate, secondClassification, secondName),
                new IndexDataSyncJobTarget(secondIndexInfoId, notExistsBaseDate, secondClassification, secondName)
        );

        // when
        int savedCount = syncJobBulkRepository.saveDataAll(worker, targets, executionId);

        // then

        List<SavedDataSyncJob> savedDataSyncJobs = getSavedDataSyncJobs(executionId);

        Map<DataSyncJobKey, SavedDataSyncJob> savedJobMap = savedDataSyncJobs.stream()
                .collect(Collectors.toMap(
                        job -> new DataSyncJobKey(
                                job.indexInfoId,
                                job.targetDate
                        ),
                        Function.identity()
                ));

        SavedDataSyncJob firstSuccessJob = savedJobMap.get(new DataSyncJobKey(firstIndexInfoId, firstBaseDate));
        SavedDataSyncJob secondSuccessJob = savedJobMap.get(new DataSyncJobKey(secondIndexInfoId, secondBaseDate));
        SavedDataSyncJob failJob = savedJobMap.get(new DataSyncJobKey(secondIndexInfoId, notExistsBaseDate));

        assertAll(
                ()-> assertEquals(3, savedCount),
                ()-> assertEquals(3, savedDataSyncJobs.size()),
                ()-> assertNotNull(firstSuccessJob),
                ()-> assertNotNull(secondSuccessJob),
                ()-> assertNotNull(failJob),

                ()-> assertEquals(JobResult.SUCCESS.name(),firstSuccessJob.result),
                ()-> assertEquals(firstClassification, firstSuccessJob.indexClassificationSnapshot),
                ()-> assertEquals(firstName, firstSuccessJob.indexNameSnapshot),
                ()-> assertEquals(firstBaseDate, firstSuccessJob.targetDate),

                ()-> assertEquals(JobResult.SUCCESS.name(), secondSuccessJob.result),
                ()-> assertEquals(secondClassification, secondSuccessJob.indexClassificationSnapshot),
                ()-> assertEquals(secondName, secondSuccessJob.indexNameSnapshot),
                ()-> assertEquals(secondBaseDate, secondSuccessJob.targetDate),

                ()-> assertEquals(JobResult.FAILED.name(), failJob.result),
                ()-> assertEquals(secondIndexInfoId, failJob.indexInfoId),
                ()-> assertEquals(notExistsBaseDate, failJob.targetDate),

                ()-> assertTrue(
                        savedDataSyncJobs.stream()
                                .allMatch(job -> JobType.INDEX_DATA.name().equals(job.jobType))
                ),
                ()-> assertTrue(
                        savedDataSyncJobs.stream()
                                .allMatch(job -> worker.equals(job.worker))
                ),
                ()-> assertTrue(
                        savedDataSyncJobs.stream()
                                .allMatch(job -> executionId.equals(job.syncExecutionId))
                )
        );

    }

    @Test
    void saveDataAll_저장된_연동이력_건수를_반환한다() {

        //given
        String suffix = getShortUUID();

        String indexClassification = "KOSPI-" + suffix;
        String indexName = "코스피-" + suffix;

        String worker = "test-worker";
        UUID executionId = UUID.randomUUID();

        Long indexInfoId = saveIndexInfo(indexClassification, indexName);

        List<IndexDataSyncJobTarget> targets = List.of(
                new IndexDataSyncJobTarget(indexInfoId, LocalDate.of(2026, 7, 14), indexClassification, indexName),
                new IndexDataSyncJobTarget(indexInfoId, LocalDate.of(2026, 7, 15), indexClassification, indexName),
                new IndexDataSyncJobTarget(indexInfoId, LocalDate.of(2026, 7, 16), indexClassification, indexName)
        );

        // when
        int savedCount = syncJobBulkRepository.saveDataAll(worker, targets, executionId);

        // then
        Long actualSavedCount = getActualSavedCount(executionId, JobType.INDEX_DATA);

        assertAll(
                () -> assertEquals(targets.size(), savedCount),
                () -> assertEquals(targets.size(), actualSavedCount),
                () -> assertEquals(actualSavedCount.longValue(), savedCount)
        );
    }

    private SavedDataSyncJob getSavedDataSyncJob(UUID executionId) {
        return jdbcTemplate.queryForObject(
                """
                        select
                            index_info_id,
                            index_classification_snapshot,
                            index_name_snapshot,
                            job_type,
                            worker,
                            job_time,
                            result,
                            sync_execution_id,
                            target_date
                        from sync_job
                        where sync_execution_id = ?
                        """,
                (rs, rn) -> new SavedDataSyncJob(
                        rs.getObject("index_info_id", Long.class),
                        rs.getString("index_classification_snapshot"),
                        rs.getString("index_name_snapshot"),
                        rs.getString("job_type"),
                        rs.getString("worker"),
                        rs.getTimestamp("job_time").toLocalDateTime(),
                        rs.getString("result"),
                        rs.getObject("sync_execution_id", UUID.class),
                        rs.getDate("target_date").toLocalDate()
                ),
                executionId
        );
    }

    private List<SavedDataSyncJob> getSavedDataSyncJobs(UUID executionId) {
        return jdbcTemplate.query(
                """
                        select
                            index_info_id,
                            index_classification_snapshot,
                            index_name_snapshot,
                            job_type,
                            worker,
                            job_time,
                            result,
                            sync_execution_id,
                            target_date
                        from sync_job
                        where sync_execution_id = ?
                        """,
                (rs, rn) -> new SavedDataSyncJob(
                        rs.getObject("index_info_id", Long.class),
                        rs.getString("index_classification_snapshot"),
                        rs.getString("index_name_snapshot"),
                        rs.getString("job_type"),
                        rs.getString("worker"),
                        rs.getTimestamp("job_time").toLocalDateTime(),
                        rs.getString("result"),
                        rs.getObject("sync_execution_id", UUID.class),
                        rs.getDate("target_date").toLocalDate()
                ),
                executionId
        );
    }

    private IndexInfoSnapshot getIndexInfoSnapshot(UUID executionId) {
        return jdbcTemplate.queryForObject(
                """
                        select
                            index_classification_snapshot,
                            index_name_snapshot
                        from sync_job
                        where sync_execution_id = ?
                        """,
                (rs, rn) -> new IndexInfoSnapshot(
                        rs.getString("index_classification_snapshot"),
                        rs.getString("index_name_snapshot")
                ),
                executionId
        );
    }

    private void indexInfoUpdate(String suffix, Long indexInfoId) {
        jdbcTemplate.update(
                """
                        update index_info
                        set index_classification = ?,
                             index_name = ?,
                             updated_at = now()
                        where id = ?
                        """,
                "CHANGED-" + suffix,
                "변경된지수-" + suffix,
                indexInfoId
        );
    }

    private String getShortUUID() {
        return UUID.randomUUID()
                .toString()
                .substring(0, 8);
    }

    private Long saveIndexInfo(String requestedClassification, String requestedName) {
        return jdbcTemplate.queryForObject(
                """
                        insert into index_info(
                        index_classification,
                        index_name,
                        employed_items_count,
                        base_point_in_time,
                        base_index,
                        source_type,
                        favorite,
                        created_at,
                        updated_at
                        )
                        values (?, ?, ?, ?, ?, ?, ?, now(), now())
                        returning id
                        """,
                Long.class,
                requestedClassification,
                requestedName,
                100,
                LocalDate.of(2026, 7, 15)
                , new BigDecimal("100.00"),
                SourceType.OPEN_API.name(),
                false
        );
    }

    private void saveIndexData(Long indexInfoId, LocalDate baseDate, LocalDateTime dataUpdatedAt) {
        jdbcTemplate.update(
                """
                            insert into index_data(
                                                   index_info_id,
                                                   base_date,
                                                   index_type,
                                                   market_price,
                                                   closing_price,
                                                   high_price,
                                                   low_price,
                                                   versus,
                                                   fluctuation_rate,
                                                   trading_price,
                                                   trading_quantity,
                                                   market_total_amount,
                                                   created_at,
                                                   updated_at
                            )
                            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                indexInfoId,
                Date.valueOf(baseDate),
                SourceType.OPEN_API.name(),
                new BigDecimal("100.00"),
                new BigDecimal("110.00"),
                new BigDecimal("120.00"),
                new BigDecimal("90.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                100_000L,
                1_000_000L,
                10_000_000L,
                Timestamp.valueOf(dataUpdatedAt.minusMinutes(1)),
                Timestamp.valueOf(dataUpdatedAt)
        );
    }

    private record SavedSyncJob(
            Long indexInfoId,
            String indexClassificationSnapshot,
            String indexNameSnapshot,
            String jobType,
            String worker,
            String result,
            UUID executionId
    ) {
    }

    private record IndexInfoSnapshot(
            String indexClassification,
            String indexName
    ) {
    }

    private record SavedDataSyncJob(
            Long indexInfoId,
            String indexClassificationSnapshot,
            String indexNameSnapshot,
            String jobType,
            String worker,
            LocalDateTime jobTime,
            String result,
            UUID syncExecutionId,
            LocalDate targetDate
    ) {
    }

    private record DataSyncJobKey(
            Long indexInfoId,
            LocalDate targetDate
    ) {
    }
}