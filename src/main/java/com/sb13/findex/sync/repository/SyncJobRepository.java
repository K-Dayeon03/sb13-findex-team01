package com.sb13.findex.sync.repository;

import com.sb13.findex.sync.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> , SyncJobBulkRepository {

}
