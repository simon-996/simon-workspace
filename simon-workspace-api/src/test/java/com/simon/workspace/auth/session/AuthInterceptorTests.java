package com.simon.workspace.auth.session;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.workspace.auth.AuthAccountService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class AuthInterceptorTests {

    private final AuthInterceptor interceptor = new AuthInterceptor(
            mock(AuthAccountService.class),
            new ObjectMapper()
    );

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/blog/categories",
            "/api/blog/tags",
            "/api/blog/posts",
            "/api/blog/posts/42",
            "/api/blog/posts/42/comments"
    })
    void allowsAnonymousBlogReadRequests(String path) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = anonymousRequestAllowed("GET", path, response);

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "GET /api/blog/manage/posts",
            "POST /api/blog/posts",
            "PUT /api/blog/posts/42",
            "DELETE /api/blog/posts/42",
            "POST /api/blog/posts/42/comments"
    })
    void keepsBlogManagementAndWritesProtected(String requestSpec) throws Exception {
        String[] requestParts = requestSpec.split(" ", 2);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = anonymousRequestAllowed(requestParts[0], requestParts[1], response);

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    private boolean anonymousRequestAllowed(
            String method,
            String path,
            MockHttpServletResponse response
    ) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::checkLogin).thenThrow(new NotLoginException(
                    "Not logged in",
                    "login",
                    NotLoginException.NOT_TOKEN
            ));
            return interceptor.preHandle(request, response, new Object());
        }
    }
}
