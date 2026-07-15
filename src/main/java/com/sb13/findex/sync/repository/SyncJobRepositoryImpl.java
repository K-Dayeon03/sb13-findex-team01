package com.sb13.findex.sync.repository;


import com.sb13.findex.sync.dto.request.SyncJobSearchCommand;
import com.sb13.findex.sync.dto.request.SyncJobSortField;
import com.sb13.findex.sync.entity.SyncJob;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class SyncJobRepositoryImpl implements SyncJobRepositoryCustom{

    private static final String ALIAS = "s";

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<SyncJob> search(SyncJobSearchCommand command) {
        StringBuilder jpql = new StringBuilder();
        Map<String,Object> params = new HashMap<>();

        jpql.append("SELECT s FROM SyncJob s JOIN FETCH s.indexInfo WHERE 1 = 1 ");

        appendSearchConditions(jpql, params, command);
        appendCursorCondition(jpql, params, command);

        jpql.append(" ORDER BY ");
        jpql.append(resolveSortField(command.sortField()));
        jpql.append(isDesc(command.sortDirection()) ? " DESC" : " ASC");
        jpql.append(", s.id ");
        jpql.append(isDesc(command.sortDirection()) ? "DESC" : "ASC");

        TypedQuery<SyncJob> query = entityManager.createQuery(jpql.toString(), SyncJob.class);
        params.forEach(query::setParameter);

        int size = command.size() == null || command.size() <= 0 ? 10 : command.size();
        query.setMaxResults(size + 1);

        return query.getResultList();

    }


    @Override
    public long count(SyncJobSearchCommand command) {
        StringBuilder jpql = new StringBuilder();
        Map<String,Object> params = new HashMap<>();

        jpql.append("SELECT COUNT(s) FROM SyncJob s WHERE 1 = 1 ");

        appendSearchConditions(jpql, params, command);

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult();
    }

    private void appendSearchConditions(
            StringBuilder jpql,
            Map<String, Object> params,
            SyncJobSearchCommand command
    ){
        if (command.jobType() != null) {
            jpql.append("AND s.jobType = :jobType ");
            params.put("jobType", command.jobType());
        }

        if (command.indexInfoId() != null) {
            jpql.append("AND s.indexInfo.id = :indexInfoId ");
            params.put("indexInfoId", command.indexInfoId());
        }

        if (command.baseDateFrom() != null) {
            jpql.append("AND s.targetDate >= :baseDateFrom ");
            params.put("baseDateFrom", command.baseDateFrom());
        }

        if (command.baseDateTo() != null) {
            jpql.append("AND s.targetDate <= :baseDateTo ");
            params.put("baseDateTo", command.baseDateTo());
        }

        if (command.worker() != null && !command.worker().isEmpty()) {
            jpql.append("AND s.worker = :worker ");
            params.put("worker", command.worker());
        }

        if (command.jobTimeFrom() != null) {
            jpql.append("AND s.jobTime >= :jobTimeFrom ");
            params.put("jobTimeFrom", command.jobTimeFrom());
        }

        if (command.jobTimeTo() != null) {
            jpql.append("AND s.jobTime <= :jobTimeTo ");
            params.put("jobTimeTo", command.jobTimeTo());
        }

        if (command.result() != null) {
            jpql.append("AND s.result = :result ");
            params.put("result", command.result());
        }
    }

    private void appendCursorCondition(
            StringBuilder jpql,
            Map<String, Object> params,
            SyncJobSearchCommand command
    ){
        if (!command.hasCursor()){
            return;
        }

        String sortField = resolveSortField(command.sortField());

        if (isDesc(command.sortDirection())) {
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

        params.put("cursor", convertCursorValue(command.sortField(), command.cursor()));
        params.put("idAfter", command.idAfter());
    }

    private String resolveSortField(String sortField){
        return ALIAS + "." + SyncJobSortField.from(sortField).getQueryField();
    }

    private Object convertCursorValue(String sortField, String cursor){
        SyncJobSortField field = SyncJobSortField.from(sortField);
        try {
            return switch (field) {
                case TARGET_DATE -> LocalDate.parse(cursor);
                case JOB_TIME -> LocalDateTime.parse(cursor);
            };
        }catch (DateTimeParseException e) {
            throw new IllegalArgumentException("유효하지 않은 cursor 값입니다: " + cursor);
        }
    }

    private boolean isDesc(String sortDirection){
        return "desc".equalsIgnoreCase(sortDirection);
    }
}
