package com.sb13.findex.sync.repository;


import com.sb13.findex.sync.dto.request.SyncJobSearchCondition;
import com.sb13.findex.sync.entity.SyncJob;

import java.util.List;

public interface SyncJobRepositoryCustom {
    List<SyncJob> search(SyncJobSearchCondition condition);
    long count(SyncJobSearchCondition condition);
}
