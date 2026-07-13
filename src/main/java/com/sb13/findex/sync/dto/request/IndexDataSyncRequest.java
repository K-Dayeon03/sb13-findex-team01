package com.sb13.findex.sync.dto.request;

import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;

import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncRequest(
      List<Integer> indexInfoIds,
      LocalDate baseDateFrom,
      LocalDate baseDateTo
) {

   public IndexDataSyncCommand toCommand() {
      return new IndexDataSyncCommand(indexInfoIds, baseDateFrom, baseDateTo);
   }
}
