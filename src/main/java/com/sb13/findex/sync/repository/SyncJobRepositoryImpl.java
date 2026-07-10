package com.sb13.findex.sync.repository;


import com.sb13.findex.sync.dto.request.SyncJobSearchCondition;
import com.sb13.findex.sync.entity.SyncJob;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SyncJobRepositoryImpl implements SyncJobRepositoryCustom{

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<SyncJob> search(SyncJobSearchCondition condition) {
        StringBuilder jpql = new StringBuilder();
        Map<String,Object> params = new HashMap<>();

        jpql.append("SELECT s FROM SyncJob s WHERE 1 = 1 ");

        appendSearchConditions(jpql, params, condition);
        appendCursorCondition(jpql, params, condition);

        jpql.append(" ORDER BY ");
        jpql.append(resolveSortField(condition.sortField()));
        jpql.append(isDesc(condition.sortDirection()) ? " DESC" : " ASC");
        jpql.append(", s.id ");
        jpql.append(isDesc(condition.sortDirection()) ? "DESC" : "ASC");

        TypedQuery<SyncJob> query = entityManager.createQuery(jpql.toString(), SyncJob.class);
        params.forEach(query::setParameter);

        int size = condition.size() == null || condition.size() <= 0 ? 10 : condition.size();
        query.setMaxResults(size + 1);

        return query.getResultList();

    }


    @Override
    public long count(SyncJobSearchCondition condition) {
        StringBuilder jpql = new StringBuilder();
        Map<String,Object> params = new HashMap<>();

        jpql.append("SELECT COUNT(s) FROM SyncJob s WHERE 1 = 1 ");

        appendSearchConditions(jpql, params, condition);

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    private void appendSearchConditions(
            StringBuilder jpql,
            Map<String, Object> params,
            SyncJobSearchCondition condition
    ){
        if (condition.jobType() != null) {
            jpql.append("AND s.jobType = :jobType ");
            params.put("jobType", condition.jobType());
        }

        if (condition.indexInfoId() != null) {
            jpql.append("AND s.indexInfo.id = :indexInfoId ");
            params.put("indexInfoId", condition.indexInfoId());
        }

        if (condition.targetDate() != null) {
            jpql.append("AND s.targetDate = :targetDate ");
            params.put("targetDate", condition.targetDate());
        }

        if (condition.worker() != null && !condition.worker().isEmpty()) {
            jpql.append("AND s.worker = :worker ");
            params.put("worker", condition.worker());
        }

        if (condition.result() != null) {
            jpql.append("AND s.result = :result ");
            params.put("result", condition.result());
        }
    }

    private void appendCursorCondition(
            StringBuilder jpql,
            Map<String, Object> params,
            SyncJobSearchCondition condition
    ){
        if (!condition.hasCursor()){
            return;
        }

        String sortField = resolveSortField(condition.sortField());

        if (isDesc(condition.sortDirection())) {
            jpql.append(" AND (");
            jpql.append(sortField).append(" < :cursor ");
            jpql.append(" OR (");
            jpql.append(sortField).append(" = :cursor ");
            jpql.append(" AND s.id < :idAfter");
            jpql.append(")) ");
        } else {
            jpql.append(" AND (");
            jpql.append(sortField).append(" > :cursor ");
            jpql.append(" OR (");
            jpql.append(sortField).append(" = :cursor ");
            jpql.append(" AND s.id > :idAfter");
            jpql.append(")) ");
        }

        params.put("cursor", convertCursorValue(condition.sortField(), condition.cursor()));
        params.put("idAfter", condition.idAfter());
    }

    private String resolveSortField(String sortField){
        if (sortField == null || sortField.isEmpty()) {
            return "s.jobTime";
        }

        return switch (sortField){
            case "targetDate" -> "s.targetDate";
            case "jobTime" -> "s.jobTime";
            default -> "s.jobTime";
        };
    }

    private Object convertCursorValue(String sortField, String cursor){
        if ("targetDate".equals(sortField)) {
            return LocalDate.parse(cursor);
        }
        return LocalDateTime.parse(cursor);
    }

    private boolean isDesc(String sortDirection){
        return "desc".equalsIgnoreCase(sortDirection);
    }
}
