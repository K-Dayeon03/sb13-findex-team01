package com.sb13.findex.sync.dto.command;

import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncCommand(
      List<Integer> indexInfoIds,
      LocalDate baseDateFrom,
      LocalDate baseDateTo
) {
}
