package com.sb13.findex.global.exception;

import java.time.Instant;

import java.util.Locale;

import com.sb13.findex.global.exception.autosyncconfig.AutoSyncConfigNotFoundException;
import com.sb13.findex.global.exception.autosyncconfig.DuplicateAutoSyncConfigException;
import com.sb13.findex.global.exception.indexdata.DuplicateIndexDataException;
import com.sb13.findex.global.exception.indexdata.IndexDataNotFoundException;
import com.sb13.findex.global.exception.indexinfo.DuplicateIndexInfoException;
import com.sb13.findex.global.exception.indexinfo.IndexInfoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
            BindException exception
    ) {
        String details = extractDetails(exception);

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "잘못된 요청입니다.",
                details
        );
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestException(
            InvalidRequestException exception
    ) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "잘못된 요청입니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IndexInfoNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIndexInfoNotFound(
            IndexInfoNotFoundException exception
    ) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                "지수 정보를 찾을 수 없습니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateIndexInfoException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateIndexInfo(
            DuplicateIndexInfoException exception
    ) {
        return createErrorResponse(
                HttpStatus.CONFLICT,
                "지수 정보 등록에 실패했습니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(AutoSyncConfigNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAutoSyncConfigNotFoundException(
            AutoSyncConfigNotFoundException exception
    ) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                "자동 연동 설정을 찾을 수 없습니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateAutoSyncConfigException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateAutoSyncConfigException(
            DuplicateAutoSyncConfigException exception
    ) {
        return createErrorResponse(
                HttpStatus.CONFLICT,
                "자동 연동 설정 등록에 실패했습니다.",
                exception.getMessage()
        );
    }


    private String extractDetails(BindException exception) {
        return exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("유효하지 않은 요청입니다.");
    }

    private ResponseEntity<ApiErrorResponse> createErrorResponse(
            HttpStatus status,
            String message,
            String details
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                message,
                details
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(IndexDataNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIndexDataNotFoundException(
        IndexDataNotFoundException exception
    ) {
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "지수 데이터를 찾을 수 없습니다.",
            exception.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
        DataIntegrityViolationException exception
    ) {
        String specificMessage = exception.getMostSpecificCause().getMessage();

        if (specificMessage != null && (specificMessage.toLowerCase(Locale.ROOT).contains("duplicate") ||
            specificMessage.toLowerCase(Locale.ROOT).contains("unique"))) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "데이터 충돌이 발생했습니다.",
                "이미 동일한 데이터가 존재하거나 유니크 제약 조건을 위반했습니다."
            );
        }

        return createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "데이터베이스 오류가 발생했습니다.",
            "데이터 처리 중 문제가 발생했습니다."
        );
    }

    @ExceptionHandler(DuplicateIndexDataException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateIndexDataException(
        DuplicateIndexDataException exception
    ) {
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "데이터 충돌이 발생했습니다.",
            exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
            HttpServletRequest request,
            MethodArgumentTypeMismatchException exception
    ) {
        log.warn(
                "요청 파라미터 타입이 올바르지 않습니다. method={}, uri={}, parameter={}, value={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getName(),
                exception.getValue()
        );

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "잘못된 요청 파라미터입니다.",
                "'%s' 파라미터의 형식이 올바르지 않습니다."
                        .formatted(exception.getName())
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(
            HttpServletRequest request,
            MissingServletRequestParameterException exception
    ) {
        log.warn(
                "필수 요청 파라미터가 누락되었습니다. method={}, uri={}, parameter={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getParameterName()
        );

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "필수 요청 파라미터가 누락되었습니다.",
                "'%s' 파라미터는 필수입니다."
                        .formatted(exception.getParameterName())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpServletRequest request,
            HttpMessageNotReadableException exception
    ) {
        log.warn(
                "요청 본문의 형식이 올바르지 않습니다. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI()
        );

        log.debug("요청 본문 파싱 실패 상세 정보", exception);

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "요청 본문의 형식이 올바르지 않습니다.",
                "JSON 문법과 필드 타입을 확인해 주세요."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            HttpServletRequest request,
            Exception exception
    ) {
        log.error(
                "처리되지 않은 서버 오류가 발생했습니다. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다.",
                "서버 내부 오류가 발생했습니다."
        );

    }

}
