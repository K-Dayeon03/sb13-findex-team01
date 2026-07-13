package com.sb13.findex.sync.dto.response;

import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobDto(
        Long id,
        JobType jobType,
        Long indexInfoId,
        LocalDate targetDate,
        String worker,
        LocalDateTime jobTime,
        JobResult result

)
{

}
