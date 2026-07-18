package com.sb13.findex.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb13.findex.global.exception.autosyncconfig.AutoSyncConfigNotFoundException;
import com.sb13.findex.global.exception.autosyncconfig.DuplicateAutoSyncConfigException;
import com.sb13.findex.global.exception.indexdata.DuplicateIndexDataException;
import com.sb13.findex.global.exception.indexdata.IndexDataNotFoundException;
import com.sb13.findex.global.exception.indexinfo.DuplicateIndexInfoException;
import com.sb13.findex.global.exception.indexinfo.IndexInfoNotFoundException;
import com.sb13.findex.global.exception.request.InvalidSortDirectionException;
import com.sb13.findex.global.exception.request.InvalidSortFieldException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleInvalidRequestExceptionReturnsBadRequest() {
        // given
        InvalidRequestException exception =
                new InvalidRequestException("지원하지 않는 정렬 필드입니다: source");

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleInvalidRequestException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().details()).isEqualTo("지원하지 않는 정렬 필드입니다: source");
    }

    @Test
    void invalidSortFieldExceptionReturnsBadRequestThroughMvcExceptionHandling() throws Exception {
        // given
        MockMvc mockMvc = standaloneSetup(new SortExceptionController())
                .setControllerAdvice(exceptionHandler)
                .build();

        // when & then
        mockMvc.perform(get("/test/invalid-sort-field"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.details").value("지원하지 않는 정렬 필드입니다: source"));
    }

    @Test
    void invalidSortDirectionExceptionReturnsBadRequestThroughMvcExceptionHandling() throws Exception {
        // given
        MockMvc mockMvc = standaloneSetup(new SortExceptionController())
                .setControllerAdvice(exceptionHandler)
                .build();

        // when & then
        mockMvc.perform(get("/test/invalid-sort-direction"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.details").value("지원하지 않는 정렬 방향입니다: sideways"));
    }

    @Test
    void handleIndexInfoNotFoundReturnsNotFound() {
        // given
        IndexInfoNotFoundException exception = new IndexInfoNotFoundException(1L);

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleIndexInfoNotFound(exception);

        // then
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "지수 정보를 찾을 수 없습니다.");
    }

    @Test
    void handleIndexDataNotFoundReturnsNotFound() {
        // given
        IndexDataNotFoundException exception = new IndexDataNotFoundException(1L);

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleIndexDataNotFoundException(exception);

        // then
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "지수 데이터를 찾을 수 없습니다.");
    }

    @Test
    void handleDuplicateIndexInfoReturnsConflict() {
        // given
        DuplicateIndexInfoException exception = new DuplicateIndexInfoException();

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleDuplicateIndexInfo(exception);

        // then
        assertErrorResponse(response, HttpStatus.CONFLICT, "지수 정보 등록에 실패했습니다.");
    }

    @Test
    void handleDuplicateIndexDataReturnsBadRequest() {
        // given
        DuplicateIndexDataException exception = new DuplicateIndexDataException();

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleDuplicateIndexDataException(exception);

        // then
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "데이터 충돌이 발생했습니다.");
    }

    @Test
    void handleAutoSyncConfigNotFoundReturnsNotFound() {
        // given
        AutoSyncConfigNotFoundException exception = new AutoSyncConfigNotFoundException(1L);

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleAutoSyncConfigNotFoundException(exception);

        // then
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "자동 연동 설정을 찾을 수 없습니다.");
    }

    @Test
    void handleDuplicateAutoSyncConfigReturnsConflict() {
        // given
        DuplicateAutoSyncConfigException exception = new DuplicateAutoSyncConfigException(1L);

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleDuplicateAutoSyncConfigException(exception);

        // then
        assertErrorResponse(response, HttpStatus.CONFLICT, "자동 연동 설정 등록에 실패했습니다.");
    }

    @Test
    void handleDataIntegrityViolationDuplicateReturnsBadRequest() {
        // given
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "constraint violation",
                new RuntimeException("duplicate key value violates unique constraint")
        );

        // when
        ResponseEntity<ApiErrorResponse> response =
                exceptionHandler.handleDataIntegrityViolationException(exception);

        // then
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "데이터 충돌이 발생했습니다.");
        assertThat(response.getBody().details())
                .isEqualTo("이미 동일한 데이터가 존재하거나 유니크 제약 조건을 위반했습니다.");
    }

    private void assertErrorResponse(
            ResponseEntity<ApiErrorResponse> response,
            HttpStatus status,
            String message
    ) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(status.value());
        assertThat(response.getBody().message()).isEqualTo(message);
        assertThat(response.getBody().details()).isNotBlank();
    }

    @RestController
    private static class SortExceptionController {

        @GetMapping("/test/invalid-sort-field")
        void throwInvalidSortFieldException() {
            throw new InvalidSortFieldException("source");
        }

        @GetMapping("/test/invalid-sort-direction")
        void throwInvalidSortDirectionException() {
            throw new InvalidSortDirectionException("sideways");
        }
    }
}
