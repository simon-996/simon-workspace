package com.simon.workspace.blog;

import java.util.List;

public record BlogMarkdownAssets(
        List<Long> fileIds,
        List<String> externalUrls
) {
    public BlogMarkdownAssets {
        fileIds = fileIds == null ? List.of() : List.copyOf(fileIds);
        externalUrls = externalUrls == null ? List.of() : List.copyOf(externalUrls);
    }
}
