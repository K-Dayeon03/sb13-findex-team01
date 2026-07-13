package com.sb13.findex.sync.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public record DataGoKrApiResponse<T>(
        Response<T> response
) {

    public record Response<T>(
            Header header,
            Body<T> body
    ) {

    }

    public record Header(
            String resultCode,
            String resultMsg
    ) {

    }

    public record Body<T>(
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount,
            Items<T> items
    ) {

    }


    /*
     * @JsonIgnoreProperties(ignoreUnknown = true)는 Jackson이 JSON을 Java 객체로 변환할 때, DTO에 없는 필드가 JSON에 있어도 무시하라는 설정
     * JSON 응답에 Java DTO가 모르는 필드가 있어도 에러 내지 말고 무시해라.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items<T>(
            /*
             * @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)는 item이 배열이 아니라 단건 객체로 내려오는 경우까지 대비하려고 넣은 것입니다.
             * 공공데이터 API 쪽은 numOfRows=1일 때 이런 형태 차이로 역직렬화 문제가 나는 경우가 있어서 방어적으로 넣어두면 좋습니다.
             */
            @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            List<T> item
    ) {
    }

    public List<T> getItem() {
        if (
                response == null ||
                        response.body == null ||
                        response.body.items == null ||
                        response.body.items.item == null
        ) {
            return List.of();
        }

        return response.body.items.item;
    }

    public String getResultCode() {
        if (response == null || response.header == null) {
            return null;
        }

        return response.header.resultCode;
    }

    public Integer getNumOfRows() {
        if (response == null || response.body == null) {
            return null;
        }

        return response.body.numOfRows;
    }

    public Integer getPageNo() {
        if (response == null || response.body == null) {
            return null;
        }
        return response.body.pageNo;
    }

    public Integer getTotalPages() {
        if (response == null || response.body == null) {
            return null;
        }

        Integer numOfRows = getNumOfRows();
        Integer totalCount = response.body.totalCount;

        if (numOfRows == null || totalCount == null) {
            return null;
        }

        if (numOfRows <= 0) {
            throw new IllegalStateException("numOfRows는 0보다 커야 합니다.");
        }

        return (totalCount + numOfRows - 1) / numOfRows;
    }

}
