package com.sb13.findex.indexinfo.service.impl;

import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.indexinfo.dto.command.IndexInfoSearchCondition;
import com.sb13.findex.indexinfo.dto.command.IndexInfoUpdateCommand;
import com.sb13.findex.indexinfo.dto.response.CursorPageResponse;
import com.sb13.findex.indexinfo.dto.response.IndexInfoResponse;
import com.sb13.findex.indexinfo.dto.response.IndexInfoSummaryResponse;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.global.exception.indexinfo.DuplicateIndexInfoException;
import com.sb13.findex.global.exception.indexinfo.IndexInfoNotFoundException;
import com.sb13.findex.indexinfo.mapper.IndexInfoMapper;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import com.sb13.findex.indexinfo.repository.IndexInfoSortField;
import com.sb13.findex.indexinfo.service.IndexInfoService;
import com.sb13.findex.indexinfo.utli.IndexInfoPaginationUtils;
import com.sb13.findex.indexinfo.entity.SourceType;
import com.sb13.findex.autosyncconfig.dto.command.AutoSyncConfigCommand;
import com.sb13.findex.autosyncconfig.service.AutoSyncConfigService;
import com.sb13.findex.sync.service.SyncJobReferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class IndexInfoServiceImpl implements IndexInfoService {

    private final IndexInfoRepository indexInfoRepository;
    private final AutoSyncConfigService autoSyncConfigService;
    private final IndexDataService indexDataService;
    private final SyncJobReferenceService syncJobReferenceService;

    @Override
    public CursorPageResponse<IndexInfoResponse> search(IndexInfoSearchCondition condition) {
        int size = IndexInfoPaginationUtils.resolveSize(condition.size());

        List<IndexInfo> found =
                indexInfoRepository.searchIndexInfo(condition);

        boolean hasNext = found.size() > size;

        List<IndexInfo> content = hasNext
                ? found.subList(0, size)
                : found;

        List<IndexInfoResponse> responses = IndexInfoMapper.toResponseList(content);

        long totalElements = indexInfoRepository.countIndexInfo(condition);

        String nextCursor = null;
        Long nextIdAfter = null;

        if (hasNext && !content.isEmpty()) {
            IndexInfo last = content.get(content.size() - 1);
            nextCursor = getCursorValue(last, condition.sortField());
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

    @Override
    @Transactional
    public IndexInfoResponse create(IndexInfoCreateCommand command) {

        String indexClassification = command.indexClassification().strip();
        String indexName = command.indexName().strip();

        if (indexInfoRepository.existsByIndexClassificationAndIndexName(
                indexClassification, indexName
        )) {
            throw new DuplicateIndexInfoException();
        }
        IndexInfo indexInfo = IndexInfo.create(
                indexClassification,
                indexName,
                command.employedItemsCount(),
                command.basePointInTime(),
                command.baseIndex(),
                SourceType.USER,
                command.favorite()
        );

        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);
        autoSyncConfigService.create(
                new AutoSyncConfigCommand(
                        savedIndexInfo,
                        false
                )
        );

        return IndexInfoMapper.toResponse(savedIndexInfo);
    }

    @Override
    public IndexInfoResponse findById(Long id) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IndexInfoNotFoundException(id));

        return IndexInfoMapper.toResponse(indexInfo);
    }

    @Override
    @Transactional
    public IndexInfoResponse update(Long id, IndexInfoUpdateCommand command) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IndexInfoNotFoundException(id));

        indexInfo.update(
                command.employedItemsCount(),
                command.basePointInTime(),
                command.baseIndex(),
                command.favorite()
        );

        return IndexInfoMapper.toResponse(indexInfo);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(() -> new IndexInfoNotFoundException(id));

        // 지수 정보애 연결된 지수데이터 전체삭제
        indexDataService.deleteByIndexInfoId(id);

        // 지수 정보에 연결된 자동연동 설정 삭제
        autoSyncConfigService.deleteByIndexInfoId(id);


        // SyncJob 이력은 유지하고 삭제 대상 IndexInfo와의 연관관계만 해제
        syncJobReferenceService.detachIndexInfo(id);

        // 지수정보 삭제
        indexInfoRepository.delete(indexInfo);
    }

    @Override
    public List<IndexInfoSummaryResponse> findSummaries() {
        return indexInfoRepository.findAllSummaries();
    }

    private String getCursorValue(IndexInfo indexInfo, String sortFieldValue) {
        IndexInfoSortField sortField = IndexInfoSortField.from(sortFieldValue);

        return switch (sortField) {
            case INDEX_CLASSIFICATION -> indexInfo.getIndexClassification();
            case INDEX_NAME -> indexInfo.getIndexName();
            case EMPLOYED_ITEMS_COUNT -> String.valueOf(indexInfo.getEmployedItemsCount());
        };
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdateOpenApiInfo(
            IndexInfoCreateCommand command
    ) {
        String indexClassification =
                command.indexClassification().strip();
        String indexName =
                command.indexName().strip();

        int affectedRows = indexInfoRepository.upsertOpenApiIndexInfo(
                indexClassification,
                indexName,
                command.employedItemsCount(),
                command.basePointInTime(),
                command.baseIndex(),
                SourceType.OPEN_API.name()
        );

        if (affectedRows == 0) {
            log.info(
                    "Open API 지수정보 갱신 생략: 동일한 USER 타입 데이터가 존재합니다. " +
                            "indexClassification={}, indexName={}",
                    indexClassification,
                    indexName
            );
        }

        IndexInfo indexInfo =
                indexInfoRepository
                        .findByIndexClassificationAndIndexName(
                                indexClassification,
                                indexName
                        )
                        .orElseThrow(() ->
                                new IndexInfoNotFoundException(indexClassification, indexName)
                        );

        autoSyncConfigService.createIfAbsent(
                new AutoSyncConfigCommand(
                        indexInfo,
                        false
                )
        );

    }

}
