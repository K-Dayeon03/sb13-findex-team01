package com.sb13.findex.sync.repository;

import com.sb13.findex.sync.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.sync.entity.AutoSyncConfig;

import java.util.List;

public interface AutoSyncConfigRepositoryCustom {
    List<AutoSyncConfig> search(AutoSyncConfigSearchCondition condition);
    long count(AutoSyncConfigSearchCondition condition);
}