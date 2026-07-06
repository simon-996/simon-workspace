package com.simon.workspace.course;

import com.simon.workspace.course.dto.CourseMaterialRequest;
import com.simon.workspace.course.dto.CourseMaterialResponse;
import com.simon.workspace.file.FileReferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourseMaterialServiceTests {

    @Test
    void createFileMaterialBindsTheUploadedFile() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        FileReferenceService fileReferenceService = mock(FileReferenceService.class);
        CourseService service = new CourseService(jdbcTemplate, fileReferenceService);
        when(jdbcTemplate.queryForObject(contains("FROM course"), eq(Integer.class), eq(3L))).thenReturn(1);
        when(jdbcTemplate.queryForObject(contains("FROM file_resource"), eq(Integer.class), eq(9L))).thenReturn(1);
        when(jdbcTemplate.update(any(org.springframework.jdbc.core.PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    keyHolder.getKeyList().add(Map.of("GENERATED_KEY", 44L));
                    return 1;
                });
        when(jdbcTemplate.query(contains("SELECT m.*"), any(org.springframework.jdbc.core.RowMapper.class), eq(44L), eq(3L)))
                .thenReturn(List.of(new CourseMaterialResponse(
                        "44",
                        "3",
                        "DOCUMENT",
                        "FILE",
                        "9",
                        null,
                        "HTML Notes",
                        "week one",
                        10,
                        "ACTIVE",
                        "lesson.md",
                        null,
                        "text/markdown",
                        "md",
                        12L,
                        null,
                        null
                )));

        service.createMaterial(3L, new CourseMaterialRequest(
                "DOCUMENT",
                "FILE",
                9L,
                null,
                "HTML Notes",
                "week one",
                10,
                "ACTIVE"
        ));

        verify(fileReferenceService).syncReferences("COURSE_MATERIAL", "44", "MATERIAL_FILE", List.of(9L));
    }

    @Test
    void createLinkMaterialRejectsMissingUrl() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        FileReferenceService fileReferenceService = mock(FileReferenceService.class);
        CourseService service = new CourseService(jdbcTemplate, fileReferenceService);
        when(jdbcTemplate.queryForObject(contains("FROM course"), eq(Integer.class), eq(3L))).thenReturn(1);

        assertThatThrownBy(() -> service.createMaterial(3L, new CourseMaterialRequest(
                "RESOURCE",
                "LINK",
                null,
                "",
                "Official Site",
                null,
                0,
                "ACTIVE"
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("外部链接不能为空");
    }

    @Test
    void deleteMaterialClearsFileReference() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        FileReferenceService fileReferenceService = mock(FileReferenceService.class);
        CourseService service = new CourseService(jdbcTemplate, fileReferenceService);
        when(jdbcTemplate.update(contains("UPDATE course_material"), eq(44L), eq(3L))).thenReturn(1);

        service.deleteMaterial(3L, 44L);

        verify(fileReferenceService).syncReferences("COURSE_MATERIAL", "44", "MATERIAL_FILE", List.of());
    }
}
