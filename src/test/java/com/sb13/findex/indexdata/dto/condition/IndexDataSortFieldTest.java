package com.sb13.findex.indexdata.dto.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sb13.findex.global.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

class IndexDataSortFieldTest {

    @Test
    void fromAcceptsExportAndTableSortFields() {
        assertThat(IndexDataSortField.from("id")).isEqualTo(IndexDataSortField.ID);
        assertThat(IndexDataSortField.from("indexInfoId")).isEqualTo(IndexDataSortField.INDEX_INFO_ID);
        assertThat(IndexDataSortField.from("indexClassification")).isEqualTo(IndexDataSortField.INDEX_CLASSIFICATION);
        assertThat(IndexDataSortField.from("indexName")).isEqualTo(IndexDataSortField.INDEX_NAME);
        assertThat(IndexDataSortField.from("sourceType")).isEqualTo(IndexDataSortField.SOURCE_TYPE);
        assertThat(IndexDataSortField.from("baseDate")).isEqualTo(IndexDataSortField.BASE_DATE);
    }

    @Test
    void fromAcceptsEnumNamesForCompatibility() {
        assertThat(IndexDataSortField.from("source_type")).isEqualTo(IndexDataSortField.SOURCE_TYPE);
        assertThat(IndexDataSortField.from("INDEX_NAME")).isEqualTo(IndexDataSortField.INDEX_NAME);
    }

    @Test
    void fromThrowsInvalidRequestExceptionForUnsupportedSortField() {
        assertThatThrownBy(() -> IndexDataSortField.from("createdAt"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("지원하지 않는 정렬 필드입니다");
    }
}
