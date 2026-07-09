package com.sb13.findex.domain.syncjob;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Table(name = "sync_job")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_info_id")
    private Long indexInfoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 30, nullable = false)
    private JobType jobType;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "worker", length = 100, nullable = false)
    private String worker;

    @Column(name = "job_time", nullable = false)
    private LocalDateTime jobTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 30, nullable = false)
    private JobResult result;

    public SyncJob(Long indexInfoId, JobType jobType, LocalDate targetDate, String worker, LocalDateTime jobTime, JobResult result) {
        this.indexInfoId = indexInfoId;
        this.jobType = jobType;
        this.targetDate = targetDate;
        this.worker = worker;
        this.jobTime = jobTime;
        this.result = result;
    }
}
