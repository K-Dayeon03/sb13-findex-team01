package com.sb13.findex.indexdata.service;

import com.sb13.findex.global.exception.DuplicateIndexDataException;
import com.sb13.findex.global.exception.IndexDataNotFoundException;
import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexdata.dto.command.IndexDataUpdateCommand;
import com.sb13.findex.indexdata.dto.response.CursorPageResponse;
import com.sb13.findex.indexdata.dto.command.IndexDataCreateCommand;
import com.sb13.findex.indexdata.dto.response.IndexDataResponse;
import com.sb13.findex.indexdata.dto.condition.IndexDataSearchCondition;
import com.sb13.findex.indexdata.dto.condition.IndexDataSortField;
import com.sb13.findex.indexdata.entity.IndexData;
import com.sb13.findex.indexdata.mapper.IndexDataMapper;
import com.sb13.findex.indexdata.repository.IndexDataRepository;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.exception.IndexInfoNotFoundException;
import com.sb13.findex.indexinfo.repository.IndexInfoRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
            .orElseThrow(() -> new IndexInfoNotFoundException(command.indexInfoId()));

        // 기존 데이터가 있으면 생성 요청은 실패 처리

        if (indexDataRepository.existsByIndexInfo_IdAndBaseDate(
                command.indexInfoId(),
                command.baseDate()
        )) {
            throw new DuplicateIndexDataException();
        }

        IndexData indexData = IndexData.createUserData(indexInfo, command);
        IndexData savedData = indexDataRepository.save(indexData);

        return IndexDataMapper.toResponse(savedData);
    }

    @Override
    @Transactional
    public IndexDataResponse updateIndexData(Long id, IndexDataUpdateCommand command) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IndexDataNotFoundException(id));

        indexData.updateByUser(
            command.marketPrice(),
            command.closingPrice(),
            command.highPrice(),
            command.lowPrice(),
            command.versus(),
            command.fluctuationRate(),
            command.tradingQuantity(),
            command.tradingPrice(),
            command.marketTotalAmount()
        );

        return IndexDataMapper.toResponse(indexData);
    }

    @Override
    @Transactional
    public void deleteIndexData(Long id) {
        IndexData indexData = indexDataRepository.findById(id)
            .orElseThrow(() -> new IndexDataNotFoundException(id));

        indexDataRepository.delete(indexData);
    }

    @Override
    @Transactional
    public void deleteByIndexInfoId(Long indexInfoId) {
        indexDataRepository.deleteAllByIndexInfo_Id(indexInfoId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOrUpdateOpenApiData(IndexDataOpenApiCommand command) {
        Optional<IndexData> existingData =
                indexDataRepository.findByIndexInfo_IdAndBaseDate(
                        command.indexInfo().getId(), command.baseDate());

        if (existingData.isPresent()) {
            IndexData indexData = existingData.get();

            indexData.updateByOpenApi(
                    command.marketPrice(),
                    command.closingPrice(),
                    command.highPrice(),
                    command.lowPrice(),
                    command.versus(),
                    command.fluctuationRate(),
                    command.tradingQuantity(),
                    command.tradingPrice(),
                    command.marketTotalAmount()
            );

            return;
        }

        IndexData indexData = IndexData.createOpenApiData(
                command.indexInfo(),
                command.baseDate(),
                command.marketPrice(),
                command.closingPrice(),
                command.highPrice(),
                command.lowPrice(),
                command.versus(),
                command.fluctuationRate(),
                command.tradingQuantity(),
                command.tradingPrice(),
                command.marketTotalAmount()
        );

        indexDataRepository.save(indexData);
    }

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
