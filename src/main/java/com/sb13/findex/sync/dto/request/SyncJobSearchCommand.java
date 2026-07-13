package com.sb13.findex.sync.dto.request;

import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;

import java.time.LocalDate;

public record SyncJobSearchCommand(
        JobType jobType,
        Long indexInfoId,
        LocalDate targetDate,
        String worker,
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
