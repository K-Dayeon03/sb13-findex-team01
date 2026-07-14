package com.sb13.findex.sync.dto.request;

import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncRequest(
        @NotEmpty(message = "지수 정보 ID는 하나 이상 입력해야 합니다.")
        List<@NotNull(message = "지수 정보 ID는 필수입니다.")
                @Positive(message = "지수 정보 ID는 양수여야 합니다.") Long> indexInfoIds,
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate baseDateFrom,
        @NotNull(message = "종료일은 필수입니다.")
        LocalDate baseDateTo
) {

    @AssertTrue(message = "시작일은 종료일보다 늦을 수 없습니다.")
    public boolean isValidDateRange() {
        if (baseDateFrom == null || baseDateTo == null) {
            return true;
        }

        return !baseDateFrom.isAfter(baseDateTo);
    }

    public IndexDataSyncCommand toCommand() {

        return new IndexDataSyncCommand(indexInfoIds, baseDateFrom, baseDateTo);
    }
}
