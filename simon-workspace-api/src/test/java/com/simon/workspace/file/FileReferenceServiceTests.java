package com.simon.workspace.file;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileReferenceServiceTests {

    @Test
    void syncReferencesRestoresActiveFilesAndMarksRemovedFilesOrphaned() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(contains("FROM file_reference"), eq(Long.class),
                eq("BLOG"), eq("42"), eq("CONTENT"))).thenReturn(List.of(1L, 2L));
        FileReferenceService service = new FileReferenceService(jdbcTemplate);

        service.syncReferences("BLOG", "42", "CONTENT", List.of(2L, 3L));

        verify(jdbcTemplate).update(contains("UPDATE file_reference"), eq("BLOG"), eq("42"), eq("CONTENT"));
        verify(jdbcTemplate).update(contains("INSERT INTO file_reference"), eq(2L), eq("BLOG"), eq("42"), eq("CONTENT"));
        verify(jdbcTemplate).update(contains("INSERT INTO file_reference"), eq(3L), eq("BLOG"), eq("42"), eq("CONTENT"));
        verify(jdbcTemplate).update(contains("status = 'ACTIVE'"), eq(2L));
        verify(jdbcTemplate).update(contains("status = 'ACTIVE'"), eq(3L));
        verify(jdbcTemplate).update(contains("status = 'ORPHANED'"), eq(1L));
    }
}
