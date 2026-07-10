package com.sb13.findex.indexinfo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb13.findex.indexinfo.dto.*;
import com.sb13.findex.indexinfo.entity.*;
import com.sb13.findex.indexinfo.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class IndexInfoRepositoryImpl
        implements IndexInfoRepositoryCustom {

    private static final int DEFAULT_SIZE = 10;

    private final JPAQueryFactory queryFactory;

    private static final QIndexInfo indexInfo = QIndexInfo.indexInfo;

    /*
    지수 정보 목록 조회
    검색 조건과 커서를 적용하고
    요청한 사이즈보다 1개 더 조회합니다.
     */
    @Override
    public List<IndexInfo> searchIndexInfo(
            IndexInfoSearchRequest request
    ) {
        int size = resolveSize(request.size());

        return queryFactory
                .selectFrom(indexInfo)
                .where(
                        filterCondition(request),
                        cursorCondition(request)
                )
                .orderBy(
                        sortOrder(request),
                        idOrder(request)
                )
                .limit(size + 1)
                .fetch();
    }

    // 검색 조건
    private BooleanBuilder filterCondition(
            IndexInfoSearchRequest request
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.indexClassification() != null
                && !request.indexClassification().isBlank()) {

            builder.and(
                    indexInfo.indexClassification.contains(
                            request.indexClassification().trim()
                    )
            );
        }

        if (request.indexName() != null
                && !request.indexName().isBlank()) {

            builder.and(
                    indexInfo.indexName.contains(
                            request.indexName().trim()
                    )
            );
        }

        if (request.favorite() != null) {
            builder.and(
                    indexInfo.favorite.eq(request.favorite())
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
            IndexInfoSearchRequest request
    ) {
        if (!hasCursor(request)) {
            return null;
        }

        IndexInfoSortField sortField =
                getSortField(request.sortField());

        boolean ascending =
                isAscending(request.sortDirection());

        return switch (sortField) {
            case INDEX_CLASSIFICATION -> compareCursor(
                    indexInfo.indexClassification,
                    request.cursor(),
                    request.idAfter(),
                    ascending
            );

            case INDEX_NAME -> compareCursor(
                    indexInfo.indexName,
                    request.cursor(),
                    request.idAfter(),
                    ascending
            );

            case EMPLOYED_ITEMS_COUNT -> compareCursor(
                    indexInfo.employedItemsCount,
                    Integer.valueOf(request.cursor()),
                    request.idAfter(),
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
            IndexInfoSearchRequest request
    ) {
        IndexInfoSortField sortField =
                getSortField(request.sortField());

        Order order = isAscending(request.sortDirection())
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
            IndexInfoSearchRequest request
    ) {
        Order order = isAscending(request.sortDirection())
                ? Order.ASC
                : Order.DESC;

        return new OrderSpecifier<>(
                order,
                indexInfo.id
        );
    }

    /**
     * sortField가 없으면 indexName을 기본 정렬 필드로 사용합니다.
     */
    private IndexInfoSortField getSortField(
            String sortField
    ) {
        if (sortField == null || sortField.isBlank()) {
            return IndexInfoSortField.INDEX_NAME;
        }

        return IndexInfoSortField.from(sortField);
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

        if ("asc".equalsIgnoreCase(sortDirection)) {
            return true;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            return false;
        }

        throw new IllegalArgumentException(
                "지원하지 않는 정렬 방향입니다: " + sortDirection
        );
    }

    /**
     * cursor와 idAfter가 모두 있어야
     * 다음 페이지 조회로 판단합니다.
     */
    private boolean hasCursor(
            IndexInfoSearchRequest request
    ) {
        return request.cursor() != null
                && !request.cursor().isBlank()
                && request.idAfter() != null;
    }

    /**
     * size가 없거나 0 이하이면 기본 크기 10을 사용합니다.
     */
    private int resolveSize(
            Integer size
    ) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }

        return size;
    }
}

