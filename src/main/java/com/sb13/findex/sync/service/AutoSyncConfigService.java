package com.sb13.findex.sync.service;

import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSortField;
import com.sb13.findex.sync.dto.request.AutoSyncConfigCreateRequest;
import com.sb13.findex.sync.dto.response.AutoSyncConfigDto;
import com.sb13.findex.sync.entity.AutoSyncConfig;
import com.sb13.findex.sync.exception.AutoSyncConfigNotFoundException;
import com.sb13.findex.sync.exception.DuplicateAutoSyncConfigException;
import com.sb13.findex.sync.repository.AutoSyncConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoSyncConfigService {

    private static final int DEFAULT_SIZE = 10;

    private final AutoSyncConfigRepository autoSyncConfigRepository;
    private final IndexInfoRepository indexInfoRepository;

    // API Controller에서 들어오는 진입점 — indexInfoId를 실제 엔티티로 변환한 뒤 기존 로직
    @Transactional
    public AutoSyncConfigDto create(AutoSyncConfigCreateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(request.indexInfoId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수입니다. indexInfoId=" + request.indexInfoId()));
        return create(new AutoSyncConfigCommand(indexInfo, request.enabled()));
    }

    // 내부 또는 타 도메인 연동용
    @Transactional
    public AutoSyncConfigDto create(AutoSyncConfigCommand command) {
        if (autoSyncConfigRepository.existsByIndexInfo(command.indexInfo())) {
            throw new DuplicateAutoSyncConfigException(command.indexInfo().getId());
        }

        AutoSyncConfig saved = autoSyncConfigRepository.save(
                AutoSyncConfig.builder()
                        .indexInfo(command.indexInfo())
                        .enabled(command.enabled())
                        .build());
        return toDto(saved);
    }

    @Transactional
    public AutoSyncConfigDto update(Long id, boolean enabled) {
        AutoSyncConfig config = autoSyncConfigRepository.findByIdWithIndexInfo(id)
                .orElseThrow(() -> new AutoSyncConfigNotFoundException(id));

        config.setEnabled(enabled);
        return toDto(config);
    }

    public CursorPageResponse<AutoSyncConfigDto> getList(AutoSyncConfigSearchCondition condition) {
        List<AutoSyncConfig> result = autoSyncConfigRepository.search(condition);

        int size = condition.size() == null || condition.size() <= 0 ? DEFAULT_SIZE : condition.size();
        boolean hasNext = result.size() > size;
        List<AutoSyncConfig> content = hasNext ? result.subList(0, size) : result;

        List<AutoSyncConfigDto> dtoList = content.stream()
                .map(this::toDto)
                .toList();

        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext && !content.isEmpty()) {
            AutoSyncConfig last = content.get(content.size() - 1);
            AutoSyncConfigSortField sortField = condition.sortField() == null || condition.sortField().isBlank()
                    ? AutoSyncConfigSortField.INDEX_INFO_ID
                    : AutoSyncConfigSortField.from(condition.sortField());
            nextCursor = switch (sortField) {
                case INDEX_INFO_ID -> String.valueOf(last.getIndexInfo().getId());
                case ENABLED -> String.valueOf(last.isEnabled());
            };
            nextIdAfter = last.getId();
        }

        long totalElements = autoSyncConfigRepository.count(condition);

        return new CursorPageResponse<>(dtoList, nextCursor, nextIdAfter, dtoList.size(), totalElements, hasNext);
    }

    private AutoSyncConfigDto toDto(AutoSyncConfig config) {
        IndexInfo indexInfo = config.getIndexInfo();
        return new AutoSyncConfigDto(config.getId(), indexInfo.getId(),
                indexInfo.getIndexClassification(), indexInfo.getIndexName(), config.isEnabled());
    }
}