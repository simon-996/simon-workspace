package com.simon.workspace.blog;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlogServiceTests {

    @Test
    void deleteCategoryRejectsCategoryWithPosts() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BlogService service = new BlogService(jdbcTemplate);
        when(jdbcTemplate.queryForObject(contains("FROM blog_post"), eq(Integer.class), eq(3L))).thenReturn(2);

        assertThatThrownBy(() -> service.deleteCategory(3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("分类下仍有文章，不能删除");

        verify(jdbcTemplate, never()).update(contains("UPDATE blog_category SET deleted"), eq(3L));
    }
}
