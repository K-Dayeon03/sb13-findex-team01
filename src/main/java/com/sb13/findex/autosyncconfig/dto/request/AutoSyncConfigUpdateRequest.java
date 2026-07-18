package com.sb13.findex.autosyncconfig.dto.request;

import jakarta.validation.constraints.NotNull;

// 자동 연동 설정 수정 요청 DTO, 스펙상 활성화(enabled) 여부만 수정 가능하므로 이 필드만 포함
public record AutoSyncConfigUpdateRequest(
        @NotNull(message = "enabled 값은 필수입니다.")
        Boolean enabled
) {
}
