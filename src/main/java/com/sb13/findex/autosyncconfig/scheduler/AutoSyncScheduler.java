package com.sb13.findex.autosyncconfig.scheduler;

import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;
import com.sb13.findex.autosyncconfig.dto.projection.AutoSyncTargetProjection;
import com.sb13.findex.autosyncconfig.service.AutoSyncConfigService;
import com.sb13.findex.sync.service.SyncJobManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoSyncScheduler {

    private static final String SCHEDULER_WORKER = "system";

    private final AutoSyncConfigService autoSyncConfigService;
    private final SyncJobManager syncJobManager;
    @Value("${findex.batch.auto-sync.zone:Asia/Seoul}")
    private String autoSyncZone;

    @Scheduled(
            cron = "${findex.batch.auto-sync.cron}",
            zone = "${findex.batch.auto-sync.zone:Asia/Seoul}"
    )
    public void syncEnabledIndexData() {
        List<AutoSyncTargetProjection> targets = autoSyncConfigService.getEnabledTargetsWithLatestBaseDate();

        if (targets.isEmpty()) {
            log.info("활성화된 자동 연동 설정이 없어 배치를 종료합니다.");
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of(autoSyncZone));

        LocalDate oldestLatestBaseDate = getOldestLatestBaseDate(targets);

        boolean allTargetsUpToDate = isAllTargetsUpToDate(targets, today);

        if (allTargetsUpToDate) {
            log.info(
                    "모든 대상 지수가 이미 최신 상태입니다. 배치를 종료합니다. "
                            + "(가장 오래된 최신 기준일={}, 오늘={})",
                    oldestLatestBaseDate,
                    today
            );
            return;
        }

        List<IndexDataSyncCommand> commands = targets.stream()
                .filter(target -> {
                    LocalDate latestBaseDate = target.getLatestBaseDate();
                    return latestBaseDate == null || latestBaseDate.isBefore(today);
                })
                .map(target -> createSyncCommand(target, today))
                .toList();

        try {
            syncJobManager.syncIndexDataList(commands, SCHEDULER_WORKER);

            log.info(
                    "자동 연동 배치 실행 완료. 대상 지수 수={}, 전체 조회 기간={} ~ {}",
                    commands.size(),
                    oldestLatestBaseDate,
                    today
            );
        } catch (Exception e) {
            log.error(
                    "자동 연동 배치 실행 실패. 대상 지수 수={}, 전체 조회 기간={} ~ {}",
                    commands.size(),
                    oldestLatestBaseDate,
                    today,
                    e
            );
        }
    }

    private boolean isAllTargetsUpToDate(List<AutoSyncTargetProjection> targets, LocalDate today) {
        return targets.stream()
                .map(AutoSyncTargetProjection::getLatestBaseDate)
                .allMatch(latestBaseDate ->
                        latestBaseDate != null
                                && !latestBaseDate.isBefore(today)
                );
    }

    private @Nullable LocalDate getOldestLatestBaseDate(List<AutoSyncTargetProjection> targets) {
        return targets.stream()
                .map(AutoSyncTargetProjection::getLatestBaseDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    private IndexDataSyncCommand createSyncCommand(AutoSyncTargetProjection config, LocalDate today) {
        return new IndexDataSyncCommand(config.getIndexInfoId(), config.getLatestBaseDate(), today);
    }
}