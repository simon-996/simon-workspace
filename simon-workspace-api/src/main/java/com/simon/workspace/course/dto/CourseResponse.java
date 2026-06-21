package com.simon.workspace.course.dto;

import com.simon.workspace.course.model.Course;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CourseResponse(
        String id,
        String courseName,
        String courseCode,
        String major,
        String grade,
        Integer totalHours,
        Integer theoryHours,
        Integer experimentHours,
        Integer weeklyHours,
        BigDecimal credit,
        String textbook,
        String courseGoal,
        String keyPoint,
        String difficultPoint,
        String assessmentMethod,
        String syllabus,
        String description,
        String status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                String.valueOf(course.id()),
                course.courseName(),
                course.courseCode(),
                course.major(),
                course.grade(),
                course.totalHours(),
                course.theoryHours(),
                course.experimentHours(),
                course.weeklyHours(),
                course.credit(),
                course.textbook(),
                course.courseGoal(),
                course.keyPoint(),
                course.difficultPoint(),
                course.assessmentMethod(),
                course.syllabus(),
                course.description(),
                course.status(),
                course.createdTime(),
                course.updatedTime()
        );
    }
}
