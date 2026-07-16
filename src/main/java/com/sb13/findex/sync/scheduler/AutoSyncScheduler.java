package com.sb13.findex.sync.scheduler;

import com.sb13.findex.sync.dto.AutoSyncTarget;
import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;
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
    private static final String SCHEDULER_WORKER = "scheduler";
    // 한 번도 연동된 적 없는 지수(lastSyncedDate == null)만 있을 때 사용하는 기본 조회 시작일
    private static final long DEFAULT_LOOKBACK_DAYS = 1;

    private final AutoSyncConfigService autoSyncConfigService;
    private final SyncJobManager syncJobManager;

    @Scheduled(cron = "${findex.batch.auto-sync.cron}")
    public void syncEnabledIndexData() {
        List<AutoSyncTarget> targets = autoSyncConfigService.getEnabledSyncTargets();

        if (targets.isEmpty()) {
            log.info("활성화된 자동 연동 설정이 없어 배치를 종료합니다.");
            return;
        }

        List<Long> indexInfoIds = targets.stream()
                .map(AutoSyncTarget::indexInfoId)
                .toList();

        LocalDate today = LocalDate.now();
        LocalDate from = targets.stream()
                .map(AutoSyncTarget::lastSyncedDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .map(date -> date.plusDays(1))
                .orElse(today.minusDays(DEFAULT_LOOKBACK_DAYS));

        try {
            IndexDataSyncCommand command = new IndexDataSyncCommand(indexInfoIds, from, today);
            syncJobManager.syncIndexDataList(command, SCHEDULER_WORKER);
            autoSyncConfigService.updateLastSyncedDate(indexInfoIds, today);

            log.info("자동 연동 배치 실행 완료. 대상 지수 수={}, 기간={} ~ {}", indexInfoIds.size(), from, today);
        } catch (Exception e) {
            log.error("자동 연동 배치 실행 실패. 대상 지수 수={}, 기간={} ~ {}", indexInfoIds.size(), from, today, e);
        }
    }
}