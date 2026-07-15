package com.sb13.findex.indexinfo.entity;

import com.sb13.findex.global.entity.BaseEntity;
import com.sb13.findex.sync.entity.SourceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.*;
import java.time.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "index_info",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_index_info_classification_name",
                        columnNames = {"index_classification", "index_name"}
                )
        }
)
public class IndexInfo extends BaseEntity {

    @Builder(access = AccessLevel.PRIVATE)
    private IndexInfo(
            String indexClassification,
            String indexName,
            int employedItemsCount,
            LocalDate basePointInTime,
            BigDecimal baseIndex,
            SourceType sourceType,
            boolean favorite
    ) {
        this.indexClassification = indexClassification;
        this.indexName = indexName;
        this.employedItemsCount = employedItemsCount;
        this.basePointInTime = basePointInTime;
        this.baseIndex = baseIndex;
        this.sourceType = sourceType;
        this.favorite = favorite;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_classification", length = 30, nullable = false)
    private String indexClassification;

    @Column(name = "index_name", length = 100, nullable = false)
    private String indexName;

    @Column(name = "employed_items_count", nullable = false)
    private int employedItemsCount;

    @Column(name = "base_point_in_time", nullable = false)
    private LocalDate basePointInTime;

    @Column(name = "base_index", nullable = false)
    private BigDecimal baseIndex;

    @Column(name = "source_type", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "favorite", nullable = false)
    private boolean favorite;

    public void update(
            Integer employedItemsCount,
            LocalDate basePointInTime,
            BigDecimal baseIndex,
            Boolean favorite
    ) {
        if(employedItemsCount != null) this.employedItemsCount = employedItemsCount;

        if (basePointInTime != null) this.basePointInTime = basePointInTime;

        if (baseIndex != null) this.baseIndex = baseIndex;

        if (favorite != null) this.favorite = favorite;
    }

    public static IndexInfo create(
            String indexClassification,
            String indexName,
            int employedItemsCount,
            LocalDate basePointInTime,
            BigDecimal baseIndex,
            SourceType sourceType,
            boolean favorite
    ) {
        return IndexInfo.builder()
                .indexClassification(indexClassification)
                .indexName(indexName)
                .employedItemsCount(employedItemsCount)
                .basePointInTime(basePointInTime)
                .baseIndex(baseIndex)
                .sourceType(sourceType)
                .favorite(favorite)
                .build();
    }

    // Open API 연동으로 갱신 가능한 지수정보만 수정
    public void updateByOpenApi(
            int employedItemsCount,
            LocalDate basePointInTime,
            BigDecimal baseIndex
    ) {

        if (this.sourceType != SourceType.OPEN_API) {
            return;
        }

        this.employedItemsCount = employedItemsCount;
        this.basePointInTime = basePointInTime;
        this.baseIndex = baseIndex;
    }


}
