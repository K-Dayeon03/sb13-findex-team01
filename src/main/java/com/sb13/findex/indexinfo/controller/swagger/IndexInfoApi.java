package com.sb13.findex.indexinfo.controller.swagger;

import com.sb13.findex.global.exception.ApiErrorResponse;
import com.sb13.findex.indexinfo.dto.request.IndexInfoCreateRequest;
import com.sb13.findex.indexinfo.dto.request.IndexInfoSearchRequest;
import com.sb13.findex.indexinfo.dto.request.IndexInfoUpdateRequest;
import com.sb13.findex.indexinfo.dto.response.CursorPageResponse;
import com.sb13.findex.indexinfo.dto.response.IndexInfoResponse;
import com.sb13.findex.indexinfo.dto.response.IndexInfoSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface IndexInfoApi {

    @Operation(
            summary = "지수 정보 등록",
            description = "새로운 지수 정보를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "지수 정보 등록 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(필수 필드 누락 등)",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
    })
    ResponseEntity<IndexInfoResponse> create(
            @Valid @RequestBody IndexInfoCreateRequest request
    );

    @Operation(
            summary = "지수 정보 상세 조회",
            description = "ID로 지수 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "지수 정보 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "조회할 지수 정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    ResponseEntity<IndexInfoResponse> findById(
            @PathVariable Long id
    );

    @Operation(
            summary = "지수 정보 목록 조회",
            description = "검색 조건과 커서를 이용해 지수 정보 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "지수 정보 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(유효하지 않은 필드 값 등)",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    ResponseEntity<CursorPageResponse<IndexInfoResponse>> search(
            @Valid
            @ParameterObject
            @ModelAttribute IndexInfoSearchRequest request
    );

    @Operation(
            summary = "지수 정보 요약 목록 조회",
            description = "지수 정보 요약 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "지수 정보 요약 목록 조회 성공"
    )
    @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(
                    mediaType = "*/*",
                    schema = @Schema(
                            implementation = ApiErrorResponse.class
                    )
            )
    )
    ResponseEntity<List<IndexInfoSummaryResponse>> findSummaries();

    @Operation(
            summary = "지수 정보 수정",
            description = "지수 정보를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "지수 정보 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(유효하지 않은 필드 값 등)",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "수정할 지수 정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    ResponseEntity<IndexInfoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody IndexInfoUpdateRequest request
    );

    @Operation(
            summary = "지수 정보 삭제",
            description = "지수 정보를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "지수 정보 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "삭제할 지수 정보를 찾을 수 없음",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}