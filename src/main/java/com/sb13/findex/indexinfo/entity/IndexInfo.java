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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_classification", length = 30, nullable = false)
    private String indexClassification;

    @Column(name = "index_name", length = 100, nullable = false)
    private String indexName;

    @Column(name = "employed_items_count")
    private int employedItemsCount;

    @Column(name = "base_point_in_time", nullable = false)
    private LocalDate basePointInTime;

    @Column(name = "base_index", nullable = false)
    private BigDecimal baseIndex;

    @Column(name = "source_type", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "favorite")
    private boolean favorite;


    public boolean isUserSource() {
        return this.sourceType == SourceType.USER;
    }

    public boolean isOpenApiSource() {
        return this.sourceType == SourceType.OPEN_API;
    }

    public void updateByUser(
            int employedItemsCount,
            LocalDate basePointInTime,
            BigDecimal baseIndex,
            boolean favorite
    ) {
        this.employedItemsCount = employedItemsCount;
        this.basePointInTime = basePointInTime;
        this.baseIndex = baseIndex;
        this.favorite = favorite;
        this.sourceType = SourceType.USER;
    }

    public void updateByOpenApi(
            int employedItemsCount,
            LocalDate basePointInTime,
            BigDecimal baseIndex
    ) {
        if (!isOpenApiSource()) {
            return;
        }

        this.employedItemsCount = employedItemsCount;
        this.basePointInTime = basePointInTime;
        this.baseIndex = baseIndex;
    }

}
