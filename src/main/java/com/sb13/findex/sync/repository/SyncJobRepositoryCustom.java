package com.sb13.findex.sync.repository;


import com.sb13.findex.sync.dto.request.SyncJobSearchCommand;
import com.sb13.findex.sync.entity.SyncJob;

import java.util.List;

public interface SyncJobRepositoryCustom {
    List<SyncJob> search(SyncJobSearchCommand command);
    long count(SyncJobSearchCommand command);
}
