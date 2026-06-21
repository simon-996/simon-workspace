package com.simon.workspace.file;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.file.dto.FileResourceResponse;
import com.simon.workspace.file.model.FileDownload;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileResourceController {

    private final FileResourceService fileResourceService;

    public FileResourceController(FileResourceService fileResourceService) {
        this.fileResourceService = fileResourceService;
    }

    @GetMapping
    public ApiResponse<List<FileResourceResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(fileResourceService.list(keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<FileResourceResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(fileResourceService.detail(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable long id) {
        FileDownload download = fileResourceService.download(id);
        return ResponseEntity.ok()
                .contentLength(download.fileSize())
                .contentType(mediaType(download.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.originalFilename(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(download.resource());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        fileResourceService.delete(id);
        return ApiResponse.ok(null);
    }

    private MediaType mediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
