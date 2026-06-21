package com.simon.workspace.template;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.template.dto.TemplateFieldResponse;
import com.simon.workspace.template.dto.TemplateFieldsRequest;
import com.simon.workspace.template.dto.TemplateResponse;
import com.simon.workspace.template.dto.TemplateUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<TemplateResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(templateService.list(keyword));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<TemplateResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(templateService.upload(file, templateName, templateType, description, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(templateService.detail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TemplateResponse> update(
            @PathVariable long id,
            @Valid @RequestBody TemplateUpdateRequest request
    ) {
        return ApiResponse.ok(templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        templateService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/fields")
    public ApiResponse<List<TemplateFieldResponse>> fields(@PathVariable long id) {
        return ApiResponse.ok(templateService.fields(id));
    }

    @PutMapping("/{id}/fields")
    public ApiResponse<List<TemplateFieldResponse>> updateFields(
            @PathVariable long id,
            @Valid @RequestBody TemplateFieldsRequest request
    ) {
        return ApiResponse.ok(templateService.updateFields(id, request));
    }
}
