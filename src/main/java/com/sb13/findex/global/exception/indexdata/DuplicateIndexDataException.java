package com.sb13.findex.global.exception.indexdata;

public class DuplicateIndexDataException extends RuntimeException {

  public DuplicateIndexDataException() {
    super("해당 날짜의 지수 데이터가 이미 존재합니다.");
  }
}