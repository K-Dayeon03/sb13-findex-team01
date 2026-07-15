package com.sb13.findex.sync.controller;

import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.sync.dto.request.AutoSyncConfigUpdateRequest;
import com.sb13.findex.sync.dto.response.AutoSyncConfigDto;
import com.sb13.findex.sync.service.AutoSyncConfigService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncConfigController {

    private final AutoSyncConfigService autoSyncConfigService;

    public AutoSyncConfigController(AutoSyncConfigService autoSyncConfigService) {
        this.autoSyncConfigService = autoSyncConfigService;
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
    public ResponseEntity<CursorPageResponse<AutoSyncConfigDto>> search(
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

        // ai 및 구영님 피드백에 따라 반환 타입 수정
        CursorPageResponse<AutoSyncConfigDto> response = autoSyncConfigService.getList(condition);
        return ResponseEntity.ok(response);
    }
}