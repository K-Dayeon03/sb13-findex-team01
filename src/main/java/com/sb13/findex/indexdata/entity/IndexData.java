package com.sb13.findex.indexdata.entity;

import com.sb13.findex.entity.IndexInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "index_data",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_index_data_info_base_date",
            columnNames = {"index_info_id", "base_date"}
        )
    }
)

public class IndexData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "index_info_id", nullable = false)
  private IndexInfo indexInfo;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "index_type", length = 30, nullable = false)
  private IndexType indexType;

  // 부동소수점 오차 방지 BigDecimal 사용
  @Column(name = "market_price", nullable = false)
  private BigDecimal marketPrice;

  @Column(name = "closing_price", nullable = false)
  private BigDecimal closingPrice;

  @Column(name = "high_price", nullable = false)
  private BigDecimal highPrice;

  @Column(name = "low_price", nullable = false)
  private BigDecimal lowPrice;

  @Column(name = "versus", nullable = false)
  private BigDecimal versus;

  @Column(name = "fluctuation_rate", nullable = false)
  private BigDecimal fluctuationRate;

 // SQL의 BIGINT 타입을 JAVA의 Long 타입과 매핑
  @Column(name = "trading_quantity", nullable = false)
  private Long tradingQuantity;

  @Column(name = "trading_price", nullable = false)
  private Long tradingPrice;

  @Column(name = "market_total_amount", nullable = false)
  private Long marketTotalAmount;
}
