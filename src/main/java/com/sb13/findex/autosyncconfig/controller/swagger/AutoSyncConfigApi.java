package com.sb13.findex.autosyncconfig.controller.swagger;

import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.global.exception.ApiErrorResponse;
import com.sb13.findex.autosyncconfig.dto.request.AutoSyncConfigUpdateRequest;
import com.sb13.findex.autosyncconfig.dto.response.AutoSyncConfigDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface AutoSyncConfigApi {

    @Operation(
            summary = "자동 연동 설정 수정",
            description = "지수별 자동 연동 활성화 여부를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자동 연동 설정 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(유효하지 않은 필드 값 등)",
                    content = @Content(mediaType = "*/*", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "수정할 자동 연동 설정을 찾을 수 없음",
                    content = @Content(mediaType = "*/*", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(mediaType = "*/*", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<AutoSyncConfigDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AutoSyncConfigUpdateRequest request
    );

    @Operation(
            summary = "자동 연동 설정 목록 조회",
            description = "지수, 활성화 여부 조건과 커서를 이용해 자동 연동 설정 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "자동 연동 설정 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(지원하지 않는 정렬 필드/방향 등)",
                    content = @Content(mediaType = "*/*", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(mediaType = "*/*", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<CursorPageResponse<AutoSyncConfigDto>> search(
            @Parameter(description = "지수 ID") @RequestParam(required = false) Long indexInfoId,
            @Parameter(description = "활성화 여부") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "커서") @RequestParam(required = false) String cursor,
            @Parameter(description = "이전 페이지 마지막 요소 ID") @RequestParam(required = false) Long idAfter,
            @Parameter(description = "페이지 크기") @RequestParam(required = false) Integer size,
            @Parameter(description = "정렬 필드 (indexInfoId, enabled)") @RequestParam(required = false, defaultValue = "indexInfoId") String sortField,
            @Parameter(description = "정렬 방향 (asc, desc)") @RequestParam(required = false, defaultValue = "desc") String sortDirection
    );
}