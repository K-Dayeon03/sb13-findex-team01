package com.sb13.findex.sync.controller;


import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.sync.controller.swagger.SyncJobApi;
import com.sb13.findex.sync.dto.request.IndexDataSyncRequest;
import com.sb13.findex.sync.dto.request.SyncJobSearchRequest;
import com.sb13.findex.sync.dto.response.SyncJobDto;
import com.sb13.findex.sync.service.SyncJobManager;
import com.sb13.findex.sync.service.SyncJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobApi {

    private final SyncJobService syncJobService;

    private final SyncJobManager syncJobManager;

    @Override
    @GetMapping
    public ResponseEntity<CursorPageResponse<SyncJobDto>> search(
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) LocalDate baseDateFrom,
            @RequestParam(required = false) LocalDate baseDateTo,
            @RequestParam(required = false) String worker,
            @RequestParam(required = false) LocalDateTime jobTimeFrom,
            @RequestParam(required = false) LocalDateTime jobTimeTo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) Integer size
    ) {
        SyncJobSearchRequest request = new SyncJobSearchRequest(
                jobType,
                indexInfoId,
                baseDateFrom,
                baseDateTo,
                worker,
                jobTimeFrom,
                jobTimeTo,
                status,
                sortField,
                sortDirection,
                cursor,
                idAfter,
                size
        );
        CursorPageResponse<SyncJobDto> response = syncJobService.search(request.toCommand());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfos() {
        List<SyncJobDto> syncJobDtos = syncJobManager.syncIndexInfos();
        HttpStatus status = syncJobDtos.isEmpty() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(syncJobDtos);
    }

    @Override
    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> syncIndexData(@Valid @RequestBody IndexDataSyncRequest request) {
        List<SyncJobDto> syncJobDtos = syncJobManager.syncIndexDataList(request.toCreateSyncCommands());
        HttpStatus status = syncJobDtos.isEmpty() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(syncJobDtos);
    }


}
