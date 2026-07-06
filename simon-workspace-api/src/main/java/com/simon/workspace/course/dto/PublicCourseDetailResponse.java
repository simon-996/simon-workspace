package com.simon.workspace.course.dto;

import java.util.List;

public record PublicCourseDetailResponse(
        CourseResponse course,
        List<CourseMaterialResponse> documents,
        List<CourseMaterialResponse> courseware,
        List<CourseMaterialResponse> resources
) {
}
