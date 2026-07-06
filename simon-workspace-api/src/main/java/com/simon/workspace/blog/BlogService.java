package com.simon.workspace.blog;

import com.simon.workspace.blog.dto.BlogCategoryRequest;
import com.simon.workspace.blog.dto.BlogCategoryResponse;
import com.simon.workspace.blog.dto.BlogCommentRequest;
import com.simon.workspace.blog.dto.BlogCommentResponse;
import com.simon.workspace.blog.dto.BlogPostDetailResponse;
import com.simon.workspace.blog.dto.BlogPostRequest;
import com.simon.workspace.blog.dto.BlogPostSummaryResponse;
import com.simon.workspace.blog.dto.BlogTagResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class BlogService {

    protected final JdbcTemplate jdbcTemplate;

    public BlogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BlogCategoryResponse> categories() {
        return jdbcTemplate.query("""
                        SELECT c.*,
                               COUNT(p.id) AS post_count
                        FROM blog_category c
                        LEFT JOIN blog_post p ON p.category_id = c.id AND p.deleted = 0
                        WHERE c.deleted = 0 AND c.status = 'ACTIVE'
                        GROUP BY c.id
                        ORDER BY c.sort_order ASC, c.id ASC
                        """,
                (rs, rowNum) -> categoryFromRow(rs)
        );
    }

    public List<BlogCategoryResponse> manageCategories() {
        return jdbcTemplate.query("""
                        SELECT c.*,
                               COUNT(p.id) AS post_count
                        FROM blog_category c
                        LEFT JOIN blog_post p ON p.category_id = c.id AND p.deleted = 0
                        WHERE c.deleted = 0
                        GROUP BY c.id
                        ORDER BY c.sort_order ASC, c.id ASC
                        """,
                (rs, rowNum) -> categoryFromRow(rs)
        );
    }

    @Transactional
    public BlogCategoryResponse saveCategory(Long id, BlogCategoryRequest request) {
        String name = required(request.name(), "分类名称不能为空");
        String slug = uniqueSlug("blog_category", id, request.slug(), name);
        String status = normalizeStatus(request.status(), "ACTIVE", List.of("ACTIVE", "DISABLED"));
        int sortOrder = request.sortOrder() == null ? 100 : request.sortOrder();
        if (id == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                                INSERT INTO blog_category (name, slug, description, sort_order, status)
                                VALUES (?, ?, ?, ?, ?)
                                """,
                        Statement.RETURN_GENERATED_KEYS
                );
                statement.setString(1, name);
                statement.setString(2, slug);
                statement.setString(3, blankToNull(request.description()));
                statement.setInt(4, sortOrder);
                statement.setString(5, status);
                return statement;
            }, keyHolder);
            return category(Objects.requireNonNull(keyHolder.getKey()).longValue());
        }

        int affected = jdbcTemplate.update("""
                        UPDATE blog_category
                        SET name = ?, slug = ?, description = ?, sort_order = ?, status = ?
                        WHERE id = ? AND deleted = 0
                        """,
                name,
                slug,
                blankToNull(request.description()),
                sortOrder,
                status,
                id
        );
        if (affected == 0) {
            throw new IllegalArgumentException("分类不存在");
        }
        return category(id);
    }

    @Transactional
    public void deleteCategory(long id) {
        Integer postCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM blog_post WHERE category_id = ? AND deleted = 0",
                Integer.class,
                id
        );
        if (postCount != null && postCount > 0) {
            throw new IllegalArgumentException("分类下仍有文章，不能删除");
        }
        int affected = jdbcTemplate.update("UPDATE blog_category SET deleted = 1 WHERE id = ? AND deleted = 0", id);
        if (affected == 0) {
            throw new IllegalArgumentException("分类不存在");
        }
    }

    public List<BlogTagResponse> tags(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM blog_tag
                            WHERE deleted = 0 AND name LIKE ?
                            ORDER BY usage_count DESC, name ASC
                            LIMIT 50
                            """,
                    (rs, rowNum) -> tag(rs.getLong("id"), rs.getString("name"), rs.getString("slug"), rs.getLong("usage_count")),
                    like
            );
        }
        return jdbcTemplate.query("""
                        SELECT *
                        FROM blog_tag
                        WHERE deleted = 0
                        ORDER BY usage_count DESC, name ASC
                        LIMIT 50
                        """,
                (rs, rowNum) -> tag(rs.getLong("id"), rs.getString("name"), rs.getString("slug"), rs.getLong("usage_count"))
        );
    }

    public List<BlogPostSummaryResponse> posts(String keyword, Long categoryId, String tag) {
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT p.*, u.nickname AS author_name, c.name AS category_name, c.slug AS category_slug,
                       c.description AS category_description, c.sort_order AS category_sort_order, c.status AS category_status,
                       c.created_time AS category_created_time, c.updated_time AS category_updated_time
                FROM blog_post p
                JOIN user u ON u.id = p.author_user_id
                LEFT JOIN blog_category c ON c.id = p.category_id AND c.deleted = 0
                LEFT JOIN blog_post_tag pt ON pt.post_id = p.id AND pt.deleted = 0
                LEFT JOIN blog_tag t ON t.id = pt.tag_id AND t.deleted = 0
                WHERE p.deleted = 0 AND p.status = 'PUBLISHED'
                """);
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (p.title LIKE ? OR p.summary LIKE ? OR p.content_md LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            args.add(categoryId);
        }
        if (StringUtils.hasText(tag)) {
            sql.append(" AND t.slug = ?");
            args.add(slugify(tag));
        }
        sql.append(" ORDER BY p.published_time DESC, p.id DESC");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> summaryFromRow(rs), args.toArray());
    }

    public BlogPostDetailResponse detail(long id) {
        BlogPostDetailResponse post = jdbcTemplate.query("""
                        SELECT p.*, u.nickname AS author_name, c.name AS category_name, c.slug AS category_slug,
                               c.description AS category_description, c.sort_order AS category_sort_order, c.status AS category_status,
                               c.created_time AS category_created_time, c.updated_time AS category_updated_time
                        FROM blog_post p
                        JOIN user u ON u.id = p.author_user_id
                        LEFT JOIN blog_category c ON c.id = p.category_id AND c.deleted = 0
                        WHERE p.id = ? AND p.deleted = 0 AND p.status = 'PUBLISHED'
                        LIMIT 1
                        """,
                (rs, rowNum) -> detailFromRow(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        jdbcTemplate.update("UPDATE blog_post SET view_count = view_count + 1 WHERE id = ?", id);
        return post;
    }

    protected BlogCategoryResponse category(long id) {
        return jdbcTemplate.query("""
                        SELECT c.*,
                               COUNT(p.id) AS post_count
                        FROM blog_category c
                        LEFT JOIN blog_post p ON p.category_id = c.id AND p.deleted = 0
                        WHERE c.id = ? AND c.deleted = 0
                        GROUP BY c.id
                        LIMIT 1
                        """,
                (rs, rowNum) -> categoryFromRow(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("分类不存在"));
    }

    protected BlogCategoryResponse categoryFromRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new BlogCategoryResponse(
                String.valueOf(rs.getLong("id")),
                rs.getString("name"),
                rs.getString("slug"),
                rs.getString("description"),
                rs.getInt("sort_order"),
                rs.getString("status"),
                rs.getLong("post_count"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    protected String uniqueSlug(String table, Long id, String slug, String fallback) {
        String normalized = slugify(StringUtils.hasText(slug) ? slug : fallback);
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE slug = ? AND deleted = 0" + (id == null ? "" : " AND id <> ?");
        Integer count = id == null
                ? jdbcTemplate.queryForObject(sql, Integer.class, normalized)
                : jdbcTemplate.queryForObject(sql, Integer.class, normalized, id);
        if (count != null && count > 0) {
            return normalized + "-" + System.currentTimeMillis();
        }
        return normalized;
    }

    protected BlogTagResponse tag(long id, String name, String slug, long usageCount) {
        return new BlogTagResponse(String.valueOf(id), name, slug, usageCount);
    }

    protected String slugify(String value) {
        String slug = required(value, "slug 不能为空")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-+|-+$", "");
        return StringUtils.hasText(slug) ? slug : "post";
    }

    protected String normalizeStatus(String status, String defaultStatus, List<String> allowed) {
        String normalized = StringUtils.hasText(status) ? status.trim().toUpperCase(Locale.ROOT) : defaultStatus;
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("状态不合法");
        }
        return normalized;
    }

    protected String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    protected String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    protected BlogCategoryResponse nullableCategory(java.sql.ResultSet rs) throws java.sql.SQLException {
        long categoryId = rs.getLong("category_id");
        if (rs.wasNull()) {
            return null;
        }
        Timestamp created = rs.getTimestamp("category_created_time");
        Timestamp updated = rs.getTimestamp("category_updated_time");
        return new BlogCategoryResponse(
                String.valueOf(categoryId),
                rs.getString("category_name"),
                rs.getString("category_slug"),
                rs.getString("category_description"),
                rs.getInt("category_sort_order"),
                rs.getString("category_status"),
                0L,
                created == null ? null : created.toLocalDateTime(),
                updated == null ? null : updated.toLocalDateTime()
        );
    }

    protected BlogPostSummaryResponse summaryFromRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        long id = rs.getLong("id");
        return new BlogPostSummaryResponse(
                String.valueOf(id),
                rs.getString("title"),
                rs.getString("summary"),
                rs.getString("slug"),
                rs.getString("status"),
                rs.getString("author_name"),
                nullableCategory(rs),
                postTags(id),
                rs.getLong("view_count"),
                rs.getLong("comment_count"),
                toLocalDateTime(rs.getTimestamp("published_time")),
                toLocalDateTime(rs.getTimestamp("updated_time"))
        );
    }

    protected BlogPostDetailResponse detailFromRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        long id = rs.getLong("id");
        return new BlogPostDetailResponse(
                String.valueOf(id),
                rs.getString("title"),
                rs.getString("summary"),
                rs.getString("slug"),
                rs.getString("content_md"),
                rs.getString("status"),
                rs.getString("author_name"),
                nullableCategory(rs),
                postTags(id),
                rs.getLong("view_count"),
                rs.getLong("comment_count"),
                toLocalDateTime(rs.getTimestamp("published_time")),
                toLocalDateTime(rs.getTimestamp("created_time")),
                toLocalDateTime(rs.getTimestamp("updated_time"))
        );
    }

    protected List<BlogTagResponse> postTags(long postId) {
        return jdbcTemplate.query("""
                        SELECT t.*
                        FROM blog_tag t
                        JOIN blog_post_tag pt ON pt.tag_id = t.id AND pt.deleted = 0
                        WHERE pt.post_id = ? AND t.deleted = 0
                        ORDER BY t.name ASC
                        """,
                (rs, rowNum) -> tag(rs.getLong("id"), rs.getString("name"), rs.getString("slug"), rs.getLong("usage_count")),
                postId
        );
    }

    protected LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    public List<BlogCommentResponse> comments(long postId) {
        return jdbcTemplate.query("""
                        SELECT c.*, u.nickname AS author_name
                        FROM blog_comment c
                        JOIN user u ON u.id = c.author_user_id
                        WHERE c.post_id = ? AND c.deleted = 0 AND c.status = 'VISIBLE'
                        ORDER BY c.created_time ASC, c.id ASC
                        """,
                (rs, rowNum) -> new BlogCommentResponse(
                        String.valueOf(rs.getLong("id")),
                        String.valueOf(rs.getLong("post_id")),
                        rs.getObject("parent_id") == null ? null : String.valueOf(rs.getLong("parent_id")),
                        rs.getString("author_name"),
                        rs.getString("content"),
                        rs.getString("status"),
                        toLocalDateTime(rs.getTimestamp("created_time"))
                ),
                postId
        );
    }

    @Transactional
    public BlogCommentResponse comment(long postId, long authorUserId, String authorName, BlogCommentRequest request) {
        String content = required(request.content(), "评论内容不能为空");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO blog_comment (post_id, author_user_id, parent_id, content, status)
                            VALUES (?, ?, ?, ?, 'VISIBLE')
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, postId);
            statement.setLong(2, authorUserId);
            if (request.parentId() == null) {
                statement.setObject(3, null);
            } else {
                statement.setLong(3, request.parentId());
            }
            statement.setString(4, content);
            return statement;
        }, keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        jdbcTemplate.update("UPDATE blog_post SET comment_count = comment_count + 1 WHERE id = ? AND deleted = 0", postId);
        return new BlogCommentResponse(String.valueOf(id), String.valueOf(postId),
                request.parentId() == null ? null : String.valueOf(request.parentId()),
                authorName, content, "VISIBLE", LocalDateTime.now());
    }
}
