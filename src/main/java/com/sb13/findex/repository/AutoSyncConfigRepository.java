package com.sb13.findex.repository;

import com.sb13.findex.entity.AutoSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long> {
    List<AutoSyncConfig> findByIsActive(Boolean isActive);
}