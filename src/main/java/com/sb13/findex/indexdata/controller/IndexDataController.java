package com.sb13.findex.indexdata.controller;

import com.sb13.findex.indexdata.dto.CursorPageResponse;
import com.sb13.findex.indexdata.dto.IndexDataCreateCommand;
import com.sb13.findex.indexdata.dto.IndexDataCreateRequest;
import com.sb13.findex.indexdata.dto.IndexDataResponse;
import com.sb13.findex.indexdata.dto.IndexDataSearchCondition;
import com.sb13.findex.indexdata.service.IndexDataService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
public class IndexDataController {

    private final IndexDataService indexDataService;

    public IndexDataController(IndexDataService indexDataService) {
        this.indexDataService = indexDataService;
    }

  @PostMapping
  public ResponseEntity<IndexDataResponse> createIndexData(@Valid @RequestBody IndexDataCreateRequest request) {
    IndexDataCreateCommand command = IndexDataCreateCommand.from(request);

    IndexDataResponse response = indexDataService.createIndexData(command);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

    @GetMapping
    public CursorPageResponse<IndexDataResponse> search(
            @RequestParam(required = false) Long indexInfoId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "baseDate") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection
    ) {
        IndexDataSearchCondition condition = new IndexDataSearchCondition(
                indexInfoId,
                startDate,
                endDate,
                cursor,
                idAfter,
                size,
                sortField,
                sortDirection
        );

        return indexDataService.search(condition);
    }

    @GetMapping(value = "/api/index-data/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Long indexInfoId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false, defaultValue = "baseDate") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection
    ) {
        IndexDataSearchCondition condition = new IndexDataSearchCondition(
                indexInfoId,
                startDate,
                endDate,
                null,
                null,
                null,
                sortField,
                sortDirection
        );

        byte[] csv = indexDataService.exportCsv(condition);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"index-data.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}