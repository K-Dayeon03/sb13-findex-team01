package com.sb13.findex.sync.service.impl;


import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.indexinfo.service.IndexInfoService;
import com.sb13.findex.sync.dto.command.IndexDataSyncJobTarget;
import com.sb13.findex.sync.dto.command.IndexInfoKey;
import com.sb13.findex.sync.dto.request.SyncJobSearchCommand;
import com.sb13.findex.sync.dto.request.SyncJobSortField;
import com.sb13.findex.sync.dto.response.SyncJobDto;
import com.sb13.findex.sync.entity.SyncJob;
import com.sb13.findex.sync.mapper.SyncJobMapper;
import com.sb13.findex.sync.repository.SyncJobRepository;
import com.sb13.findex.sync.service.SyncJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true) // 조회 전용이라 읽기 전용 트랜잭션으로 설정 했습니다.
@RequiredArgsConstructor
@Slf4j
public class SyncJobServiceImpl implements SyncJobService {

    // size 파라미터가 없을 때 기본 페이지 크기
    private static final int DEFAULT_SIZE = 10;

    private final SyncJobRepository syncJobRepository;

    private final IndexDataService indexDataService;

    private final IndexInfoService indexInfoService;

    @Override
    public CursorPageResponse<SyncJobDto> search(SyncJobSearchCommand command) {
        int size = getSize(command.size());

        // Repository에서 size보다 1개 더 조회해온다 (다음 페이지 존재 여부 판단용)
        List<SyncJob> found = syncJobRepository.search(command);

        // 요청한 size보다 많이 왔으면 다음 페이지가 있다는 뜻
        boolean hasNext = found.size() > size;

        // 실제 응답으로 나갈 데이터는 size개만큼만 자른다
        List<SyncJob> content = hasNext
                ? found.subList(0, size)
                : found;

        // Entity 목록 -> DTO 목록 변환은 Mapper에게 위임
        List<SyncJobDto> responses = SyncJobMapper.toResponseList(content);

        // 필터 조건 기준 전체 데이터 개수
        long totalElements = syncJobRepository.count(command);

        String nextCursor = null;
        Long nextIdAfter = null;

        // 다음 페이지가 있으면, 이번 페이지 마지막 데이터를 기준으로 다음 cursor 값을 만든다
        if (hasNext && !content.isEmpty()) {
            SyncJob last = content.get(content.size() - 1);
            nextCursor = getCursorValue(last, command.sortField());
            nextIdAfter = last.getId();
        }

        return new CursorPageResponse<>(
                responses,
                nextCursor,
                nextIdAfter,
                responses.size(),
                totalElements,
                hasNext
        );
    }

    @Transactional
    @Override
    public void indexDataSaveAll(List<IndexDataOpenApiCommand> dataOpenApiCommands, String worker, UUID uuid ) {
        log.info("indexDataSaveAll called with {} commands", dataOpenApiCommands.size());

        dataOpenApiCommands.forEach(indexDataService::saveOrUpdateOpenApiData);

        List<IndexDataSyncJobTarget> indexDataSyncJobTargets = dataOpenApiCommands.stream()
                .map(this::createIndexDataKey)
                .toList();

        syncJobRepository.saveDataAll(worker, indexDataSyncJobTargets, uuid);

    }

    @Transactional
    @Override
    public List<SyncJobDto> indexInfoSaveAll(List<IndexInfoCreateCommand> infoCreateCommands, String worker) {
        log.info("indexInfoSaveAll called with {} commands", infoCreateCommands.size());

        infoCreateCommands.forEach(indexInfoService::saveOrUpdateOpenApiInfo);

        List<IndexInfoKey> indexInfoKeys = infoCreateCommands.stream()
                .map(this::getIndexInfoKey)
                .toList();

        UUID uuid = UUID.randomUUID();

        syncJobRepository.saveInfoAll(worker, indexInfoKeys, uuid);

        List<SyncJob> foundSyncJobs = syncJobRepository.findBySyncExecutionId(uuid);

        return foundSyncJobs.stream()
                .map(SyncJobMapper::toSyncJobDto)
                .toList();
    }

    @Override
    public List<SyncJobDto> foundSyncJobs(UUID uuid) {
        return syncJobRepository.findBySyncExecutionId(uuid).stream()
                .map(SyncJobMapper::toSyncJobDto)
                .toList();
    }


    private IndexInfoKey getIndexInfoKey(IndexInfoCreateCommand command) {
        return new IndexInfoKey(command.indexClassification(), command.indexName());
    }

    private IndexDataSyncJobTarget createIndexDataKey(IndexDataOpenApiCommand command) {
        return new IndexDataSyncJobTarget(
                command.indexInfo().getId(),
                command.baseDate(),
                command.indexInfo().getIndexClassification(),
                command.indexInfo().getIndexName()
        );
    }

    // size 파라미터가 없거나 잘못된 값이면 기본값(10) 사용
    private int getSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return size;
    }

    // 정렬 기준(targetDate 또는 jobTime)에 따라 마지막 데이터의 값을 문자열로 꺼낸다
    private String getCursorValue(SyncJob syncJob, String sortFieldValue) {
        SyncJobSortField sortField = getSortField(sortFieldValue);

        return switch (sortField) {
            case TARGET_DATE -> {
                LocalDate targetDate = syncJob.getTargetDate();
                if (targetDate == null) {
                    throw new IllegalStateException(
                            "targetDate가 null인 SyncJob은 커서 정렬에 사용할 수 없습니다. id=" + syncJob.getId());
                }
                yield targetDate.toString();
            }
            case JOB_TIME -> {
                LocalDateTime jobTime = syncJob.getJobTime();
                if (jobTime == null) {
                    throw new IllegalStateException(
                            "jobTime이 null인 SyncJob은 커서 정렬에 사용할 수 없습니다. id=" + syncJob.getId());
                }
                yield jobTime.toString();
            }
        };
    }

    // 정렬 필드 문자열을 Enum으로 변환, 없으면 기본값(jobTime)
    private SyncJobSortField getSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return SyncJobSortField.JOB_TIME;
        }
        return SyncJobSortField.from(sortField);
    }
}
