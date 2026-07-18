package com.sb13.findex.global.exception.request;

import com.sb13.findex.global.exception.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSortDirectionException extends InvalidRequestException {
    public InvalidSortDirectionException(String value) {
        super("지원하지 않는 정렬 방향입니다: " + value);
    }
}
