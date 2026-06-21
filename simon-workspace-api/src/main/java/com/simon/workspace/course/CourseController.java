package com.simon.workspace.course;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.course.dto.CourseRequest;
import com.simon.workspace.course.dto.CourseResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<CourseResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(courseService.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(courseService.detail(id));
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.ok(courseService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(@PathVariable long id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.ok(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        courseService.delete(id);
        return ApiResponse.ok(null);
    }
}
