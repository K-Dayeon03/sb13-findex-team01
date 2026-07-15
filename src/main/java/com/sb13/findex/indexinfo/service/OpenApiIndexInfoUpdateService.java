package com.sb13.findex.indexinfo.service;

import com.sb13.findex.indexinfo.dto.command.*;
import com.sb13.findex.indexinfo.entity.*;
import com.sb13.findex.indexinfo.repository.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class OpenApiIndexInfoUpdateService {

    private final IndexInfoRepository indexInfoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAfterConflict(
            IndexInfoCreateCommand command
    ) {

        String indexClassification =
                command.indexClassification().strip();

        String indexName =
                command.indexName().strip();

        IndexInfo indexInfo =
                indexInfoRepository
                        .findByIndexClassificationAndIndexName(
                                indexClassification,
                                indexName
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "동시 생성된 지수정보를 찾을 수 없습니다."
                                )
                        );

        indexInfo.updateByOpenApi(
                command.employedItemsCount(),
                command.basePointInTime(),
                command.baseIndex()
        );
    }
}