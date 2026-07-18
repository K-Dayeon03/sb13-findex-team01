// service/AutoSyncConfigCommand.java
package com.sb13.findex.autosyncconfig.dto.command;


import com.sb13.findex.indexinfo.entity.IndexInfo;

// AutoSyncConfig 생성에 필요한 내부 파라미터
public record AutoSyncConfigCommand(IndexInfo indexInfo, boolean enabled) {


}