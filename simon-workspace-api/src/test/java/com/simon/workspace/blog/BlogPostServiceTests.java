package com.simon.workspace.blog;

import com.simon.workspace.auth.model.CurrentUser;
import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.blog.dto.BlogPostRequest;
import com.simon.workspace.file.FileReferenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlogPostServiceTests {

    @AfterEach
    void tearDown() {
        AuthContextHolder.clear();
    }

    @Test
    void savePostCreatesTagsAndBindsMarkdownImagesToPost() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        FileReferenceService fileReferenceService = mock(FileReferenceService.class);
        BlogPostService service = new BlogPostService(jdbcTemplate, fileReferenceService);
        AuthContextHolder.set(new CurrentUser(7L, "simon", "Simon", null, null, List.of(), List.of("blog:post:create")));
        when(jdbcTemplate.update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    keyHolder.getKeyList().add(java.util.Map.of("GENERATED_KEY", 42L));
                    return 1;
                });
        when(jdbcTemplate.queryForList(contains("public_url IN"), eq(Long.class),
                eq("https://pub.example.com/files/cover.webp"))).thenReturn(List.of(99L));

        service.save(null, new BlogPostRequest(
                "Hello Blog",
                "A short summary",
                "slug",
                3L,
                List.of("Vue", "Java"),
                "![a](/api/files/12/download)\n![b](https://pub.example.com/files/cover.webp)",
                "PUBLISHED"
        ));

        verify(jdbcTemplate).update(contains("INSERT INTO blog_tag"), eq("Vue"), eq("vue"));
        verify(jdbcTemplate).update(contains("INSERT INTO blog_tag"), eq("Java"), eq("java"));
        verify(fileReferenceService).syncReferences("BLOG_POST", "42", "CONTENT_IMAGE", List.of(12L, 99L));
    }
}
