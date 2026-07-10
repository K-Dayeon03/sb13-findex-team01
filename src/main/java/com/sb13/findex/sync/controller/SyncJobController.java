package com.sb13.findex.sync.controller;


import com.sb13.findex.indexdata.dto.CursorPageResponse;
import com.sb13.findex.sync.dto.request.SyncJobSearchCondition;
import com.sb13.findex.sync.dto.response.SyncJobDto;
import com.sb13.findex.sync.entity.JobResult;
import com.sb13.findex.sync.entity.JobType;
import com.sb13.findex.sync.service.SyncJobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

    private final SyncJobService syncJobService;

    public SyncJobController(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @GetMapping
    public CursorPageResponse<SyncJobDto> search(
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) LocalDate targetDate,
            @RequestParam(required = false) String worker,
            @RequestParam(required = false) JobResult result,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) Integer size
            ){
        SyncJobSearchCondition condition = new SyncJobSearchCondition(
                jobType,
                indexInfoId,
                targetDate,
                worker,
                result,
                sortField,
                sortDirection,
                cursor,
                idAfter,
                size
        );
        return syncJobService.search(condition);
    }
}
