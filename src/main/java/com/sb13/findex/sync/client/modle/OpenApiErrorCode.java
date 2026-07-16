package com.sb13.findex.sync.client.modle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum OpenApiErrorCode {

    APPLICATION_ERROR("1", "어플리케이션 에러"),
    INVALID_REQUEST_PARAMETER_ERROR("10", "잘못된 요청 파라미터 에러"),
    NO_OPENAPI_SERVICE_ERROR("12", "해당 오픈API 서비스가 없거나 폐기됨"),
    SERVICE_ACCESS_DENIED_ERROR("20", "서비스 접근 거부"),
    LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR(
            "22",
            "서비스 요청 제한 횟수 초과"
    ),
    SERVICE_KEY_IS_NOT_REGISTERED_ERROR("30", "등록되지 않은 서비스 키"),
    DEADLINE_HAS_EXPIRED_ERROR("31", "기한이 만료된 서비스 키"),
    UNREGISTERED_IP_ERROR("32", "등록되지 않은 IP"),
    UNKNOWN_ERROR("99", "기타 에러");

    private final String code;
    private final String message;

    public static OpenApiErrorCode from(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN_ERROR;
        }
        String normalizedCode = code.strip();

        return Arrays.stream(values())
                .filter(errorCode -> errorCode.code.equals(normalizedCode))
                .findFirst()
                .orElse(UNKNOWN_ERROR);
    }

}
