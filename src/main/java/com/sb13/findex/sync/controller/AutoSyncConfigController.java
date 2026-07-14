package com.sb13.findex.sync.controller;

import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.sync.dto.request.AutoSyncConfigCreateRequest;
import com.sb13.findex.sync.dto.request.AutoSyncConfigUpdateRequest;
import com.sb13.findex.sync.dto.response.AutoSyncConfigDto;
import com.sb13.findex.sync.service.AutoSyncConfigService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncConfigController {

    private final AutoSyncConfigService autoSyncConfigService;

    public AutoSyncConfigController(AutoSyncConfigService autoSyncConfigService) {
        this.autoSyncConfigService = autoSyncConfigService;
    }

    @PostMapping
    public ResponseEntity<AutoSyncConfigDto> create(@Valid @RequestBody AutoSyncConfigCreateRequest request) {
        AutoSyncConfigDto response = autoSyncConfigService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AutoSyncConfigDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AutoSyncConfigUpdateRequest request
    ) {
        AutoSyncConfigDto response = autoSyncConfigService.update(id, request.enabled());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public CursorPageResponse<AutoSyncConfigDto> search(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "indexInfoId") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection
    ) {
        AutoSyncConfigSearchCondition condition = new AutoSyncConfigSearchCondition(
                indexInfoId,
                enabled,
                cursor,
                idAfter,
                size,
                sortField,
                sortDirection
        );

        return autoSyncConfigService.getList(condition);
    }
}