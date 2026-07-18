package com.sb13.findex.indexdata.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sb13.findex.indexdata.repository.IndexDataRepositoryCustom;
import com.sb13.findex.indexinfo.entity.QIndexInfo;
import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;
import com.sb13.findex.indexdata.dto.condition.IndexDataSortField;
import com.sb13.findex.indexdata.dto.response.IndexDataCsvRow;
import com.sb13.findex.indexdata.entity.IndexData;
import com.sb13.findex.indexdata.entity.IndexType;
import com.sb13.findex.indexdata.entity.QIndexData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

@Repository
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

  private static final int DEFAULT_SIZE = 10;

  private final JPAQueryFactory queryFactory;

  public IndexDataRepositoryImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public List<IndexData> search(IndexDataSearchCondition condition) {
    QIndexData indexData = QIndexData.indexData;
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    int size = condition.size() == null || condition.size() <= 0
        ? DEFAULT_SIZE
        : condition.size();

    return queryFactory
        .selectFrom(indexData)
        .join(indexData.indexInfo, indexInfo).fetchJoin()
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
  public Stream<IndexDataCsvRow> streamForExport(IndexDataSearchCondition condition) {
    QIndexData indexData = QIndexData.indexData;
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    return queryFactory
        .select(Projections.constructor(
            IndexDataCsvRow.class,
            indexData.id,
            indexInfo.id,
            indexInfo.indexClassification,
            indexInfo.indexName,
            indexData.baseDate,
            indexData.indexType,
            indexData.marketPrice,
            indexData.closingPrice,
            indexData.highPrice,
            indexData.lowPrice,
            indexData.versus,
            indexData.fluctuationRate,
            indexData.tradingQuantity,
            indexData.tradingPrice,
            indexData.marketTotalAmount
        ))
        .from(indexData)
        .join(indexData.indexInfo, indexInfo)
        .where(filterCondition(condition))
        .orderBy(
            sortOrder(condition),
            idOrder(condition)
        )
        .stream();
  }
  /*
  * 즐겨찾기된 지수들 중에서
  * 각 지수별 가장 최신 baseDate의 IndexData만 가져온다.
  * */
  @Override
  public List<IndexData> findLatestDataForFavoriteIndexes() {
    QIndexData indexData = QIndexData.indexData;
    QIndexData subIndexData = new QIndexData("subIndexData");
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    return queryFactory
            .selectFrom(indexData)
            .join(indexData.indexInfo, indexInfo).fetchJoin()
            .where(
                    indexInfo.favorite.isTrue(),
                    latestBaseDateCondition(indexData, subIndexData)
            )
            .orderBy(indexInfo.indexName.asc())
            .fetch();
  }

  @Override
  public long count(IndexDataSearchCondition condition) {
    QIndexData indexData = QIndexData.indexData;

    Long count = queryFactory
        .select(indexData.count())
        .from(indexData)
        .where(filterCondition(condition))
        .fetchOne();

    return count == null ? 0L : count;
  }
  private BooleanExpression latestBaseDateCondition(
          QIndexData indexData,
          QIndexData subIndexData
  ) {
    return indexData.baseDate.eq(
            JPAExpressions
                    .select(subIndexData.baseDate.max())
                    .from(subIndexData)
                    .where(subIndexData.indexInfo.id.eq(indexData.indexInfo.id))
    );
  }

  private BooleanBuilder filterCondition(IndexDataSearchCondition condition) {
    QIndexData indexData = QIndexData.indexData;

    BooleanBuilder builder = new BooleanBuilder();

    if (condition.indexInfoId() != null) {
      builder.and(indexData.indexInfo.id.eq(condition.indexInfoId()));
    }

    if (condition.startDate() != null) {
      builder.and(indexData.baseDate.goe(condition.startDate()));
    }

    if (condition.endDate() != null) {
      builder.and(indexData.baseDate.loe(condition.endDate()));
    }

    return builder;
  }

  private BooleanBuilder cursorCondition(IndexDataSearchCondition condition) {
    if (condition.cursor() == null || condition.cursor().isBlank() || condition.idAfter() == null) {
      return null;
    }

    IndexDataSortField sortField = getSortField(condition.sortField());
    boolean ascending = isAscending(condition.sortDirection());

    return switch (sortField) {
      case ID -> compareCursor(
          QIndexData.indexData.id,
          Long.valueOf(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case INDEX_INFO_ID -> compareCursor(
          QIndexData.indexData.indexInfo.id,
          Long.valueOf(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case INDEX_CLASSIFICATION -> compareCursor(
          QIndexData.indexData.indexInfo.indexClassification,
          condition.cursor(),
          condition.idAfter(),
          ascending
      );
      case INDEX_NAME -> compareCursor(
          QIndexData.indexData.indexInfo.indexName,
          condition.cursor(),
          condition.idAfter(),
          ascending
      );
      case BASE_DATE -> compareCursor(
          QIndexData.indexData.baseDate,
          LocalDate.parse(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case SOURCE_TYPE -> compareCursor(
          QIndexData.indexData.indexType,
          IndexType.valueOf(condition.cursor().trim().toUpperCase()),
          condition.idAfter(),
          ascending
      );
      case MARKET_PRICE -> compareCursor(
          QIndexData.indexData.marketPrice,
          new BigDecimal(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case CLOSING_PRICE -> compareCursor(
          QIndexData.indexData.closingPrice,
          new BigDecimal(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case HIGH_PRICE -> compareCursor(
          QIndexData.indexData.highPrice,
          new BigDecimal(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case LOW_PRICE -> compareCursor(
          QIndexData.indexData.lowPrice,
          new BigDecimal(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case VERSUS -> compareCursor(
          QIndexData.indexData.versus,
          new BigDecimal(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case FLUCTUATION_RATE -> compareCursor(
          QIndexData.indexData.fluctuationRate,
          new BigDecimal(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case TRADING_QUANTITY -> compareCursor(
          QIndexData.indexData.tradingQuantity,
          Long.valueOf(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case TRADING_PRICE -> compareCursor(
          QIndexData.indexData.tradingPrice,
          Long.valueOf(condition.cursor()),
          condition.idAfter(),
          ascending
      );
      case MARKET_TOTAL_AMOUNT -> compareCursor(
          QIndexData.indexData.marketTotalAmount,
          Long.valueOf(condition.cursor()),
          condition.idAfter(),
          ascending
      );
    };
  }

  /*커서 페이지네이션은 이전 페이지의 마지막 데이터를 기준으로 그 다음 데이터를 조회합니다.
  * 단순히 정렬 필드만 비교하면 같은 정렬 값을 가진 데이터가 누락될 수 있어서,
  * 정렬 필드가 같은 경우에는 idAfter로 한 번 더 비교합니다.
  * */
  private <T extends Comparable> BooleanBuilder compareCursor(
      ComparableExpression<T> sortPath,
      T cursor,
      Long idAfter,
      boolean ascending
  ) {
    QIndexData indexData = QIndexData.indexData;

    BooleanBuilder builder = new BooleanBuilder();

    if (ascending) {
      builder.and(
          sortPath.gt(cursor)
              .or(sortPath.eq(cursor).and(indexData.id.gt(idAfter)))
      );
    } else {
      builder.and(
          sortPath.lt(cursor)
              .or(sortPath.eq(cursor).and(indexData.id.lt(idAfter)))
      );
    }

    return builder;
  }

  private <T extends Number & Comparable<?>> BooleanBuilder compareCursor(
      NumberExpression<T> sortPath,
      T cursor,
      Long idAfter,
      boolean ascending
  ) {
    QIndexData indexData = QIndexData.indexData;

    BooleanBuilder builder = new BooleanBuilder();

    if (ascending) {
      builder.and(
          sortPath.gt(cursor)
              .or(sortPath.eq(cursor).and(indexData.id.gt(idAfter)))
      );
    } else {
      builder.and(
          sortPath.lt(cursor)
              .or(sortPath.eq(cursor).and(indexData.id.lt(idAfter)))
      );
    }

    return builder;
  }

  private OrderSpecifier<?> sortOrder(IndexDataSearchCondition condition) {
    IndexDataSortField sortField = getSortField(condition.sortField());
    Order order = isAscending(condition.sortDirection()) ? Order.ASC : Order.DESC;

    return switch (sortField) {
      case ID -> new OrderSpecifier<>(order, QIndexData.indexData.id);
      case INDEX_INFO_ID -> new OrderSpecifier<>(order, QIndexData.indexData.indexInfo.id);
      case INDEX_CLASSIFICATION -> new OrderSpecifier<>(order, QIndexData.indexData.indexInfo.indexClassification);
      case INDEX_NAME -> new OrderSpecifier<>(order, QIndexData.indexData.indexInfo.indexName);
      case BASE_DATE -> new OrderSpecifier<>(order, QIndexData.indexData.baseDate);
      case SOURCE_TYPE -> new OrderSpecifier<>(order, QIndexData.indexData.indexType);
      case MARKET_PRICE -> new OrderSpecifier<>(order, QIndexData.indexData.marketPrice);
      case CLOSING_PRICE -> new OrderSpecifier<>(order, QIndexData.indexData.closingPrice);
      case HIGH_PRICE -> new OrderSpecifier<>(order, QIndexData.indexData.highPrice);
      case LOW_PRICE -> new OrderSpecifier<>(order, QIndexData.indexData.lowPrice);
      case VERSUS -> new OrderSpecifier<>(order, QIndexData.indexData.versus);
      case FLUCTUATION_RATE -> new OrderSpecifier<>(order, QIndexData.indexData.fluctuationRate);
      case TRADING_QUANTITY -> new OrderSpecifier<>(order, QIndexData.indexData.tradingQuantity);
      case TRADING_PRICE -> new OrderSpecifier<>(order, QIndexData.indexData.tradingPrice);
      case MARKET_TOTAL_AMOUNT -> new OrderSpecifier<>(order, QIndexData.indexData.marketTotalAmount);
    };
  }

  private OrderSpecifier<Long> idOrder(IndexDataSearchCondition condition) {
    Order order = isAscending(condition.sortDirection()) ? Order.ASC : Order.DESC;
    return new OrderSpecifier<>(order, QIndexData.indexData.id);
  }

  private IndexDataSortField getSortField(String sortField) {
    if (sortField == null || sortField.isBlank()) {
      return IndexDataSortField.BASE_DATE;
    }

    return IndexDataSortField.from(sortField);
  }

  private boolean isAscending(String sortDirection) {
    return "asc".equalsIgnoreCase(sortDirection);
  }

  @Override
  public Optional<IndexData> findNearestDataOnOrBefore(
          Long indexInfoId,
          LocalDate targetDate
  ) {
    QIndexData indexData = QIndexData.indexData;
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    IndexData result = queryFactory
            .selectFrom(indexData)
            .join(indexData.indexInfo, indexInfo).fetchJoin()
            .where(
                    indexData.indexInfo.id.eq(indexInfoId),
                    indexData.baseDate.loe(targetDate)
            )
            .orderBy(
                    indexData.baseDate.desc(),
                    indexData.id.desc()
            )
            .limit(1)
            .fetchOne();

    return Optional.ofNullable(result);
  }

  @Override
  public List<IndexData> findNearestDataOnOrBeforeByIndexInfoIds(
          Map<Long, LocalDate> targetDatesByIndexInfoId
  ) {
    if (targetDatesByIndexInfoId.isEmpty()) {
      return List.of();
    }

    QIndexData indexData = QIndexData.indexData;
    QIndexData subIndexData = new QIndexData("subIndexData");
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    BooleanBuilder builder = new BooleanBuilder();

    targetDatesByIndexInfoId.forEach((indexInfoId, targetDate) -> {
      builder.or(
              indexData.indexInfo.id.eq(indexInfoId)
                      .and(indexData.baseDate.eq(
                              JPAExpressions
                                      .select(subIndexData.baseDate.max())
                                      .from(subIndexData)
                                      .where(
                                              subIndexData.indexInfo.id.eq(indexInfoId),
                                              subIndexData.baseDate.loe(targetDate)
                                      )
                      ))
      );
    });

    return queryFactory
            .selectFrom(indexData)
            .join(indexData.indexInfo, indexInfo).fetchJoin()
            .where(builder)
            .fetch();
  }
  @Override
  public List<IndexData> findDataByIndexInfoIdAndBaseDateBetween(
          Long indexInfoId,
          LocalDate startDate,
          LocalDate endDate
  ) {
    QIndexData indexData = QIndexData.indexData;
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    return queryFactory
            .selectFrom(indexData)
            .join(indexData.indexInfo, indexInfo).fetchJoin()
            .where(
                    indexData.indexInfo.id.eq(indexInfoId),
                    indexData.baseDate.goe(startDate),
                    indexData.baseDate.loe(endDate)
            )
            .orderBy(
                    indexData.baseDate.asc(),
                    indexData.id.asc()
            )
            .fetch();
  }
  //indexinfoid가 있으면 그 지수만,
  //없으면 전체 지수 기준으로 각 지수별 최신 데이터를 가져옴
  @Override
  public List<IndexData> findLatestDataForRanking(Long indexInfoId) {
    QIndexData indexData = QIndexData.indexData;
    QIndexData subIndexData = new QIndexData("subIndexData");
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    BooleanBuilder builder = new BooleanBuilder();

    if (indexInfoId != null) {
      builder.and(indexData.indexInfo.id.eq(indexInfoId));
    }

    builder.and(latestBaseDateCondition(indexData, subIndexData));

    return queryFactory
            .selectFrom(indexData)
            .join(indexData.indexInfo, indexInfo).fetchJoin()
            .where(builder)
            .orderBy(indexInfo.indexName.asc())
            .fetch();
  }
}
