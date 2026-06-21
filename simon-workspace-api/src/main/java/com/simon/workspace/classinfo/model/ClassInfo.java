package com.simon.workspace.classinfo.model;

import java.time.LocalDateTime;

public record ClassInfo(
        long id,
        String className,
        String major,
        String grade,
        Integer studentCount,
        String counselor,
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
