package com.sb13.findex.indexdata.repository;

import com.sb13.findex.indexdata.entity.IndexData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

}
