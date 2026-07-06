package com.simon.workspace.blog;

import com.simon.workspace.auth.model.CurrentUser;
import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.blog.dto.BlogPostDetailResponse;
import com.simon.workspace.blog.dto.BlogPostRequest;
import com.simon.workspace.file.FileReferenceService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
public class BlogPostService extends BlogService {

    private final FileReferenceService fileReferenceService;

    public BlogPostService(JdbcTemplate jdbcTemplate, FileReferenceService fileReferenceService) {
        super(jdbcTemplate);
        this.fileReferenceService = fileReferenceService;
    }

    @Transactional
    public BlogPostDetailResponse save(Long id, BlogPostRequest request) {
        CurrentUser user = AuthContextHolder.requireUser();
        String title = required(request.title(), "文章标题不能为空");
        String content = required(request.contentMd(), "文章内容不能为空");
        String status = normalizeStatus(request.status(), "DRAFT", List.of("DRAFT", "PUBLISHED"));
        String slug = id == null ? uniqueSlug("blog_post", null, request.slug(), title) : uniqueSlug("blog_post", id, request.slug(), title);
        Long postId = id == null
                ? insertPost(user.id(), title, request.summary(), slug, request.categoryId(), content, status)
                : updatePost(id, user.id(), title, request.summary(), slug, request.categoryId(), content, status);
        syncTags(postId, request.tags());
        syncAssets(postId, content);
        return new BlogPostDetailResponse(String.valueOf(postId), title, request.summary(), slug, content, status,
                user.nickname(), null, List.of(), 0L, 0L,
                "PUBLISHED".equals(status) ? LocalDateTime.now() : null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Transactional
    public void delete(long id) {
        CurrentUser user = AuthContextHolder.requireUser();
        int affected = jdbcTemplate.update("""
                        UPDATE blog_post
                        SET deleted = 1
                        WHERE id = ? AND author_user_id = ? AND deleted = 0
                        """,
                id,
                user.id()
        );
        if (affected == 0) {
            throw new IllegalArgumentException("文章不存在或无权操作");
        }
        fileReferenceService.syncReferences("BLOG_POST", String.valueOf(id), "CONTENT_IMAGE", List.of());
    }

    private long insertPost(
            long authorUserId,
            String title,
            String summary,
            String slug,
            Long categoryId,
            String content,
            String status
    ) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO blog_post (
                                author_user_id, category_id, title, slug, summary, content_md, status, published_time
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, CASE WHEN ? = 'PUBLISHED' THEN CURRENT_TIMESTAMP ELSE NULL END)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, authorUserId);
            if (categoryId == null) {
                statement.setObject(2, null);
            } else {
                statement.setLong(2, categoryId);
            }
            statement.setString(3, title);
            statement.setString(4, slug);
            statement.setString(5, blankToNull(summary));
            statement.setString(6, content);
            statement.setString(7, status);
            statement.setString(8, status);
            return statement;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private long updatePost(
            long id,
            long authorUserId,
            String title,
            String summary,
            String slug,
            Long categoryId,
            String content,
            String status
    ) {
        int affected = jdbcTemplate.update("""
                        UPDATE blog_post
                        SET category_id = ?, title = ?, slug = ?, summary = ?, content_md = ?, status = ?,
                            published_time = CASE
                                WHEN ? = 'PUBLISHED' AND published_time IS NULL THEN CURRENT_TIMESTAMP
                                WHEN ? = 'DRAFT' THEN NULL
                                ELSE published_time
                            END
                        WHERE id = ? AND author_user_id = ? AND deleted = 0
                        """,
                categoryId,
                title,
                slug,
                blankToNull(summary),
                content,
                status,
                status,
                status,
                id,
                authorUserId
        );
        if (affected == 0) {
            throw new IllegalArgumentException("文章不存在或无权操作");
        }
        return id;
    }

    private void syncTags(long postId, List<String> tags) {
        jdbcTemplate.update("UPDATE blog_post_tag SET deleted = 1 WHERE post_id = ? AND deleted = 0", postId);
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (tags != null) {
            tags.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .limit(8)
                    .forEach(normalized::add);
        }
        for (String name : normalized) {
            String slug = slugify(name);
            jdbcTemplate.update("""
                            INSERT INTO blog_tag (name, slug, usage_count)
                            VALUES (?, ?, 0)
                            ON DUPLICATE KEY UPDATE name = VALUES(name), deleted = 0
                            """,
                    name,
                    slug
            );
            jdbcTemplate.update("""
                            INSERT INTO blog_post_tag (post_id, tag_id, deleted)
                            SELECT ?, id, 0 FROM blog_tag WHERE slug = ? AND deleted = 0
                            ON DUPLICATE KEY UPDATE deleted = 0, updated_time = CURRENT_TIMESTAMP
                            """,
                    postId,
                    slug
            );
        }
        jdbcTemplate.update("""
                UPDATE blog_tag t
                SET usage_count = (
                    SELECT COUNT(*)
                    FROM blog_post_tag pt
                    JOIN blog_post p ON p.id = pt.post_id AND p.deleted = 0
                    WHERE pt.tag_id = t.id AND pt.deleted = 0
                )
                """);
    }

    private void syncAssets(long postId, String content) {
        BlogMarkdownAssets assets = BlogMarkdownAssetExtractor.extract(content);
        LinkedHashSet<Long> fileIds = new LinkedHashSet<>(assets.fileIds());
        if (!assets.externalUrls().isEmpty()) {
            String placeholders = String.join(",", java.util.Collections.nCopies(assets.externalUrls().size(), "?"));
            fileIds.addAll(jdbcTemplate.queryForList(
                    "SELECT id FROM file_resource WHERE public_url IN (" + placeholders + ") AND deleted = 0",
                    Long.class,
                    assets.externalUrls().toArray()
            ));
        }
        fileReferenceService.syncReferences("BLOG_POST", String.valueOf(postId), "CONTENT_IMAGE", List.copyOf(fileIds));
    }
}
