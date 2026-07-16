package com.sb13.findex.sync.scheduler;

import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;
import com.sb13.findex.sync.dto.projection.AutoSyncTargetProjection;
import com.sb13.findex.sync.service.AutoSyncConfigService;
import com.sb13.findex.sync.service.SyncJobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoSyncScheduler {

    private static final String SCHEDULER_WORKER = "system";

    private static final long DEFAULT_LOOKBACK_DAYS = 1;

    private final AutoSyncConfigService autoSyncConfigService;
    private final SyncJobManager syncJobManager;

    @Scheduled(cron = "${findex.batch.auto-sync.cron}")
    public void syncEnabledIndexData() {
        List<AutoSyncTargetProjection> targets = autoSyncConfigService.getEnabledTargetsWithLatestBaseDate();

        if (targets.isEmpty()) {
            log.info("활성화된 자동 연동 설정이 없어 배치를 종료합니다.");
            return;
        }

        List<Long> indexInfoIds = targets.stream()
                .map(AutoSyncTargetProjection::getIndexInfoId)
                .toList();

        LocalDate today = LocalDate.now();

        // 대상 지수들 중 가장 오래된 마지막 저장 날짜의 다음 날을 조회 시작일로 설정
        LocalDate from = targets.stream()
                .map(AutoSyncTargetProjection::getLatestBaseDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .map(date -> date.plusDays(1))
                .orElse(today.minusDays(DEFAULT_LOOKBACK_DAYS));

        if (from.isAfter(today)) {
            log.info("모든 대상 지수가 이미 최신 상태입니다. 배치를 종료합니다. (계산된 시작일={}, 오늘={})", from, today);
            return;
        }

        try {
            // 실제 연동 실행. 실패한 지수는 IndexData에 반영되지 않음
            IndexDataSyncCommand command = new IndexDataSyncCommand(indexInfoIds, from, today);
            syncJobManager.syncIndexDataList(command, SCHEDULER_WORKER);

            log.info("자동 연동 배치 실행 완료. 대상 지수 수={}, 기간={} ~ {}", indexInfoIds.size(), from, today);
        } catch (Exception e) {
            log.error("자동 연동 배치 실행 실패. 대상 지수 수={}, 기간={} ~ {}", indexInfoIds.size(), from, today, e);
        }
    }
}