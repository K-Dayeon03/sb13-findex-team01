package com.sb13.findex.autosyncconfig.repository;

import com.sb13.findex.autosyncconfig.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.autosyncconfig.entity.AutoSyncConfig;

import java.util.List;

public interface AutoSyncConfigRepositoryCustom {
    List<AutoSyncConfig> search(AutoSyncConfigSearchCondition condition);
    long count(AutoSyncConfigSearchCondition condition);
}