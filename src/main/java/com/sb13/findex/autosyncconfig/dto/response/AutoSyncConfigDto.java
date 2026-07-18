package com.sb13.findex.autosyncconfig.dto.response;


 //자동 연동 설정 응답 DTO
public record AutoSyncConfigDto(
        Long id, Long indexInfoId,
        String indexClassification, String indexName,
        boolean enabled
) {


}


