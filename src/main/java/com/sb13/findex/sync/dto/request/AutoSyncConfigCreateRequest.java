package com.sb13.findex.sync.dto.request;

import jakarta.validation.constraints.NotNull;

// 자동 연동 설정 등록 요청 DTO, 스펙상 모든 속성(indexInfoId, enabled)을 받아 등록
public record AutoSyncConfigCreateRequest(
        @NotNull Long indexInfoId,
        boolean enabled
) {


}
