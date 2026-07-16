package com.sb13.findex.sync.dto.projection;

import java.time.LocalDate;

public interface AutoSyncTargetProjection {
    Long getIndexInfoId();
    LocalDate getLatestBaseDate();
}