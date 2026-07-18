package com.sb13.findex.indexdata.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.sb13.findex.indexdata.controller.swagger.IndexDataApi;
import com.sb13.findex.indexdata.dto.request.IndexDataCreateRequest;
import com.sb13.findex.indexdata.dto.request.IndexDataUpdateRequest;
import com.sb13.findex.indexdata.dto.response.IndexDataResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

class IndexDataApiContractTest {

    @Test
    void dashboardSwaggerApiMethodsMatchControllerSignatures() throws Exception {
        assertControllerImplementsApiMethod("getFavoritePerformance", com.sb13.findex.indexdata.entity.UnitPeriodType.class);
        assertControllerImplementsApiMethod("getIndexChart", Long.class, com.sb13.findex.indexdata.entity.ChartPeriodType.class);
        assertControllerImplementsApiMethod(
                "getPerformanceRank",
                Long.class,
                com.sb13.findex.indexdata.entity.UnitPeriodType.class,
                int.class
        );
    }

    @Test
    void indexDataMutationAndExportMethodsKeepDocumentedResponseBodyTypes() throws Exception {
        assertResponseEntityBodyType(
                IndexDataApi.class.getMethod("createIndexData", IndexDataCreateRequest.class),
                IndexDataResponse.class
        );
        assertResponseEntityBodyType(
                IndexDataController.class.getMethod("createIndexData", IndexDataCreateRequest.class),
                IndexDataResponse.class
        );
        assertResponseEntityBodyType(
                IndexDataApi.class.getMethod("updateIndexData", Long.class, IndexDataUpdateRequest.class),
                IndexDataResponse.class
        );
        assertResponseEntityBodyType(
                IndexDataController.class.getMethod("updateIndexData", Long.class, IndexDataUpdateRequest.class),
                IndexDataResponse.class
        );
        assertResponseEntityBodyType(
                IndexDataApi.class.getMethod(
                        "exportCsv",
                        Long.class,
                        java.time.LocalDate.class,
                        java.time.LocalDate.class,
                        String.class,
                        String.class
                ),
                StreamingResponseBody.class
        );
        assertResponseEntityBodyType(
                IndexDataController.class.getMethod(
                        "exportCsv",
                        Long.class,
                        java.time.LocalDate.class,
                        java.time.LocalDate.class,
                        String.class,
                        String.class
                ),
                StreamingResponseBody.class
        );
    }

    @Test
    void createAndUpdateRequestBodiesRemainValidated() throws Exception {
        assertValidatedRequestBody(
                IndexDataApi.class.getMethod("createIndexData", IndexDataCreateRequest.class),
                0,
                IndexDataCreateRequest.class
        );
        assertValidatedRequestBody(
                IndexDataController.class.getMethod("createIndexData", IndexDataCreateRequest.class),
                0,
                IndexDataCreateRequest.class
        );
        assertValidatedRequestBody(
                IndexDataApi.class.getMethod("updateIndexData", Long.class, IndexDataUpdateRequest.class),
                1,
                IndexDataUpdateRequest.class
        );
        assertValidatedRequestBody(
                IndexDataController.class.getMethod("updateIndexData", Long.class, IndexDataUpdateRequest.class),
                1,
                IndexDataUpdateRequest.class
        );
    }

    @Test
    void dashboardControllerRequestParametersMatchDocumentedContract() throws Exception {
        Method favorite = IndexDataController.class.getMethod(
                "getFavoritePerformance",
                com.sb13.findex.indexdata.entity.UnitPeriodType.class
        );
        assertThat(favorite.getAnnotation(GetMapping.class).value())
                .containsExactly("/performance/favorite");
        RequestParam favoritePeriodType = favorite.getParameters()[0].getAnnotation(RequestParam.class);
        assertThat(favoritePeriodType.defaultValue()).isEqualTo("DAILY");

        Method chart = IndexDataController.class.getMethod(
                "getIndexChart",
                Long.class,
                com.sb13.findex.indexdata.entity.ChartPeriodType.class
        );
        assertThat(chart.getAnnotation(GetMapping.class).value()).containsExactly("/{id}/chart");
        assertThat(chart.getParameters()[0].getAnnotation(PathVariable.class)).isNotNull();
        RequestParam chartPeriodType = chart.getParameters()[1].getAnnotation(RequestParam.class);
        assertThat(chartPeriodType.defaultValue()).isEqualTo("MONTHLY");

        Method rank = IndexDataController.class.getMethod(
                "getPerformanceRank",
                Long.class,
                com.sb13.findex.indexdata.entity.UnitPeriodType.class,
                int.class
        );
        assertThat(rank.getAnnotation(GetMapping.class).value()).containsExactly("/performance/rank");
        assertThat(rank.getParameters()[0].getAnnotation(RequestParam.class).required()).isFalse();
        assertThat(rank.getParameters()[1].getAnnotation(RequestParam.class).defaultValue()).isEqualTo("DAILY");
        assertThat(rank.getParameters()[2].getAnnotation(RequestParam.class).defaultValue()).isEqualTo("10");
        assertThat(rank.getParameters()[2].getAnnotation(Min.class).value()).isEqualTo(1);
    }

    private void assertControllerImplementsApiMethod(String methodName, Class<?>... parameterTypes) throws Exception {
        Method apiMethod = IndexDataApi.class.getMethod(methodName, parameterTypes);
        Method controllerMethod = IndexDataController.class.getMethod(methodName, parameterTypes);

        assertThat(controllerMethod.getReturnType()).isEqualTo(apiMethod.getReturnType());
    }

    private void assertResponseEntityBodyType(Method method, Class<?> expectedBodyType) {
        Type genericReturnType = method.getGenericReturnType();

        assertThat(genericReturnType).isInstanceOf(ParameterizedType.class);

        ParameterizedType responseType = (ParameterizedType) genericReturnType;
        assertThat(responseType.getRawType()).isEqualTo(ResponseEntity.class);
        assertThat(responseType.getActualTypeArguments()).containsExactly(expectedBodyType);
    }

    private void assertValidatedRequestBody(Method method, int parameterIndex, Class<?> expectedParameterType) {
        Parameter parameter = method.getParameters()[parameterIndex];

        assertThat(parameter.getType()).isEqualTo(expectedParameterType);
        assertThat(parameter.getAnnotation(RequestBody.class)).isNotNull();
        assertThat(parameter.getAnnotation(Valid.class)).isNotNull();
    }
}
