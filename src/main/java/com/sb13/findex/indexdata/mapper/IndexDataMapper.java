package com.sb13.findex.indexdata.mapper;

import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexdata.dto.response.IndexDataResponse;
import com.sb13.findex.indexdata.entity.IndexData;

import java.util.List;
//IndexData Entity를 IndexDataResponse DTO로 변환
public class IndexDataMapper {

    public static IndexDataResponse toResponse(IndexData indexData) {
        IndexInfo indexInfo = indexData.getIndexInfo();
//IndexData의 값과 연관된 IndexInfo의 지수 분류명, 지수명을 함께 매핑
        return new IndexDataResponse(
                indexData.getId(),
                indexInfo.getId(),
                indexInfo.getIndexClassification(),
                indexInfo.getIndexName(),
                indexData.getBaseDate(),
                indexData.getIndexType(),
                indexData.getMarketPrice(),
                indexData.getClosingPrice(),
                indexData.getHighPrice(),
                indexData.getLowPrice(),
                indexData.getVersus(),
                indexData.getFluctuationRate(),
                indexData.getTradingQuantity(),
                indexData.getTradingPrice(),
                indexData.getMarketTotalAmount()
        );

    }

    public static List<IndexDataResponse> toResponseList(List<IndexData> indexDataList) {
        return indexDataList.stream()
                .map(IndexDataMapper::toResponse)
                .toList();
    }

}
