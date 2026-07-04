package com.simon.workspace.storage;

import org.springframework.core.io.Resource;

public record StorageObjectDownload(
        Resource resource,
        long fileSize
) {
}
