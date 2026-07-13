package com.sb13.findex.sync.dto.request;

import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;

import java.time.LocalDate;

public record SyncJobSearchRequest(
        String jobType,
        Long indexInfoId,
        LocalDate targetDate,
        String worker,
        String result,
        String sortField,
        String sortDirection,
        String cursor,
        Long idAfter,
        Integer size
) {
    // jobType 유효성 검증
    private JobType parseJobType(String jobType) {
        if (jobType == null || jobType.isBlank()) {
            return null;
        }
        try{
            return JobType.valueOf(jobType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 jobType 값입니다:" + jobType);
        }
    }

    private JobResult parseJobResult(String result) {
        if (result == null || result.isBlank()) {
            return null;
        }
        try {
            return JobResult.valueOf(result.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 result 값입니다: " + result);
        }
    }


   public SyncJobSearchCommand toCommand() {
       return new SyncJobSearchCommand(
               parseJobType(jobType),
               indexInfoId,
               targetDate,
               worker,
               parseJobResult(result),
               sortField,
               SortDirection.from(sortDirection).name(),
               cursor,
               idAfter,
               size
       );
   }
}
