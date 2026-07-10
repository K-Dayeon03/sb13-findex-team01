package com.sb13.findex.sync.dto.request;

public enum SyncJobSortField {
    TARGET_DATE,
    JOB_TIME;


    public static SyncJobSortField from(String value){
        for (SyncJobSortField field : values()) {
            if (field.name().equalsIgnoreCase(value)) {
                return field;
            }
        }
        return JOB_TIME;
    }
}
