package com.sb13.findex.sync.dto;

import java.time.LocalDate;

public record AutoSyncTarget(
        Long indexInfoId,
        LocalDate lastSyncedDate
) {
}