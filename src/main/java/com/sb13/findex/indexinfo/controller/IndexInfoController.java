package com.sb13.findex.indexinfo.controller;

import com.sb13.findex.indexinfo.controller.swagger.IndexInfoApi;
import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.indexinfo.dto.command.IndexInfoUpdateCommand;
import com.sb13.findex.indexinfo.dto.request.IndexInfoCreateRequest;
import com.sb13.findex.indexinfo.dto.request.IndexInfoSearchRequest;
import com.sb13.findex.indexinfo.dto.request.IndexInfoUpdateRequest;
import com.sb13.findex.indexinfo.dto.response.CursorPageResponse;
import com.sb13.findex.indexinfo.dto.response.IndexInfoResponse;
import com.sb13.findex.indexinfo.dto.response.IndexInfoSummaryResponse;
import com.sb13.findex.indexinfo.service.IndexInfoService;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController implements IndexInfoApi {

    private final IndexInfoService indexInfoService;

    @PostMapping
    public ResponseEntity<IndexInfoResponse> create(@Valid @RequestBody IndexInfoCreateRequest request) {

        IndexInfoCreateCommand command = request.toCommand();

        IndexInfoResponse response =
                indexInfoService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<IndexInfoResponse> findById(@PathVariable Long id) {

        IndexInfoResponse response = indexInfoService.findById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<IndexInfoResponse>> search(
            @Valid @ModelAttribute IndexInfoSearchRequest request
    ) {
        CursorPageResponse<IndexInfoResponse> response =
                indexInfoService.search(request.toCondition());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<IndexInfoSummaryResponse>> findSummaries() {

        List<IndexInfoSummaryResponse> response = indexInfoService.findSummaries();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IndexInfoResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody IndexInfoUpdateRequest request) {

        IndexInfoUpdateCommand command = request.toCommand();

        IndexInfoResponse response =
                indexInfoService.update(id, command);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        indexInfoService.delete(id);

        return ResponseEntity.noContent().build();
    }

}


