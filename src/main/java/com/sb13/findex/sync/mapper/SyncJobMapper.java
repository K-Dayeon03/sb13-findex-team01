package com.sb13.findex.sync.mapper;

import com.sb13.findex.sync.dto.response.SyncJobDto;
import com.sb13.findex.sync.entity.SyncJob;


import java.util.List;

public class SyncJobMapper {

    public static SyncJobDto toSyncJobDto(SyncJob syncJob) {
        Long indexInfoId = syncJob.getIndexInfo().getId();

        return new SyncJobDto(
                syncJob.getId(),
                syncJob.getJobType(),
                indexInfoId,
                syncJob.getTargetDate(),
                syncJob.getWorker(),
                syncJob.getJobTime(),
                syncJob.getResult()
        );
    }

    // 목록 조회 api에서 여러 건을 한번에 변환할 때 사용
    public static List<SyncJobDto> toResponseList(List<SyncJob> syncJobList) {
        return syncJobList.stream()
                .map(SyncJobMapper::toSyncJobDto)
                .toList();
    }
}
