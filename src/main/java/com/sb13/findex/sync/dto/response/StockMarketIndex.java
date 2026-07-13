package com.sb13.findex.sync.dto.response;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 주가지수시세 단일 항목입니다.
 *
 * @param lsYrEdVsFltRt  전년말대비 등락률
 *                       <p>항목명: lsYrEdVsFltRt</p>
 *                       <p>샘플데이터: 4.35</p>
 *                       <p>항목설명: 지수의 전년말대비 등락율</p>
 * @param basPntm        기준시점
 *                       <p>항목명: basPntm</p>
 *                       <p>샘플데이터: 19800104</p>
 *                       <p>항목설명: 지수를 산출하기 위한 기준시점</p>
 * @param basIdx         기준지수
 *                       <p>항목명: basIdx</p>
 *                       <p>샘플데이터: 100</p>
 *                       <p>항목설명: 기준시점의 지수값</p>
 * @param basDt          기준일자
 *                       <p>항목명: basDt</p>
 *                       <p>샘플데이터: 20240731</p>
 *                       <p>항목설명: 기준일자</p>
 * @param idxCsf         지수분류명
 *                       <p>항목명: idxCsf</p>
 *                       <p>샘플데이터: KOSPI시리즈</p>
 *                       <p>항목설명: 지수의 분류명칭</p>
 * @param idxNm          지수명
 *                       <p>항목명: idxNm</p>
 *                       <p>샘플데이터: 코스피</p>
 *                       <p>항목설명: 지수의 명칭</p>
 * @param epyItmsCnt     채용종목 수
 *                       <p>항목명: epyItmsCnt</p>
 *                       <p>샘플데이터: 839</p>
 *                       <p>항목설명: 지수가 채용한 종목 수</p>
 * @param clpr           종가
 *                       <p>항목명: clpr</p>
 *                       <p>샘플데이터: 2770.69</p>
 *                       <p>항목설명: 정규시장의 매매시간 종료시까지 형성되는 최종가격</p>
 * @param vs             대비
 *                       <p>항목명: vs</p>
 *                       <p>샘플데이터: 32.5</p>
 *                       <p>항목설명: 전일 대비 등락</p>
 * @param fltRt          등락률
 *                       <p>항목명: fltRt</p>
 *                       <p>샘플데이터: 1.19</p>
 *                       <p>항목설명: 전일 대비 등락에 따른 비율</p>
 * @param mkp            시가
 *                       <p>항목명: mkp</p>
 *                       <p>샘플데이터: 2745.58</p>
 *                       <p>항목설명: 정규시장의 매매시간 개시 후 형성되는 최초가격</p>
 * @param hipr           고가
 *                       <p>항목명: hipr</p>
 *                       <p>샘플데이터: 2770.7</p>
 *                       <p>항목설명: 하루 중 지수의 최고치</p>
 * @param lopr           저가
 *                       <p>항목명: lopr</p>
 *                       <p>샘플데이터: 2733.63</p>
 *                       <p>항목설명: 하루 중 지수의 최저치</p>
 * @param trqu           거래량
 *                       <p>항목명: trqu</p>
 *                       <p>샘플데이터: 557090057</p>
 *                       <p>항목설명: 지수에 포함된 종목의 거래량 총합</p>
 * @param trPrc          거래대금
 *                       <p>항목명: trPrc</p>
 *                       <p>샘플데이터: 12197991898146</p>
 *                       <p>항목설명: 지수에 포함된 종목의 거래대금 총합</p>
 * @param lstgMrktTotAmt 상장시가총액
 *                       <p>항목명: lstgMrktTotAmt</p>
 *                       <p>샘플데이터: 2262832341048634</p>
 *                       <p>항목설명: 지수에 포함된 종목의 시가총액</p>
 * @param lsYrEdVsFltRg  전년말대비 등락폭
 *                       <p>항목명: lsYrEdVsFltRg</p>
 *                       <p>샘플데이터: 115</p>
 *                       <p>항목설명: 지수의 전년말대비 등락폭</p>
 * @param yrWRcrdHgst    연중기록최고
 *                       <p>항목명: yrWRcrdHgst</p>
 *                       <p>샘플데이터: 2891.35</p>
 *                       <p>항목설명: 지수의 연중최고치</p>
 * @param yrWRcrdHgstDt  연중기록최고 일자
 *                       <p>항목명: yrWRcrdHgstDt</p>
 *                       <p>샘플데이터: 20240711</p>
 *                       <p>항목설명: 지수가 연중최고치를 기록한 날짜</p>
 * @param yrWRcrdLwst    연중기록최저
 *                       <p>항목명: yrWRcrdLwst</p>
 *                       <p>샘플데이터: 0</p>
 *                       <p>항목설명: 지수의 연중최저치</p>
 * @param yrWRcrdLwstDt  연중기록최저 일자
 *                       <p>항목명: yrWRcrdLwstDt</p>
 *                       <p>샘플데이터: 20240801</p>
 *                       <p>항목설명: 지수가 연중최저치를 기록한 날짜</p>
 */
public record StockMarketIndex(
        String lsYrEdVsFltRt,
        String basPntm,
        String basIdx,
        String basDt,
        String idxCsf,
        String idxNm,
        String epyItmsCnt,
        String clpr,
        String vs,
        String fltRt,
        String mkp,
        String hipr,
        String lopr,
        String trqu,
        String trPrc,
        String lstgMrktTotAmt,
        String lsYrEdVsFltRg,
        String yrWRcrdHgst,
        String yrWRcrdHgstDt,
        String yrWRcrdLwst,
        String yrWRcrdLwstDt
) {
    private static final DateTimeFormatter BASIC_ISO_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public LocalDate parseBasDt() {
        if (!StringUtils.hasText(basDt)) {
            return null;
        }
        return LocalDate.parse(basDt, BASIC_ISO_DATE_FORMATTER);
    }

    public BigDecimal parseBasIdx() {
        if (!StringUtils.hasText(basIdx)) {
            return null;
        }
        return new BigDecimal(basIdx);
    }

    public Integer parseEpyItmsCnt() {
        if (!StringUtils.hasText(epyItmsCnt)) {
            return null;
        }
        return Integer.parseInt(epyItmsCnt);
    }

    public LocalDate parseBasPntm() {
        if (!StringUtils.hasText(basPntm)) {
            return null;
        }
        return LocalDate.parse(basPntm, BASIC_ISO_DATE_FORMATTER);
    }

}
