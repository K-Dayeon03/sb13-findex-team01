package com.sb13.findex.sync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSortFieldException extends IllegalArgumentException {
    public InvalidSortFieldException(String value) {
        super("지원하지 않는 정렬 필드입니다: " + value);
    }
}