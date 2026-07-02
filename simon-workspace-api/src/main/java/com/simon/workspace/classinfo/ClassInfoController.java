package com.simon.workspace.classinfo;

import com.simon.workspace.auth.permission.RequirePermission;
import com.simon.workspace.classinfo.dto.ClassInfoRequest;
import com.simon.workspace.classinfo.dto.ClassInfoResponse;
import com.simon.workspace.common.ApiResponse;
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
@RequestMapping("/api/classes")
@RequirePermission("class:manage")
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    public ClassInfoController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @GetMapping
    public ApiResponse<List<ClassInfoResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(classInfoService.list(keyword));
    }

    @PostMapping
    public ApiResponse<ClassInfoResponse> create(@Valid @RequestBody ClassInfoRequest request) {
        return ApiResponse.ok(classInfoService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClassInfoResponse> update(@PathVariable long id, @Valid @RequestBody ClassInfoRequest request) {
        return ApiResponse.ok(classInfoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        classInfoService.delete(id);
        return ApiResponse.ok(null);
    }
}
