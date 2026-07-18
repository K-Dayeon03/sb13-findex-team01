package com.sb13.findex.global.exception.indexinfo;

public class DuplicateIndexInfoException extends RuntimeException {

    public DuplicateIndexInfoException() {
        super("이미 존재하는 지수 정보입니다.");
    }
}
