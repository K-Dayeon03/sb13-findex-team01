package com.sb13.findex.indexdata.entity;

import com.sb13.findex.global.entity.BaseEntity;
import com.sb13.findex.indexdata.dto.command.IndexDataCreateCommand;
import com.sb13.findex.indexinfo.entity.IndexInfo;
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

public class IndexData extends BaseEntity {
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

  //openApi 관련 메서드
  public boolean isUserData() {
        return this.indexType == IndexType.USER;
  }

  public boolean isOpenApiData() {
        return this.indexType == IndexType.OPEN_API;
  }

  //사용자가 직접 등록하는 데ㅣ터는 USER enum
    //사용자 등록용 생성 메서드
  public static IndexData createUserData(
            IndexInfo indexInfo,
            IndexDataCreateCommand command
  ) {
        IndexData indexData = new IndexData();
        indexData.indexInfo = indexInfo;
        indexData.baseDate = command.baseDate();
        indexData.indexType = IndexType.USER;
        indexData.marketPrice = command.marketPrice();
        indexData.closingPrice = command.closingPrice();
        indexData.highPrice = command.highPrice();
        indexData.lowPrice = command.lowPrice();
        indexData.versus = command.versus();
        indexData.fluctuationRate = command.fluctuationRate();
        indexData.tradingQuantity = command.tradingQuantity();
        indexData.tradingPrice = command.tradingPrice();
        indexData.marketTotalAmount = command.marketTotalAmount();
        return indexData;
  }
  //openAPI로 새로 들어온 데이터는 OPEN_API로 저장
    //Open API 등록용 생성 메서드
    public static IndexData createOpenApiData(
            IndexInfo indexInfo,
            LocalDate baseDate,
            BigDecimal marketPrice,
            BigDecimal closingPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal versus,
            BigDecimal fluctuationRate,
            Long tradingQuantity,
            Long tradingPrice,
            Long marketTotalAmount
    ) {
        IndexData indexData = new IndexData();
        indexData.indexInfo = indexInfo;
        indexData.baseDate = baseDate;
        indexData.indexType = IndexType.OPEN_API;
        indexData.marketPrice = marketPrice;
        indexData.closingPrice = closingPrice;
        indexData.highPrice = highPrice;
        indexData.lowPrice = lowPrice;
        indexData.versus = versus;
        indexData.fluctuationRate = fluctuationRate;
        indexData.tradingQuantity = tradingQuantity;
        indexData.tradingPrice = tradingPrice;
        indexData.marketTotalAmount = marketTotalAmount;
        return indexData;
    }
    //사용자가 수정하는 기존이 OPEN_API였어도 USER로 변경
    //사용자 수정 메서드
    public void updateByUser(
            BigDecimal marketPrice,
            BigDecimal closingPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal versus,
            BigDecimal fluctuationRate,
            Long tradingQuantity,
            Long tradingPrice,
            Long marketTotalAmount
    ) {
        this.indexType = IndexType.USER;
        updateValues(
                marketPrice,
                closingPrice,
                highPrice,
                lowPrice,
                versus,
                fluctuationRate,
                tradingQuantity,
                tradingPrice,
                marketTotalAmount
        );
    }
    //USER면 갱신 x, OPEN_API일때만 갱신
    //OPEN_API 갱신 메서드
    public void updateByOpenApi(
            BigDecimal marketPrice,
            BigDecimal closingPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal versus,
            BigDecimal fluctuationRate,
            Long tradingQuantity,
            Long tradingPrice,
            Long marketTotalAmount
    ) {
        if (isUserData()) {
            return;
        }

        updateValues(
                marketPrice,
                closingPrice,
                highPrice,
                lowPrice,
                versus,
                fluctuationRate,
                tradingQuantity,
                tradingPrice,
                marketTotalAmount
        );
    }
    //공통 수정 로직 메서드 분리
    private void updateValues(
            BigDecimal marketPrice,
            BigDecimal closingPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal versus,
            BigDecimal fluctuationRate,
            Long tradingQuantity,
            Long tradingPrice,
            Long marketTotalAmount
    ) {
        this.marketPrice = marketPrice;
        this.closingPrice = closingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.versus = versus;
        this.fluctuationRate = fluctuationRate;
        this.tradingQuantity = tradingQuantity;
        this.tradingPrice = tradingPrice;
        this.marketTotalAmount = marketTotalAmount;
    }
}
