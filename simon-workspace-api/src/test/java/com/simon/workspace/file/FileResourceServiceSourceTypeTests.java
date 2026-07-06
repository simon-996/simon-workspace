package com.simon.workspace.file;

import com.simon.workspace.storage.StorageProviderRegistry;
import com.simon.workspace.storage.StorageProviderStateService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FileResourceServiceSourceTypeTests {

    @Test
    void acceptsAvatarAsAFileSourceType() {
        FileResourceService service = new FileResourceService(
                mock(JdbcTemplate.class),
                mock(StorageProviderStateService.class),
                mock(StorageProviderRegistry.class)
        );

        String sourceType = ReflectionTestUtils.invokeMethod(service, "normalizeSourceType", "avatar");

        assertThat(sourceType).isEqualTo("AVATAR");
    }

    @Test
    void acceptsBlogEditorAsAFileSourceType() {
        FileResourceService service = new FileResourceService(
                mock(JdbcTemplate.class),
                mock(StorageProviderStateService.class),
                mock(StorageProviderRegistry.class)
        );

        String sourceType = ReflectionTestUtils.invokeMethod(service, "normalizeSourceType", "blog_editor");

        assertThat(sourceType).isEqualTo("BLOG_EDITOR");
    }
}
