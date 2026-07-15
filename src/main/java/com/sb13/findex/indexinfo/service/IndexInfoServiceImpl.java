package com.sb13.findex.indexinfo.service;

import com.sb13.findex.indexdata.service.*;
import com.sb13.findex.indexinfo.dto.command.*;
import com.sb13.findex.indexinfo.dto.response.*;
import com.sb13.findex.indexinfo.entity.*;
import com.sb13.findex.indexinfo.exception.*;
import com.sb13.findex.indexinfo.mapper.*;
import com.sb13.findex.indexinfo.repository.*;
import com.sb13.findex.indexinfo.utli.*;
import com.sb13.findex.sync.entity.*;
import com.sb13.findex.sync.service.*;
import com.sb13.findex.sync.service.AutoSyncConfigService;
import lombok.*;
import org.springframework.dao.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
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

        if(hasNext && !content.isEmpty()) {
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

        if(indexInfoRepository.existsByIndexClassificationAndIndexName(
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
                .orElseThrow(()-> new IndexInfoNotFoundException(id));

        return IndexInfoMapper.toResponse(indexInfo);
    }

    @Override
    @Transactional
    public IndexInfoResponse update(Long id, IndexInfoUpdateCommand command) {
        IndexInfo indexInfo = indexInfoRepository.findById(id)
                .orElseThrow(()-> new IndexInfoNotFoundException(id));

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
                .orElseThrow(()-> new IndexInfoNotFoundException(id));

        // 지수 정보애 연결된 지수데이터 전체삭제
        indexDataService.deleteByIndexInfoId(id);

        // TODO: 자동 연동 담당자 메서드 추가 후 연결
        // autoSyncConfigService.deleteByIndexInfoId(id);


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

        Optional<IndexInfo> existingIndexInfo =
                indexInfoRepository
                        .findByIndexClassificationAndIndexName(
                                indexClassification,
                                indexName
                        );

        if (existingIndexInfo.isPresent()) {
            existingIndexInfo.get().updateByOpenApi(
                    command.employedItemsCount(),
                    command.basePointInTime(),
                    command.baseIndex()
            );
            return;
        }

        IndexInfo indexInfo = IndexInfo.create(
                indexClassification,
                indexName,
                command.employedItemsCount(),
                command.basePointInTime(),
                command.baseIndex(),
                SourceType.OPEN_API,
                false
        );

        IndexInfo savedIndexInfo =
                indexInfoRepository.saveAndFlush(indexInfo);

        autoSyncConfigService.create(
                new AutoSyncConfigCommand(
                        savedIndexInfo,
                        false
                )
        );
    }
}
