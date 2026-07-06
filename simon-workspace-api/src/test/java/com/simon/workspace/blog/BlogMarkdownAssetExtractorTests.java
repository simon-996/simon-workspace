package com.simon.workspace.blog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlogMarkdownAssetExtractorTests {

    @Test
    void extractsInternalFileIdsAndExternalImageUrlsFromMarkdown() {
        BlogMarkdownAssets assets = BlogMarkdownAssetExtractor.extract("""
                intro
                ![local](/api/files/12/download)
                ![public](https://pub.example.com/files/2026/07/cover.webp)
                ![duplicate](/files/12/download?download=1)
                [not-image](https://pub.example.com/not-image.webp)
                """);

        assertThat(assets.fileIds()).containsExactly(12L);
        assertThat(assets.externalUrls()).containsExactly("https://pub.example.com/files/2026/07/cover.webp");
    }
}
