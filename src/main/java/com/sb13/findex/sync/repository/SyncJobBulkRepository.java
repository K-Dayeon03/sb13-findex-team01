package com.sb13.findex.sync.repository;

import com.sb13.findex.sync.dto.command.IndexInfoKey;

import java.util.List;

public interface SyncJobBulkRepository {
    int saveAll(String worker, List<IndexInfoKey> indexInfoKeys);
}
