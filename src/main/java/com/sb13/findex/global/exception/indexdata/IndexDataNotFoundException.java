package com.sb13.findex.global.exception.indexdata;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IndexDataNotFoundException extends RuntimeException {

    public IndexDataNotFoundException(Long indexInfoId) {
        super("지수 데이터가 존재하지 않습니다. indexInfoId=" + indexInfoId);
    }
}