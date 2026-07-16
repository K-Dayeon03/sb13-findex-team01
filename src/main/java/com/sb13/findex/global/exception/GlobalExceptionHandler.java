package com.sb13.findex.global.exception;

import com.sb13.findex.indexinfo.exception.DuplicateIndexInfoException;
import com.sb13.findex.indexinfo.exception.ErrorResponse;
import com.sb13.findex.indexinfo.exception.IndexInfoNotFoundException;
import com.sb13.findex.sync.exception.AutoSyncConfigNotFoundException;
import com.sb13.findex.sync.exception.DuplicateAutoSyncConfigException;
import java.time.Instant;

import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
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
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException exception
    ) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "잘못된 요청입니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IndexInfoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIndexInfoNotFound(
            IndexInfoNotFoundException exception
    ) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                "지수 정보를 찾을 수 없습니다.",
                exception.getMessage()
        );
    }
    @ExceptionHandler(DuplicateIndexInfoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateIndexInfo(
            DuplicateIndexInfoException exception
    ) {
        return createErrorResponse(
                HttpStatus.CONFLICT,
                "지수 정보 등록에 실패했습니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(AutoSyncConfigNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAutoSyncConfigNotFoundException(
            AutoSyncConfigNotFoundException exception
    ) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                "자동 연동 설정을 찾을 수 없습니다.",
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateAutoSyncConfigException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAutoSyncConfigException(
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

    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatus status,
            String message,
            String details
    ) {
        ErrorResponse response = new ErrorResponse(
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
    public ResponseEntity<ErrorResponse> handleIndexDataNotFoundException(
        IndexDataNotFoundException exception
    ) {
        return createErrorResponse(
            HttpStatus.NOT_FOUND,
            "지수 데이터를 찾을 수 없습니다.",
            exception.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
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
    public ResponseEntity<ErrorResponse> handleDuplicateIndexDataException(
        DuplicateIndexDataException exception
    ) {
        return createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "데이터 충돌이 발생했습니다.",
            exception.getMessage()
        );
    }

}
