package com.sb13.findex.indexdata.controller;

import com.sb13.findex.indexdata.dto.request.IndexDataCreateRequest;
import com.sb13.findex.indexdata.dto.request.IndexDataUpdateRequest;
import com.sb13.findex.indexdata.dto.response.*;
import com.sb13.findex.indexdata.entity.ChartPeriodType;
import com.sb13.findex.indexdata.entity.UnitPeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Tag(name = "지수 데이터 API", description = "지수 데이터(IndexData) 등록, 조회, 수정, 삭제 API")
public interface IndexDataApi {

  @Operation(summary = "지수 데이터 등록", description = "새로운 지수 데이터를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "지수 데이터 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)"),
      @ApiResponse(responseCode = "404", description = "참조하는 지수 정보를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<IndexDataResponse> createIndexData(@Valid @RequestBody IndexDataCreateRequest request);

  @Operation(summary = "지수 데이터 수정", description = "기존 지수 데이터의 가격 및 거래량 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 데이터 값 등)"),
      @ApiResponse(responseCode = "404", description = "수정할 지수 데이터를 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<IndexDataResponse> updateIndexData(Long id, @Valid @RequestBody IndexDataUpdateRequest request);

    @Operation(summary = "지수 데이터 물리 삭제", description = "해당 ID의 지수 데이터를 DB에서 완전히 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "지수 데이터 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "삭제할 지수 데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    ResponseEntity<Void> deleteIndexData(Long id);

    @Operation(summary = "지수 데이터 목록 검색", description = "조건에 맞는 지수 데이터 목록을 커서 기반 페이징으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지수 데이터 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    CursorPageResponse<IndexDataResponse> search(
            @Parameter(description = "지수 정보 ID") Long indexInfoId,
            @Parameter(description = "시작 일자 (YYYY-MM-DD)") LocalDate startDate,
            @Parameter(description = "종료 일자 (YYYY-MM-DD)") LocalDate endDate,
            @Parameter(description = "커서 (다음 페이지 시작점)") String cursor,
            @Parameter(description = "이전 페이지 마지막 요소 ID") Long idAfter,
            @Parameter(description = "페이지 크기") Integer size,
            @Parameter(description = "정렬 필드 (baseDate, marketPrice 등)") String sortField,
            @Parameter(description = "정렬 방향 (asc, desc)") String sortDirection
    );

  @Operation(summary = "지수 데이터 CSV 다운로드", description = "검색 조건에 맞는 데이터를 CSV 파일로 추출합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "CSV 파일 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  ResponseEntity<StreamingResponseBody> exportCsv(
          @Parameter(description = "지수 정보 ID") Long indexInfoId,
          @Parameter(description = "시작 일자 (YYYY-MM-DD)") LocalDate startDate,
          @Parameter(description = "종료 일자 (YYYY-MM-DD)") LocalDate endDate,
          @Parameter(description = "정렬 필드") String sortField,
          @Parameter(description = "정렬 방향") String sortDirection
  );
  
    @Operation(summary = "관심 지수 성과 조회", description = "사용자가 등록한 관심 지수의 기간별 성과를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관심 지수 성과 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    List<IndexPerformanceResponse> getFavoritePerformance(
            @Parameter(description = "성과 기간 유형 (DAILY, WEEKLY, MONTHLY)") UnitPeriodType periodType
    );

    @Operation(summary = "지수 차트 데이터 조회", description = "특정 지수의 기간별 차트 데이터를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 기간 유형 등)"),
            @ApiResponse(responseCode = "404", description = "지수 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    IndexChartResponse getIndexChart(
            @Parameter(description = "지수 정보 ID") Long id,
            @Parameter(description = "차트 기간 유형 (MONTHLY, QUARTERLY, YEARLY)") ChartPeriodType periodType
    );
  @Operation(summary = "지수 성과 순위 조회", description = "기간별 성과가 가장 좋은 지수들의 순위를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성과 랭킹 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 기간 유형 등)"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  List<RankedIndexPerformanceResponse> getPerformanceRank(
      @Parameter(description = "지수 정보 ID") Long indexInfoId,
      @Parameter(description = "성과 기간 유형 (DAILY, WEEKLY, MONTHLY)") UnitPeriodType periodType,
      @Parameter(description = "최대 랭킹 수") @Min(1) int limit
  );
}
