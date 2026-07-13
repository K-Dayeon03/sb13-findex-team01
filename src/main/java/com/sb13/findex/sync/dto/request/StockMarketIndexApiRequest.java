package com.sb13.findex.sync.dto.request;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 주가지수시세 요청 파라미터입니다.
 *
 * @param numOfRows           한 페이지 결과 수
 *                            <p>항목명: numOfRows</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>샘플데이터: 1</p>
 *                            <p>항목설명: 한 페이지 결과 수</p>
 * @param pageNo              페이지 번호
 *                            <p>항목명: pageNo</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>샘플데이터: 1</p>
 *                            <p>항목설명: 페이지 번호</p>
 * @param resultType          결과형식
 *                            <p>항목명: resultType</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>샘플데이터: xml</p>
 *                            <p>항목설명: 구분(xml, json), 기본값은 xml</p>
 * @param serviceKey          서비스키
 *                            <p>항목명: serviceKey</p>
 *                            <p>항목구분: 필수(1)</p>
 *                            <p>샘플데이터: 공공데이터포털에서 받은 인증키</p>
 *                            <p>항목설명: 공공데이터포털에서 받은 인증키</p>
 * @param basDt               기준일자
 *                            <p>항목명: basDt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>샘플데이터: 20240731</p>
 *                            <p>항목설명: 검색값과 기준일자가 일치하는 데이터를 검색</p>
 * @param beginBasDt          기준일자 시작값
 *                            <p>항목명: beginBasDt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 기준일자가 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endBasDt            기준일자 종료값
 *                            <p>항목명: endBasDt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 기준일자가 검색값보다 작은 데이터를 검색</p>
 * @param likeBasDt           기준일자 포함 검색값
 *                            <p>항목명: likeBasDt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 기준일자값이 검색값을 포함하는 데이터를 검색</p>
 * @param idxNm               지수명
 *                            <p>항목명: idxNm</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>샘플데이터: 코스피</p>
 *                            <p>항목설명: 검색값과 지수명이 일치하는 데이터를 검색</p>
 * @param likeIdxNm           지수명 포함 검색값
 *                            <p>항목명: likeIdxNm</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 지수명이 검색값을 포함하는 데이터를 검색</p>
 * @param beginEpyItmsCnt     채용종목 수 시작값
 *                            <p>항목명: beginEpyItmsCnt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 채용종목 수가 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endEpyItmsCnt       채용종목 수 종료값
 *                            <p>항목명: endEpyItmsCnt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 채용종목 수가 검색값보다 작은 데이터를 검색</p>
 * @param beginFltRt          등락률 시작값
 *                            <p>항목명: beginFltRt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 등락률이 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endFltRt            등락률 종료값
 *                            <p>항목명: endFltRt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 등락률이 검색값보다 작은 데이터를 검색</p>
 * @param beginTrqu           거래량 시작값
 *                            <p>항목명: beginTrqu</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 거래량이 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endTrqu             거래량 종료값
 *                            <p>항목명: endTrqu</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 거래량이 검색값보다 작은 데이터를 검색</p>
 * @param beginTrPrc          거래대금 시작값
 *                            <p>항목명: beginTrPrc</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 거래대금이 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endTrPrc            거래대금 종료값
 *                            <p>항목명: endTrPrc</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 거래대금이 검색값보다 작은 데이터를 검색</p>
 * @param beginLstgMrktTotAmt 상장시가총액 시작값
 *                            <p>항목명: beginLstgMrktTotAmt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 상장시가총액이 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endLstgMrktTotAmt   상장시가총액 종료값
 *                            <p>항목명: endLstgMrktTotAmt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 상장시가총액이 검색값보다 작은 데이터를 검색</p>
 * @param beginLsYrEdVsFltRg  전년말대비 등락폭 시작값
 *                            <p>항목명: beginLsYrEdVsFltRg</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 전년말대비 등락폭이 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endLsYrEdVsFltRg    전년말대비 등락폭 종료값
 *                            <p>항목명: endLsYrEdVsFltRg</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 전년말대비 등락폭이 검색값보다 작은 데이터를 검색</p>
 * @param beginLsYrEdVsFltRt  전년말대비 등락률 시작값
 *                            <p>항목명: beginLsYrEdVsFltRt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 전년말대비 등락률이 검색값보다 크거나 같은 데이터를 검색</p>
 * @param endLsYrEdVsFltRt    전년말대비 등락률 종료값
 *                            <p>항목명: endLsYrEdVsFltRt</p>
 *                            <p>항목구분: 옵션(0)</p>
 *                            <p>항목설명: 전년말대비 등락률이 검색값보다 작은 데이터를 검색</p>
 */
public record StockMarketIndexApiRequest(
        Integer numOfRows,
        Integer pageNo,
        String resultType,
        String serviceKey,
        String basDt,
        String beginBasDt,
        String endBasDt,
        String likeBasDt,
        String idxNm,
        String likeIdxNm,
        String beginEpyItmsCnt,
        String endEpyItmsCnt,
        String beginFltRt,
        String endFltRt,
        String beginTrqu,
        String endTrqu,
        String beginTrPrc,
        String endTrPrc,
        String beginLstgMrktTotAmt,
        String endLstgMrktTotAmt,
        String beginLsYrEdVsFltRg,
        String endLsYrEdVsFltRg,
        String beginLsYrEdVsFltRt,
        String endLsYrEdVsFltRt
) {

    public static StockMarketIndexApiRequest ofPage(Integer numOfRows, Integer pageNo) {
        return new StockMarketIndexApiRequest(
                numOfRows,
                pageNo,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }


    public MultiValueMap<String, String> toQueryParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        add(params, "numOfRows", numOfRows);
        add(params, "pageNo", pageNo);

        // serviceKey, resultType은 ApiService에서 공통으로 넣는 것을 추천
        // add(params, "resultType", resultType);
        // add(params, "serviceKey", serviceKey);

        add(params, "basDt", basDt);
        add(params, "beginBasDt", beginBasDt);
        add(params, "endBasDt", endBasDt);
        add(params, "likeBasDt", likeBasDt);
        add(params, "idxNm", idxNm);
        add(params, "likeIdxNm", likeIdxNm);
        add(params, "beginEpyItmsCnt", beginEpyItmsCnt);
        add(params, "endEpyItmsCnt", endEpyItmsCnt);
        add(params, "beginFltRt", beginFltRt);
        add(params, "endFltRt", endFltRt);
        add(params, "beginTrqu", beginTrqu);
        add(params, "endTrqu", endTrqu);
        add(params, "beginTrPrc", beginTrPrc);
        add(params, "endTrPrc", endTrPrc);
        add(params, "beginLstgMrktTotAmt", beginLstgMrktTotAmt);
        add(params, "endLstgMrktTotAmt", endLstgMrktTotAmt);
        add(params, "beginLsYrEdVsFltRg", beginLsYrEdVsFltRg);
        add(params, "endLsYrEdVsFltRg", endLsYrEdVsFltRg);
        add(params, "beginLsYrEdVsFltRt", beginLsYrEdVsFltRt);
        add(params, "endLsYrEdVsFltRt", endLsYrEdVsFltRt);

        return params;
    }

    private static void add(MultiValueMap<String, String> params, String name, Object value) {
        if (value == null) {
            return;
        }

        String stringValue = String.valueOf(value);

        if (stringValue.isBlank()) {
            return;
        }

        params.add(name, stringValue);
    }
}
