package com.simon.workspace.classinfo.dto;

import com.simon.workspace.classinfo.model.ClassInfo;

import java.time.LocalDateTime;

public record ClassInfoResponse(
        String id,
        String className,
        String major,
        String grade,
        Integer studentCount,
        String counselor,
        String remark,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public static ClassInfoResponse from(ClassInfo classInfo) {
        return new ClassInfoResponse(
                String.valueOf(classInfo.id()),
                classInfo.className(),
                classInfo.major(),
                classInfo.grade(),
                classInfo.studentCount(),
                classInfo.counselor(),
                classInfo.remark(),
                classInfo.createdTime(),
                classInfo.updatedTime()
        );
    }
}
