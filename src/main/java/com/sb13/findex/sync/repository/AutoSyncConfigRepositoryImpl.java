package com.sb13.findex.sync.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSearchCondition;
import com.sb13.findex.sync.dto.condition.AutoSyncConfigSortField;
import com.sb13.findex.sync.entity.AutoSyncConfig;
import com.sb13.findex.sync.exception.InvalidSortDirectionException;
import org.springframework.stereotype.Repository;
import com.sb13.findex.indexinfo.entity.QIndexInfo;
import com.sb13.findex.sync.entity.QAutoSyncConfig;

import java.util.List;

@Repository
public class AutoSyncConfigRepositoryImpl implements AutoSyncConfigRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AutoSyncConfigRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<AutoSyncConfig> search(AutoSyncConfigSearchCondition condition) {
        QAutoSyncConfig autoSyncConfig = QAutoSyncConfig.autoSyncConfig;
        QIndexInfo indexInfo = QIndexInfo.indexInfo;

        int size = condition.resolvedSize();

        return queryFactory
                .selectFrom(autoSyncConfig)
                .join(autoSyncConfig.indexInfo, indexInfo).fetchJoin()
                .where(
                        filterCondition(condition),
                        cursorCondition(condition)
                )
                .orderBy(
                        sortOrder(condition),
                        idOrder(condition)
                )
                .limit(size + 1)
                .fetch();
    }

    @Override
    public long count(AutoSyncConfigSearchCondition condition) {
        QAutoSyncConfig autoSyncConfig = QAutoSyncConfig.autoSyncConfig;

        Long count = queryFactory
                .select(autoSyncConfig.count())
                .from(autoSyncConfig)
                .where(filterCondition(condition))
                .fetchOne();

        return count == null ? 0L : count;
    }

    private BooleanBuilder filterCondition(AutoSyncConfigSearchCondition condition) {
        QAutoSyncConfig autoSyncConfig = QAutoSyncConfig.autoSyncConfig;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.indexInfoId() != null) {
            builder.and(autoSyncConfig.indexInfo.id.eq(condition.indexInfoId()));
        }

        if (condition.enabled() != null) {
            builder.and(autoSyncConfig.enabled.eq(condition.enabled()));
        }

        return builder;
    }

    private BooleanBuilder cursorCondition(AutoSyncConfigSearchCondition condition) {
        if (condition.cursor() == null || condition.cursor().isBlank() || condition.idAfter() == null) {
            return null;
        }

        AutoSyncConfigSortField sortField = AutoSyncConfigSortField.from(condition.sortField());
        boolean ascending = isAscending(condition.sortDirection());

        return switch (sortField) {
            case INDEX_INFO_ID -> compareIndexInfoIdCursor(
                    Long.valueOf(condition.cursor()), condition.idAfter(), ascending);
            case ENABLED -> compareEnabledCursor(
                    Boolean.valueOf(condition.cursor()), condition.idAfter(), ascending);
        };
    }



    // 정렬 필드 값이 같으면 id로 한 번 더 비교 (스펙: 이전 페이지 마지막 요소 ID 기준 커서)
    private BooleanBuilder compareIndexInfoIdCursor(Long cursor, Long idAfter, boolean ascending) {
        QAutoSyncConfig autoSyncConfig = QAutoSyncConfig.autoSyncConfig;

        BooleanBuilder builder = new BooleanBuilder();
        if (ascending) {
            builder.and(
                    autoSyncConfig.indexInfo.id.gt(cursor)
                            .or(autoSyncConfig.indexInfo.id.eq(cursor).and(autoSyncConfig.id.gt(idAfter)))
            );
        } else {
            builder.and(
                    autoSyncConfig.indexInfo.id.lt(cursor)
                            .or(autoSyncConfig.indexInfo.id.eq(cursor).and(autoSyncConfig.id.lt(idAfter)))
            );
        }
        return builder;
    }

    // enabled는 true/false뿐이라 다음 값이 아예 없을 수 있음
    // 같은 값 구간에서는 id로 비교하고, 값이 바뀌는 경계에서는 방향에 맞는 값을 통째로 포함
    private BooleanBuilder compareEnabledCursor(Boolean cursor, Long idAfter, boolean ascending) {
        QAutoSyncConfig autoSyncConfig = QAutoSyncConfig.autoSyncConfig;

        BooleanBuilder sameValue = new BooleanBuilder()
                .and(autoSyncConfig.enabled.eq(cursor))
                .and(ascending ? autoSyncConfig.id.gt(idAfter) : autoSyncConfig.id.lt(idAfter));

        BooleanBuilder builder = new BooleanBuilder();
        if (ascending) {
            // false -> true 순서: cursor가 false면 true 전체 + false 중 id 큰 것
            if (Boolean.FALSE.equals(cursor)) {
                builder.and(autoSyncConfig.enabled.isTrue().or(sameValue));
            } else {
                builder.and(sameValue);
            }
        } else {
            // true -> false 순서: cursor가 true면 false 전체 + true 중 id 작은 것
            if (Boolean.TRUE.equals(cursor)) {
                builder.and(autoSyncConfig.enabled.isFalse().or(sameValue));
            } else {
                builder.and(sameValue);
            }
        }
        return builder;
    }

    private OrderSpecifier<?> sortOrder(AutoSyncConfigSearchCondition condition) {
        AutoSyncConfigSortField sortField = AutoSyncConfigSortField.from(condition.sortField());
        Order order = isAscending(condition.sortDirection()) ? Order.ASC : Order.DESC;

        return switch (sortField) {
            case INDEX_INFO_ID -> new OrderSpecifier<>(order, QAutoSyncConfig.autoSyncConfig.indexInfo.id);
            case ENABLED -> new OrderSpecifier<>(order, QAutoSyncConfig.autoSyncConfig.enabled);
        };
    }

    private OrderSpecifier<Long> idOrder(AutoSyncConfigSearchCondition condition) {
        Order order = isAscending(condition.sortDirection()) ? Order.ASC : Order.DESC;
        return new OrderSpecifier<>(order, QAutoSyncConfig.autoSyncConfig.id);
    }

    // null/blank는 "미지정"으로 보고 기본값(desc) 처리, 그 외 asc/desc가 아닌 값은 명시적으로 거부
    private boolean isAscending(String sortDirection) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return false;
        }
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return true;
        }
        if ("desc".equalsIgnoreCase(sortDirection)) {
            return false;
        }
        throw new InvalidSortDirectionException(sortDirection);
    }
}