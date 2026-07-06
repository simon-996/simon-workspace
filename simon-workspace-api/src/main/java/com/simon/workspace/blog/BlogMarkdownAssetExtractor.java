package com.simon.workspace.blog;

import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BlogMarkdownAssetExtractor {

    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[[^\\]]*]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern INTERNAL_FILE_PATTERN = Pattern.compile("(?:^|/)api/files/(\\d+)/download|(?:^|/)files/(\\d+)/download");

    private BlogMarkdownAssetExtractor() {
    }

    public static BlogMarkdownAssets extract(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return new BlogMarkdownAssets(List.of(), List.of());
        }

        Set<Long> fileIds = new LinkedHashSet<>();
        Set<String> externalUrls = new LinkedHashSet<>();
        Matcher imageMatcher = IMAGE_PATTERN.matcher(markdown);
        while (imageMatcher.find()) {
            String url = imageMatcher.group(1).trim();
            Matcher fileMatcher = INTERNAL_FILE_PATTERN.matcher(url);
            if (fileMatcher.find()) {
                String id = fileMatcher.group(1) != null ? fileMatcher.group(1) : fileMatcher.group(2);
                fileIds.add(Long.parseLong(id));
            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                externalUrls.add(url);
            }
        }
        return new BlogMarkdownAssets(List.copyOf(fileIds), List.copyOf(externalUrls));
    }
}
