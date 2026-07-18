package com.sb13.findex.autosyncconfig.dto.projection;

import java.time.LocalDate;

public interface AutoSyncTargetProjection {
    Long getIndexInfoId();
    LocalDate getLatestBaseDate();
}