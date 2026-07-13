package com.sb13.findex.sync.service;

import com.sb13.findex.sync.dto.response.AutoSyncConfigDto;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.sync.entity.AutoSyncConfig;
import com.sb13.findex.sync.exception.AutoSyncConfigNotFoundException;
import com.sb13.findex.sync.exception.DuplicateAutoSyncConfigException;
import com.sb13.findex.sync.repository.AutoSyncConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 자동 연동 설정 관리 서비스
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoSyncConfigService {

    private final AutoSyncConfigRepository autoSyncConfigRepository;

    // 지수 등록 여부를 먼저 검증한 뒤 자동 연동 설정 등록
    @Transactional
    public AutoSyncConfigDto create(AutoSyncConfigCommand command) {
        // 지수 등록 여부를 먼저 검증 이후 자동 연동 설정 등록 (중복 등록 방지)
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

    // 활성화 여부만 토글
    @Transactional
    public AutoSyncConfigDto update(Long id, boolean enabled) {
        // join fetch로 IndexInfo까지 함께 조회 -> toDto에서 추가 쿼리 없이 바로 사용 가능
        AutoSyncConfig config = autoSyncConfigRepository.findByIdWithIndexInfo(id)
                .orElseThrow(() -> new AutoSyncConfigNotFoundException(id));
        // 트랜잭션 범위 안에서 영속성 컨텍스트가 유지
        // setter로 값만 바꿔도 트랜잭션 종료 시점에 변경 감지(dirty checking)로 update 쿼리가 자동 실행됨
        config.setEnabled(enabled);
        return toDto(config);
    }

    // AutoSyncConfig + IndexInfo를 조합해 응답 DTO를 만들기
    private AutoSyncConfigDto toDto(AutoSyncConfig config) {
        IndexInfo indexInfo = config.getIndexInfo();
        return new AutoSyncConfigDto(config.getId(), indexInfo.getId(),
                indexInfo.getIndexClassification(), indexInfo.getIndexName(), config.isEnabled());
    }
}