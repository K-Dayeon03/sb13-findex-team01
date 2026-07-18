package com.sb13.findex.global.exception.indexinfo;

public class IndexInfoNotFoundException extends RuntimeException {

    public IndexInfoNotFoundException(Long id) {
        super("존재하지 않는 지수 정보입니다. ID: " + id);
    }

    public IndexInfoNotFoundException(
            String indexClassification,
            String indexName
    ) {
        super("지수 정보를 찾을 수 없습니다. " + "indexClassification=" + indexClassification + ", indexName=" + indexName);
    }

}
