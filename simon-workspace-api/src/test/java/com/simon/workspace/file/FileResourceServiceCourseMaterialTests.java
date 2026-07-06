package com.simon.workspace.file;

import com.simon.workspace.storage.StorageProviderRegistry;
import com.simon.workspace.storage.StorageProviderStateService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FileResourceServiceCourseMaterialTests {

    @Test
    void acceptsCourseMaterialAsAFileSourceType() {
        FileResourceService service = new FileResourceService(
                mock(JdbcTemplate.class),
                mock(StorageProviderStateService.class),
                mock(StorageProviderRegistry.class)
        );

        assertThatThrownBy(() -> service.saveResource(
                1L,
                "COURSE_MATERIAL",
                new ByteArrayInputStream("hello".getBytes()),
                "lesson.md",
                "text/markdown"
        )).isNotInstanceOf(IllegalArgumentException.class);
    }
}
