package com.sb13.findex.sync.dto.request;

import com.sb13.findex.global.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncJobSortFieldTest {

    @Test
    @DisplayName("sortField가 없으면 기본값 JOB_TIME을 사용한다")
    void fromReturnsDefaultWhenValueIsBlank() {
        assertThat(SyncJobSortField.from(null)).isEqualTo(SyncJobSortField.JOB_TIME);
        assertThat(SyncJobSortField.from("")).isEqualTo(SyncJobSortField.JOB_TIME);
        assertThat(SyncJobSortField.from("   ")).isEqualTo(SyncJobSortField.JOB_TIME);
    }

    @Test
    @DisplayName("queryField 값도 sortField로 변환한다")
    void fromAcceptsQueryFieldValue() {
        assertThat(SyncJobSortField.from("jobTime")).isEqualTo(SyncJobSortField.JOB_TIME);
        assertThat(SyncJobSortField.from("targetDate")).isEqualTo(SyncJobSortField.TARGET_DATE);
    }


    @Test
    @DisplayName("sortField는 대소문자와 앞뒤 공백을 무시한다")
    void fromIgnoresCaseAndWhitespace() {
        assertThat(SyncJobSortField.from(" jobTime ")).isEqualTo(SyncJobSortField.JOB_TIME);
        assertThat(SyncJobSortField.from(" targetDate ")).isEqualTo(SyncJobSortField.TARGET_DATE);
        assertThat(SyncJobSortField.from(" job_time ")).isEqualTo(SyncJobSortField.JOB_TIME);
    }

    @Test
    @DisplayName("지원하지 않는 sortField 값이면 예외가 발생한다")
    void fromThrowsExceptionWhenValueIsInvalid() {
        assertThatThrownBy(() -> SyncJobSortField.from("createdAt"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("유효하지 않은 sortField 값입니다");
    }
}
