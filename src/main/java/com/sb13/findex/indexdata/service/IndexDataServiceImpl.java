package com.sb13.findex.indexdata.service;

import com.sb13.findex.entity.IndexInfo;
import com.sb13.findex.indexdata.dto.CursorPageResponse;
import com.sb13.findex.indexdata.dto.IndexDataCreateCommand;
import com.sb13.findex.indexdata.dto.IndexDataResponse;
import com.sb13.findex.indexdata.dto.IndexDataSearchCondition;
import com.sb13.findex.indexdata.dto.IndexDataSortField;
import com.sb13.findex.indexdata.entity.IndexData;
import com.sb13.findex.indexdata.entity.IndexType;
import com.sb13.findex.indexdata.mapper.IndexDataMapper;
import com.sb13.findex.indexdata.repository.IndexDataRepository;
import com.sb13.findex.repository.IndexInfoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IndexDataServiceImpl implements IndexDataService {

    private static final int DEFAULT_SIZE = 10;

    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;

    public IndexDataServiceImpl(IndexDataRepository indexDataRepository, IndexInfoRepository indexInfoRepository) {
        this.indexDataRepository = indexDataRepository;
        this.indexInfoRepository = indexInfoRepository;
    }

    @Override
    @Transactional
    public IndexDataResponse createIndexData(IndexDataCreateCommand command) {
        IndexInfo indexInfo = indexInfoRepository.findById(command.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지수 정보입니다. ID: " + command.indexInfoId()));

        if (indexDataRepository.existsByIndexInfoIdAndBaseDate(command.indexInfoId(), command.baseDate())) {
            throw new IllegalArgumentException("해당 날짜의 지수 데이터가 이미 존재합니다.");
        }

        IndexData indexData = IndexData.builder()
            .indexInfo(indexInfo)
            .baseDate(command.baseDate())
            .indexType(IndexType.valueOf(command.sourceType()))
            .marketPrice(command.marketPrice())
            .closingPrice(command.closingPrice())
            .highPrice(command.highPrice())
            .lowPrice(command.lowPrice())
            .versus(command.versus())
            .fluctuationRate(command.fluctuationRate())
            .tradingQuantity(command.tradingQuantity())
            .tradingPrice(command.tradingPrice())
            .marketTotalAmount(command.marketTotalAmount())
            .build();

        IndexData savedData = indexDataRepository.save(indexData);

        return new IndexDataResponse(
            savedData.getId(),
            savedData.getIndexInfo().getId(),
            savedData.getIndexInfo().getIndexClassification(),
            savedData.getIndexInfo().getIndexName(),
            savedData.getBaseDate(),
            savedData.getIndexType(),
            savedData.getMarketPrice(),
            savedData.getClosingPrice(),
            savedData.getHighPrice(),
            savedData.getLowPrice(),
            savedData.getVersus(),
            savedData.getFluctuationRate(),
            savedData.getTradingQuantity(),
            savedData.getTradingPrice(),
            savedData.getMarketTotalAmount()
        );
    }

    /*
    * Repository에서 size보다 1개 더 조회한 뒤,
    *  Service에서 실제 응답 데이터는 size만큼만 자릅니다.
    초과 데이터가 있으면 hasNext를 true로 설정
    * */
    @Override
    public CursorPageResponse<IndexDataResponse> search(IndexDataSearchCondition condition) {
        int size = getSize(condition.size());

        List<IndexData> found = indexDataRepository.search(condition);

        boolean hasNext = found.size() > size;

        List<IndexData> content = hasNext
                ? found.subList(0, size)
                : found;

        List<IndexDataResponse> responses = IndexDataMapper.toResponseList(content);

        long totalElements = indexDataRepository.count(condition);

        String nextCursor = null;
        Long nextIdAfter = null;
        //마지막 데이터를 기준으로 다음 페이지용 cursor를 만든다.
        if (hasNext && !content.isEmpty()) {
            IndexData last = content.get(content.size() - 1);
            nextCursor = getCursorValue(last, condition.sortField());
            nextIdAfter = last.getId();
        }

        return new CursorPageResponse<>(
                responses,
                nextCursor,
                nextIdAfter,
                responses.size(),
                totalElements,
                hasNext
        );
    }

    private int getSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }

        return size;
    }

    private String getCursorValue(IndexData indexData, String sortFieldValue) {
        IndexDataSortField sortField = getSortField(sortFieldValue);

        return switch (sortField) {
            case INDEX_INFO_ID -> String.valueOf(indexData.getIndexInfo().getId());
            case BASE_DATE -> indexData.getBaseDate().toString();
            case MARKET_PRICE -> indexData.getMarketPrice().toPlainString();
            case CLOSING_PRICE -> indexData.getClosingPrice().toPlainString();
            case HIGH_PRICE -> indexData.getHighPrice().toPlainString();
            case LOW_PRICE -> indexData.getLowPrice().toPlainString();
            case VERSUS -> indexData.getVersus().toPlainString();
            case FLUCTUATION_RATE -> indexData.getFluctuationRate().toPlainString();
            case TRADING_QUANTITY -> String.valueOf(indexData.getTradingQuantity());
            case TRADING_PRICE -> String.valueOf(indexData.getTradingPrice());
            case MARKET_TOTAL_AMOUNT -> String.valueOf(indexData.getMarketTotalAmount());
        };
    }

    private IndexDataSortField getSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return IndexDataSortField.BASE_DATE;
        }

        return IndexDataSortField.from(sortField);
    }
    @Override
    public byte[] exportCsv(IndexDataSearchCondition condition) {
        List<IndexData> indexDataList = indexDataRepository.searchForExport(condition);

        StringBuilder csv = new StringBuilder();

        csv.append("\uFEFF"); // Excel 한글 깨짐 방지용 UTF-8 BOM
        csv.append("ID,지수정보ID,지수분류명,지수명,기준일자,소스타입,시가,종가,고가,저가,대비,등락률,거래량,거래대금,상장시가총액\n");

        for (IndexData indexData : indexDataList) {
            csv.append(indexData.getId()).append(",");
            csv.append(indexData.getIndexInfo().getId()).append(",");
            csv.append(escapeCsv(indexData.getIndexInfo().getIndexClassification())).append(",");
            csv.append(escapeCsv(indexData.getIndexInfo().getIndexName())).append(",");
            csv.append(indexData.getBaseDate()).append(",");
            csv.append(indexData.getIndexType()).append(",");
            csv.append(indexData.getMarketPrice()).append(",");
            csv.append(indexData.getClosingPrice()).append(",");
            csv.append(indexData.getHighPrice()).append(",");
            csv.append(indexData.getLowPrice()).append(",");
            csv.append(indexData.getVersus()).append(",");
            csv.append(indexData.getFluctuationRate()).append(",");
            csv.append(indexData.getTradingQuantity()).append(",");
            csv.append(indexData.getTradingPrice()).append(",");
            csv.append(indexData.getMarketTotalAmount()).append("\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}
