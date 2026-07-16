package com.sb13.findex.indexdata.dto.condition;

import com.sb13.findex.global.exception.InvalidRequestException;
import java.util.Arrays;
//정렬 가능 필드만 허용
//스웨거 지수데이터목록 조회 response부분
public enum IndexDataSortField {
    ID("id"),
    INDEX_INFO_ID("indexInfoId"),
    INDEX_CLASSIFICATION("indexClassification"),
    INDEX_NAME("indexName"),
    BASE_DATE("baseDate"),
    SOURCE_TYPE("sourceType"),
    MARKET_PRICE("marketPrice"),
    CLOSING_PRICE("closingPrice"),
    HIGH_PRICE("highPrice"),
    LOW_PRICE("lowPrice"),
    VERSUS("versus"),
    FLUCTUATION_RATE("fluctuationRate"),
    TRADING_QUANTITY("tradingQuantity"),
    TRADING_PRICE("tradingPrice"),
    MARKET_TOTAL_AMOUNT("marketTotalAmount");

    private final String value;

    IndexDataSortField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IndexDataSortField from(String value) {
        if (value == null || value.isBlank()) {
            return BASE_DATE;
        }

        String normalizedValue = value.trim();

        return Arrays.stream(values())
                .filter(field ->
                        field.value.equalsIgnoreCase(normalizedValue)
                                || field.name().equalsIgnoreCase(normalizedValue)
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidRequestException("지원하지 않는 정렬 필드입니다: " + value));
    }
}
