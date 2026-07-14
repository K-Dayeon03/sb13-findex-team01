package com.sb13.findex.indexdata.controller;
import com.sb13.findex.indexdata.dto.response.*;
import com.sb13.findex.indexdata.dto.command.IndexDataUpdateCommand;
import com.sb13.findex.indexdata.dto.request.IndexDataUpdateRequest;
import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexdata.dto.command.IndexDataCreateCommand;
import com.sb13.findex.indexdata.dto.request.IndexDataCreateRequest;
import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;
import com.sb13.findex.indexdata.entity.ChartPeriodType;
import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexdata.entity.UnitPeriodType;
import com.sb13.findex.indexdata.service.DashboardIndexDataService;
import java.util.List;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-data")
@Validated
public class IndexDataController {

    private final IndexDataService indexDataService;
    private final DashboardIndexDataService dashboardIndexDataService;

    public IndexDataController(
            IndexDataService indexDataService,
            DashboardIndexDataService dashboardIndexDataService
    ) {
        this.indexDataService = indexDataService;
        this.dashboardIndexDataService = dashboardIndexDataService;
    }

  @PostMapping
  public ResponseEntity<IndexDataResponse> createIndexData(
      @Valid @RequestBody IndexDataCreateRequest request) {
    IndexDataCreateCommand command = IndexDataCreateCommand.from(request);

    IndexDataResponse response = indexDataService.createIndexData(command);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexDataResponse> updateIndexData(
      @PathVariable Long id,
      @Valid @RequestBody IndexDataUpdateRequest request) {

    IndexDataUpdateCommand command = IndexDataUpdateCommand.from(request);
    IndexDataResponse response = indexDataService.updateIndexData(id, command);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteIndexData(@PathVariable Long id) {
    indexDataService.deleteIndexData(id);
    return ResponseEntity.noContent().build();
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

    @GetMapping(value = "/export", produces = "text/csv")
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
    @GetMapping("/performance/favorite")
    public List<IndexPerformanceResponse> getFavoritePerformance(
            @RequestParam(defaultValue = "DAILY") UnitPeriodType periodType
    ) {
        return dashboardIndexDataService.getFavoritePerformance(periodType);
    }
    //지수차트
    @GetMapping("/{id}/chart")
    public IndexChartResponse getIndexChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "MONTHLY") ChartPeriodType periodType
    ) {
        return dashboardIndexDataService.getIndexChart(id, periodType);
    }
    @GetMapping("/performance/rank")
    public List<RankedIndexPerformanceResponse> getPerformanceRank(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(defaultValue = "DAILY") UnitPeriodType periodType,
            @RequestParam(defaultValue = "10") @Min(1) int limit
    ) {
        return dashboardIndexDataService.getPerformanceRank(
                indexInfoId,
                periodType,
                limit
        );
    }

}