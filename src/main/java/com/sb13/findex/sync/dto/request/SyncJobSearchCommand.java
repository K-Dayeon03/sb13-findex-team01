package com.sb13.findex.sync.dto.request;

import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobSearchCommand(
        JobType jobType,
        Long indexInfoId,
        LocalDate baseDateFrom,
        LocalDate baseDateTo,
        String worker,
        LocalDateTime jobTimeFrom,
        LocalDateTime jobTimeTo,
        JobResult result,
        String sortField,
        String sortDirection,
        String cursor,
        Long idAfter,
        Integer size
) {
    public boolean hasCursor() {
        return cursor != null && !cursor.isBlank() && idAfter != null;
    }

}
