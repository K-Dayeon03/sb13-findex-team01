package com.sb13.findex.indexinfo.repository;

import com.sb13.findex.indexinfo.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;


@Repository
public interface IndexInfoRepository extends IndexInfoRepositoryCustom, JpaRepository<IndexInfo, Long> {

    boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);

    Optional<IndexInfo> findByIndexClassificationAndIndexName(
            String indexClassification,
            String indexName
    );
}
