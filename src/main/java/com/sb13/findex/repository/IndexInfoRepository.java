package com.sb13.findex.repository;

import com.sb13.findex.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

@Repository
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

    boolean existsByIndexClassificationAndIndexName(String indexClassification, String indexName);


    @Query("SELECT i FROM IndexInfo i " +
            "WHERE i.indexClassification LIKE %:classification% " +
            "AND i.indexName LIKE %:name% " +
            "AND i.favorite = :isFavorite")
    Page<IndexInfo> searchIndices(
            @Param("classification") String indexClassification,
            @Param("name") String indexName,
            @Param("isFavorite") boolean isFavorite,
            Pageable pageable
    );

}
