package com.simon.workspace.file.model;

import org.springframework.core.io.Resource;

public record FileDownload(
        Resource resource,
        String originalFilename,
        String contentType,
        long fileSize
) {
}
