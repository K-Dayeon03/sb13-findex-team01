package com.sb13.findex.dto.response;

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
        if (response == null) throw new NullPointerException("response is null");
        if (response.body() == null) throw new NullPointerException("response.body is null");
        if (response.body().items() == null) throw new NullPointerException("response.body.items is null");
        if (response.body().items().item() == null) throw new NullPointerException("response.body.items.item is null");

        return response.body().items().item();
    }

    public String getResultCode() {
        if (response == null) throw new NullPointerException("response is null");
        if (response.header() == null) throw new NullPointerException("response.header is null");
        if (response.header().resultCode() == null) throw new NullPointerException("response.header.resultCode is null");

        return response.header().resultCode();
    }

}
