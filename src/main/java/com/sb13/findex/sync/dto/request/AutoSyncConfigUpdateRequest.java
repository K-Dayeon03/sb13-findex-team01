package com.sb13.findex.sync.dto.request;

// 자동 연동 설정 수정 요청 DTO, 스펙상 활성화(enabled) 여부만 수정 가능하므로 이 필드만 포함
public record AutoSyncConfigUpdateRequest(boolean enabled) {


}
