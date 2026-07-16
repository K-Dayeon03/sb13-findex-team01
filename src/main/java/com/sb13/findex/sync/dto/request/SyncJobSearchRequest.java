package com.sb13.findex.sync.dto.request;

import com.sb13.findex.global.exception.InvalidRequestException;
import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobSearchRequest(
        String jobType,
        Long indexInfoId,
        LocalDate baseDateFrom,
        LocalDate baseDateTo,
        String worker,
        LocalDateTime jobTimeFrom,
        LocalDateTime jobTimeTo,
        String status,
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
            throw new InvalidRequestException("유효하지 않은 jobType 값입니다:" + jobType, e);
        }
    }

    private JobResult parseJobResult(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return JobResult.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("유효하지 않은 status 값입니다: " + status, e);
        }
    }


   public SyncJobSearchCommand toCommand() {
       return new SyncJobSearchCommand(
               parseJobType(jobType),
               indexInfoId,
               baseDateFrom,
               baseDateTo,
               worker,
               jobTimeFrom,
               jobTimeTo,
               parseJobResult(status),
               sortField,
               SortDirection.from(sortDirection).name(),
               cursor,
               idAfter,
               size
       );
   }
}
