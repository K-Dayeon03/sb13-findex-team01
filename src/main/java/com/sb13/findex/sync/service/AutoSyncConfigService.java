package com.sb13.findex.sync.service;

import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSortField;
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

    private final AutoSyncConfigRepository autoSyncConfigRepository;
    // 지수 등록 로직과 연동할 때 쓰일 수 있음
    private final IndexInfoRepository indexInfoRepository;


    // 내부 또는 타 도메인 연동용
    @Transactional
    public AutoSyncConfigDto create(AutoSyncConfigCommand command) {
        if (autoSyncConfigRepository.existsByIndexInfo(command.indexInfo())) {
            throw new DuplicateAutoSyncConfigException(command.indexInfo().getId());
        }
        return saveNew(command);
    }



    private AutoSyncConfigDto saveNew(AutoSyncConfigCommand command) {
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

    // 지수 UPSERT 로직에서 호출 (하정님 요청)
    // 존재 확인 후 별도 insert 방식 대신, DB 레벨 원자적 upsert로 경쟁 상태 방지 (구영님 피드백 반영)
    @Transactional
    public void createIfAbsent(AutoSyncConfigCommand command) {
        autoSyncConfigRepository.upsertIfAbsent(command.indexInfo().getId(), command.enabled());
    }

    public CursorPageResponse<AutoSyncConfigDto> getList(AutoSyncConfigSearchCondition condition) {
        List<AutoSyncConfig> result = autoSyncConfigRepository.search(condition);

        int size = condition.resolvedSize();
        boolean hasNext = result.size() > size;
        List<AutoSyncConfig> content = hasNext ? result.subList(0, size) : result;

        List<AutoSyncConfigDto> dtoList = content.stream()
                .map(this::toDto)
                .toList();

        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext && !content.isEmpty()) {
            AutoSyncConfig last = content.get(content.size() - 1);
            AutoSyncConfigSortField sortField = AutoSyncConfigSortField.from(condition.sortField());
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

    // 지수 삭제 시 IndexInfo 서비스에서 호출 (하정님 요청 - Repository 대신 Service를 통해 접근하도록 정리)
    @Transactional
    public void deleteByIndexInfoId(Long indexInfoId) {
        autoSyncConfigRepository.deleteByIndexInfoId(indexInfoId);
    }
}