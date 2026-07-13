package com.sb13.findex.sync.dto.command;

import com.sb13.findex.sync.dto.response.StockMarketIndex;

/**
 * 공공데이터 API 응답으로부터 지수 정보를 식별하기 위한 키입니다.
 *
 * @param indexClassification 지수 분류명
 * @param indexName           지수명
 */
public record IndexInfoKey(
        String indexClassification,
        String indexName
) {

    public static IndexInfoKey from(StockMarketIndex index) {
        return new IndexInfoKey(
                index.idxCsf(),
                index.idxNm()
        );
    }
}
