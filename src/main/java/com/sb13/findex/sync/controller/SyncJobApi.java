package com.sb13.findex.sync.controller;

import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.sync.dto.request.IndexDataSyncRequest;
import com.sb13.findex.sync.dto.response.SyncJobDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "연동 작업 관리", description = "연동 작업(SyncJob) 조회 및 실행 API")
public interface SyncJobApi {

    @Operation(summary = "연동 작업 목록 조회", description = "연동 작업 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연동 작업 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<CursorPageResponse<SyncJobDto>> search(
            @Parameter(description = "연동 작업 유형 (INDEX_INFO, INDEX_DATA)") String jobType,
            @Parameter(description = "지수 정보 ID") Long indexInfoId,
            @Parameter(description = "대상 날짜 (부터)") LocalDate baseDateFrom,
            @Parameter(description = "대상 날짜 (까지)") LocalDate baseDateTo,
            @Parameter(description = "작업자 (요청 IP 또는 system)") String worker,
            @Parameter(description = "작업 일시 (부터)") LocalDateTime jobTimeFrom,
            @Parameter(description = "작업 일시 (까지)") LocalDateTime jobTimeTo,
            @Parameter(description = "결과 (SUCCESS, FAILED)") String status,
            @Parameter(description = "정렬 필드 (targetDate, jobTime)") String sortField,
            @Parameter(description = "정렬 방향 (asc, desc)") String sortDirection,
            @Parameter(description = "커서 (이전 페이지 마지막 정렬 값)") String cursor,
            @Parameter(description = "이전 페이지 마지막 요소 ID") Long idAfter,
            @Parameter(description = "페이지 크기") Integer size
    );

    @Operation(summary = "지수 정보 연동", description = "Open API를 통해 지수 정보를 연동합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연동할 데이터 없음"),
            @ApiResponse(responseCode = "201", description = "연동 작업 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")

    })
    ResponseEntity<List<SyncJobDto>> syncIndexInfos();

    @Operation(summary = "지수 데이터 연동", description = "Open API를 통해 지수 데이터를 연동합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연동할 데이터 없음"),
            @ApiResponse(responseCode = "201", description = "연동 작업 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(유효하지 않는 날짜 범위 등)"),
            @ApiResponse(responseCode = "404", description = "지수 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<List<SyncJobDto>> syncIndexData(@Valid @RequestBody IndexDataSyncRequest request);
}
