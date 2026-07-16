package com.sb13.findex.indexinfo.repository;

import com.sb13.findex.global.exception.InvalidRequestException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb13.findex.indexinfo.dto.command.*;
import com.sb13.findex.indexinfo.dto.response.*;
import com.sb13.findex.indexinfo.entity.*;
import com.sb13.findex.indexinfo.utli.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class IndexInfoRepositoryImpl
        implements IndexInfoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QIndexInfo indexInfo = QIndexInfo.indexInfo;

    /*
    지수 정보 목록 조회
    검색 조건과 커서를 적용하고
    요청한 사이즈보다 1개 더 조회합니다.
     */
    @Override
    public List<IndexInfo> searchIndexInfo(
            IndexInfoSearchCondition condition
    ) {
        int size =
                IndexInfoPaginationUtils.resolveSize(condition.size());

        return queryFactory
                .selectFrom(indexInfo)
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

    // 검색 조건
    private BooleanBuilder filterCondition(
            IndexInfoSearchCondition condition
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.indexClassification() != null
                && !condition.indexClassification().isBlank()) {

            builder.and(
                    indexInfo.indexClassification.contains(
                            condition.indexClassification().trim()
                    )
            );
        }

        if (condition.indexName() != null
                && !condition.indexName().isBlank()) {

            builder.and(
                    indexInfo.indexName.contains(
                            condition.indexName().trim()
                    )
            );
        }

        if (condition.favorite() != null) {
            builder.and(
                    indexInfo.favorite.eq(condition.favorite())
            );
        }

        return builder;
    }

    /**
     * 커서 기반 페이지네이션 조건을 생성합니다.
     * cursor 또는 idAfter가 없으면 첫 페이지이므로
     * 커서 조건을 적용하지 않습니다.
     */
    private BooleanBuilder cursorCondition(
            IndexInfoSearchCondition condition
    ) {
        if (!hasCursor(condition)) {
            return null;
        }

        IndexInfoSortField sortField =
                IndexInfoSortField.from(condition.sortField());

        boolean ascending =
                isAscending(condition.sortDirection());

        return switch (sortField) {
            case INDEX_CLASSIFICATION -> compareCursor(
                    indexInfo.indexClassification,
                    condition.cursor(),
                    condition.idAfter(),
                    ascending
            );

            case INDEX_NAME -> compareCursor(
                    indexInfo.indexName,
                    condition.cursor(),
                    condition.idAfter(),
                    ascending
            );

            case EMPLOYED_ITEMS_COUNT -> compareCursor(
                    indexInfo.employedItemsCount,
                    Integer.valueOf(condition.cursor()),
                    condition.idAfter(),
                    ascending
            );
        };
    }

    /**
     * 문자열 등 Comparable 타입 필드의 커서 조건을 생성합니다.
     * ASC:
     * 정렬 필드 > cursor
     * OR
     * 정렬 필드 = cursor AND id > idAfter
     * DESC:
     * 정렬 필드 < cursor
     * OR
     * 정렬 필드 = cursor AND id < idAfter
     */
    private <T extends Comparable> BooleanBuilder compareCursor(
            ComparableExpression<T> sortPath,
            T cursor,
            Long idAfter,
            boolean ascending
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (ascending) {
            builder.and(
                    sortPath.gt(cursor)
                            .or(
                                    sortPath.eq(cursor)
                                            .and(indexInfo.id.gt(idAfter))
                            )
            );
        } else {
            builder.and(
                    sortPath.lt(cursor)
                            .or(
                                    sortPath.eq(cursor)
                                            .and(indexInfo.id.lt(idAfter))
                            )
            );
        }

        return builder;
    }

    /**
     * 숫자 타입 필드의 커서 조건을 생성합니다.
     */
    private <T extends Number & Comparable<?>> BooleanBuilder compareCursor(
            NumberExpression<T> sortPath,
            T cursor,
            Long idAfter,
            boolean ascending
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (ascending) {
            builder.and(
                    sortPath.gt(cursor)
                            .or(
                                    sortPath.eq(cursor)
                                            .and(indexInfo.id.gt(idAfter))
                            )
            );
        } else {
            builder.and(
                    sortPath.lt(cursor)
                            .or(
                                    sortPath.eq(cursor)
                                            .and(indexInfo.id.lt(idAfter))
                            )
            );
        }

        return builder;
    }

    /**
     * 요청된 정렬 필드와 정렬 방향에 맞는
     * OrderSpecifier를 반환합니다.
     */
    private OrderSpecifier<?> sortOrder(
            IndexInfoSearchCondition condition
    ) {
        IndexInfoSortField sortField =
                IndexInfoSortField.from(condition.sortField());

        Order order = isAscending(condition.sortDirection())
                ? Order.ASC
                : Order.DESC;

        return switch (sortField) {
            case INDEX_CLASSIFICATION ->
                    new OrderSpecifier<>(
                            order,
                            indexInfo.indexClassification
                    );

            case INDEX_NAME ->
                    new OrderSpecifier<>(
                            order,
                            indexInfo.indexName
                    );

            case EMPLOYED_ITEMS_COUNT ->
                    new OrderSpecifier<>(
                            order,
                            indexInfo.employedItemsCount
                    );
        };
    }

    /**
     * 정렬 필드의 값이 같은 데이터를 안정적으로 정렬하기 위해
     * id를 보조 정렬 기준으로 사용합니다.
     */
    private OrderSpecifier<Long> idOrder(
            IndexInfoSearchCondition condition
    ) {
        Order order = isAscending(condition.sortDirection())
                ? Order.ASC
                : Order.DESC;

        return new OrderSpecifier<>(
                order,
                indexInfo.id
        );
    }

    /**
     * sortDirection이 없으면 ASC를 기본값으로 사용합니다.
     * asc와 desc 이외의 값이 들어오면 예외를 발생시킵니다.
     */
    private boolean isAscending(
            String sortDirection
    ) {
        if (sortDirection == null || sortDirection.isBlank()) {
            return true;
        }

        String direction = sortDirection.strip();

        if ("asc".equalsIgnoreCase(direction)) {
            return true;
        }

        if ("desc".equalsIgnoreCase(direction)) {
            return false;
        }

        throw new InvalidRequestException(
                "지원하지 않는 정렬 방향입니다: " + sortDirection
        );
    }

    /**
     * cursor와 idAfter가 모두 있어야
     * 다음 페이지 조회로 판단합니다.
     */
    private boolean hasCursor(
            IndexInfoSearchCondition condition
    ) {
        return condition.cursor() != null
                && !condition.cursor().isBlank()
                && condition.idAfter() != null;
    }

    @Override
    public long countIndexInfo(IndexInfoSearchCondition condition) {
        Long count = queryFactory
                .select(indexInfo.count())
                .from(indexInfo)
                .where(filterCondition(condition))
                .fetchOne();

        return count == null ? 0L : count;
    }

    @Override
    public List<IndexInfoSummaryResponse> findAllSummaries() {
        return queryFactory
                .select(Projections.constructor(
                        IndexInfoSummaryResponse.class,
                        indexInfo.id,
                        indexInfo.indexClassification,
                        indexInfo.indexName
                        ))
                .from(indexInfo)
                .orderBy(indexInfo.indexName.asc())
                .fetch();
    }
}
