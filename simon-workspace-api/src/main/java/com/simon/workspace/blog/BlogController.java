package com.simon.workspace.blog;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.simon.workspace.auth.model.CurrentUser;
import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.blog.dto.BlogCategoryRequest;
import com.simon.workspace.blog.dto.BlogCategoryResponse;
import com.simon.workspace.blog.dto.BlogCommentRequest;
import com.simon.workspace.blog.dto.BlogCommentResponse;
import com.simon.workspace.blog.dto.BlogPostDetailResponse;
import com.simon.workspace.blog.dto.BlogPostRequest;
import com.simon.workspace.blog.dto.BlogPostSummaryResponse;
import com.simon.workspace.blog.dto.BlogTagResponse;
import com.simon.workspace.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;
    private final BlogPostService blogPostService;

    public BlogController(BlogService blogService, BlogPostService blogPostService) {
        this.blogService = blogService;
        this.blogPostService = blogPostService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<BlogCategoryResponse>> categories() {
        return ApiResponse.ok(blogService.categories());
    }

    @PostMapping("/categories")
    @SaCheckPermission("blog:category:manage")
    public ApiResponse<BlogCategoryResponse> createCategory(@Valid @RequestBody BlogCategoryRequest request) {
        return ApiResponse.ok(blogService.saveCategory(null, request));
    }

    @PutMapping("/categories/{id}")
    @SaCheckPermission("blog:category:manage")
    public ApiResponse<BlogCategoryResponse> updateCategory(
            @PathVariable long id,
            @Valid @RequestBody BlogCategoryRequest request
    ) {
        return ApiResponse.ok(blogService.saveCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @SaCheckPermission("blog:category:manage")
    public ApiResponse<Void> deleteCategory(@PathVariable long id) {
        blogService.deleteCategory(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/tags")
    public ApiResponse<List<BlogTagResponse>> tags(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(blogService.tags(keyword));
    }

    @GetMapping("/posts")
    public ApiResponse<List<BlogPostSummaryResponse>> posts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag
    ) {
        return ApiResponse.ok(blogService.posts(keyword, categoryId, tag));
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<BlogPostDetailResponse> detail(@PathVariable long id) {
        return ApiResponse.ok(blogService.detail(id));
    }

    @PostMapping("/posts")
    @SaCheckPermission("blog:post:create")
    public ApiResponse<BlogPostDetailResponse> createPost(@Valid @RequestBody BlogPostRequest request) {
        return ApiResponse.ok(blogPostService.save(null, request));
    }

    @PutMapping("/posts/{id}")
    @SaCheckPermission("blog:post:update")
    public ApiResponse<BlogPostDetailResponse> updatePost(
            @PathVariable long id,
            @Valid @RequestBody BlogPostRequest request
    ) {
        return ApiResponse.ok(blogPostService.save(id, request));
    }

    @DeleteMapping("/posts/{id}")
    @SaCheckPermission("blog:post:delete")
    public ApiResponse<Void> deletePost(@PathVariable long id) {
        blogPostService.delete(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/posts/{id}/comments")
    public ApiResponse<List<BlogCommentResponse>> comments(@PathVariable long id) {
        return ApiResponse.ok(blogService.comments(id));
    }

    @PostMapping("/posts/{id}/comments")
    @SaCheckLogin
    public ApiResponse<BlogCommentResponse> comment(
            @PathVariable long id,
            @Valid @RequestBody BlogCommentRequest request
    ) {
        CurrentUser user = AuthContextHolder.requireUser();
        return ApiResponse.ok(blogService.comment(id, user.id(), user.nickname(), request));
    }
}
