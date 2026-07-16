package com.sb13.findex.sync.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 존재하지 않는 자동 연동 설정을 조회/수정 시 발생
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AutoSyncConfigNotFoundException extends RuntimeException {
    public AutoSyncConfigNotFoundException(Long id) {
        super("존재하지 않는 자동 연동 설정입니다. id=" + id);
    }
}
