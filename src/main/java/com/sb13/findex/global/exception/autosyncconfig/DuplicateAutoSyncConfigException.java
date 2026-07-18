package com.sb13.findex.global.exception.autosyncconfig;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 이미 등록된 지수에 자동 연동 설정을 중복 등록 할 때 발생
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAutoSyncConfigException extends RuntimeException {
    public DuplicateAutoSyncConfigException(Long indexInfoId) {
        super("이미 등록된 지수의 자동 연동 설정입니다. indexInfoId=" + indexInfoId);
    }
}
