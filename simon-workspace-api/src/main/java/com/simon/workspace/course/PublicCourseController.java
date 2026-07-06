package com.simon.workspace.course;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.course.dto.CourseResponse;
import com.simon.workspace.course.dto.PublicCourseDetailResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/courses")
public class PublicCourseController {

    private final CourseService courseService;

    public PublicCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<CourseResponse>> list() {
        return ApiResponse.ok(courseService.publicList());
    }

    @GetMapping("/{id}")
    public ApiResponse<PublicCourseDetailResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(courseService.publicDetail(id));
    }
}
