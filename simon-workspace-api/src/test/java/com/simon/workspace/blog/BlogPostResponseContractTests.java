package com.simon.workspace.blog;

import com.simon.workspace.blog.dto.BlogPostDetailResponse;
import com.simon.workspace.blog.dto.BlogPostSummaryResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlogPostResponseContractTests {

    @Test
    void blogPostResponsesExposeAuthorUserIdForOwnerActions() {
        assertThat(recordComponentNames(BlogPostSummaryResponse.class))
                .containsSubsequence("authorName", "authorUserId", "category");
        assertThat(recordComponentNames(BlogPostDetailResponse.class))
                .containsSubsequence("authorName", "authorUserId", "category");
    }

    private static List<String> recordComponentNames(Class<? extends Record> type) {
        return Arrays.stream(type.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
    }
}
