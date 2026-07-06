package com.simon.workspace.course;

import com.simon.workspace.course.dto.CourseRequest;
import com.simon.workspace.course.dto.CourseResponse;
import com.simon.workspace.course.dto.CourseMaterialRequest;
import com.simon.workspace.course.dto.CourseMaterialResponse;
import com.simon.workspace.course.dto.PublicCourseDetailResponse;
import com.simon.workspace.course.model.Course;
import com.simon.workspace.file.FileReferenceService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class CourseService {

    private final JdbcTemplate jdbcTemplate;
    private final FileReferenceService fileReferenceService;

    public CourseService(JdbcTemplate jdbcTemplate, FileReferenceService fileReferenceService) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileReferenceService = fileReferenceService;
    }

    public List<CourseResponse> list(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM course
                            WHERE deleted = 0
                              AND (course_name LIKE ? OR course_code LIKE ?)
                            ORDER BY updated_time DESC, id DESC
                            """,
                    (rs, rowNum) -> CourseResponse.from(CourseRowMapper.map(rs)),
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM course
                        WHERE deleted = 0
                        ORDER BY updated_time DESC, id DESC
                        """,
                (rs, rowNum) -> CourseResponse.from(CourseRowMapper.map(rs))
        );
    }

    public List<CourseResponse> publicList() {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM course
                        WHERE deleted = 0 AND status = 'ACTIVE' AND public_visible = 1
                        ORDER BY public_sort_order ASC, updated_time DESC, id DESC
                        """,
                (rs, rowNum) -> CourseResponse.from(CourseRowMapper.map(rs))
        );
    }

    public CourseResponse detail(long id) {
        return CourseResponse.from(findRequired(id));
    }

    public PublicCourseDetailResponse publicDetail(long id) {
        Course course = jdbcTemplate.query("""
                        SELECT *
                        FROM course
                        WHERE id = ? AND deleted = 0 AND status = 'ACTIVE' AND public_visible = 1
                        LIMIT 1
                        """,
                (rs, rowNum) -> CourseRowMapper.map(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("课程不存在"));
        List<CourseMaterialResponse> materials = listPublicMaterials(id);
        return new PublicCourseDetailResponse(
                CourseResponse.from(course),
                filterSection(materials, "DOCUMENT"),
                filterSection(materials, "COURSEWARE"),
                filterSection(materials, "RESOURCE")
        );
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        validateUnique(null, request);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO course (
                                course_name, course_code, major, grade, total_hours, theory_hours,
                                experiment_hours, weekly_hours, credit, textbook, course_goal,
                                key_point, difficult_point, assessment_method, syllabus, description, status,
                                public_visible, public_sort_order
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            bindCourse(statement, request);
            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return detail(id);
    }

    @Transactional
    public CourseResponse update(long id, CourseRequest request) {
        findRequired(id);
        validateUnique(id, request);

        int affected = jdbcTemplate.update("""
                        UPDATE course
                        SET course_name = ?, course_code = ?, major = ?, grade = ?, total_hours = ?,
                            theory_hours = ?, experiment_hours = ?, weekly_hours = ?, credit = ?,
                            textbook = ?, course_goal = ?, key_point = ?, difficult_point = ?,
                            assessment_method = ?, syllabus = ?, description = ?, status = ?,
                            public_visible = ?, public_sort_order = ?
                        WHERE id = ? AND deleted = 0
                        """,
                request.courseName().trim(),
                blankToNull(request.courseCode()),
                blankToNull(request.major()),
                blankToNull(request.grade()),
                request.totalHours(),
                request.theoryHours(),
                request.experimentHours(),
                request.weeklyHours(),
                request.credit(),
                blankToNull(request.textbook()),
                blankToNull(request.courseGoal()),
                blankToNull(request.keyPoint()),
                blankToNull(request.difficultPoint()),
                blankToNull(request.assessmentMethod()),
                blankToNull(request.syllabus()),
                blankToNull(request.description()),
                normalizeStatus(request.status()),
                Boolean.TRUE.equals(request.publicVisible()) ? 1 : 0,
                request.publicSortOrder() == null ? 0 : request.publicSortOrder(),
                id
        );

        if (affected == 0) {
            throw new IllegalArgumentException("课程不存在");
        }

        return detail(id);
    }

    public List<CourseMaterialResponse> listMaterials(long courseId) {
        findRequired(courseId);
        return listMaterialsByCourse(courseId, false);
    }

    @Transactional
    public CourseMaterialResponse createMaterial(long courseId, CourseMaterialRequest request) {
        requireCourse(courseId);
        String section = normalizeValue(request.section(), List.of("DOCUMENT", "COURSEWARE", "RESOURCE"), "目录类型不合法");
        String materialType = normalizeValue(request.materialType(), List.of("FILE", "LINK"), "资料类型不合法");
        String title = required(request.title(), "资料标题不能为空");
        Long fileId = "FILE".equals(materialType) ? requirePublicFile(request.fileId()) : null;
        String externalUrl = "LINK".equals(materialType) ? required(request.externalUrl(), "外部链接不能为空") : null;
        String status = normalizeValue(request.status(), List.of("ACTIVE", "DISABLED"), "资料状态不合法");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO course_material (
                                course_id, section, material_type, file_id, external_url,
                                title, description, sort_order, status
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            bindMaterial(statement, courseId, section, materialType, fileId, externalUrl, title, request.description(),
                    request.sortOrder(), status);
            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        syncMaterialFile(id, fileId);
        return materialDetail(courseId, id);
    }

    @Transactional
    public CourseMaterialResponse updateMaterial(long courseId, long materialId, CourseMaterialRequest request) {
        requireCourse(courseId);
        String section = normalizeValue(request.section(), List.of("DOCUMENT", "COURSEWARE", "RESOURCE"), "目录类型不合法");
        String materialType = normalizeValue(request.materialType(), List.of("FILE", "LINK"), "资料类型不合法");
        String title = required(request.title(), "资料标题不能为空");
        Long fileId = "FILE".equals(materialType) ? requirePublicFile(request.fileId()) : null;
        String externalUrl = "LINK".equals(materialType) ? required(request.externalUrl(), "外部链接不能为空") : null;
        String status = normalizeValue(request.status(), List.of("ACTIVE", "DISABLED"), "资料状态不合法");
        int affected = jdbcTemplate.update("""
                        UPDATE course_material
                        SET section = ?, material_type = ?, file_id = ?, external_url = ?,
                            title = ?, description = ?, sort_order = ?, status = ?
                        WHERE id = ? AND course_id = ? AND deleted = 0
                        """,
                section,
                materialType,
                fileId,
                externalUrl,
                title,
                blankToNull(request.description()),
                request.sortOrder() == null ? 0 : request.sortOrder(),
                status,
                materialId,
                courseId
        );
        if (affected == 0) {
            throw new IllegalArgumentException("课程资料不存在");
        }
        syncMaterialFile(materialId, fileId);
        return materialDetail(courseId, materialId);
    }

    @Transactional
    public void deleteMaterial(long courseId, long materialId) {
        int affected = jdbcTemplate.update(
                "UPDATE course_material SET deleted = 1 WHERE id = ? AND course_id = ? AND deleted = 0",
                materialId,
                courseId
        );
        if (affected == 0) {
            throw new IllegalArgumentException("课程资料不存在");
        }
        syncMaterialFile(materialId, null);
    }

    @Transactional
    public void delete(long id) {
        int affected = jdbcTemplate.update("UPDATE course SET deleted = 1 WHERE id = ? AND deleted = 0", id);
        if (affected == 0) {
            throw new IllegalArgumentException("课程不存在");
        }
    }

    private Course findRequired(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM course
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> CourseRowMapper.map(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("课程不存在"));
    }

    private void validateUnique(Long currentId, CourseRequest request) {
        String courseName = request.courseName().trim();
        if (existsByField("course_name", courseName, currentId)) {
            throw new IllegalArgumentException("课程名称已存在");
        }

        String courseCode = blankToNull(request.courseCode());
        if (courseCode != null && existsByField("course_code", courseCode, currentId)) {
            throw new IllegalArgumentException("课程编码已存在");
        }
    }

    private boolean existsByField(String field, String value, Long currentId) {
        String sql = "SELECT COUNT(1) FROM course WHERE " + field + " = ? AND deleted = 0"
                + (currentId == null ? "" : " AND id <> ?");
        Long count = currentId == null
                ? jdbcTemplate.queryForObject(sql, Long.class, value)
                : jdbcTemplate.queryForObject(sql, Long.class, value, currentId);
        return count != null && count > 0;
    }

    private void bindCourse(PreparedStatement statement, CourseRequest request) {
        try {
            statement.setString(1, request.courseName().trim());
            statement.setString(2, blankToNull(request.courseCode()));
            statement.setString(3, blankToNull(request.major()));
            statement.setString(4, blankToNull(request.grade()));
            statement.setInt(5, request.totalHours());
            statement.setObject(6, request.theoryHours());
            statement.setObject(7, request.experimentHours());
            statement.setObject(8, request.weeklyHours());
            statement.setObject(9, request.credit());
            statement.setString(10, blankToNull(request.textbook()));
            statement.setString(11, blankToNull(request.courseGoal()));
            statement.setString(12, blankToNull(request.keyPoint()));
            statement.setString(13, blankToNull(request.difficultPoint()));
            statement.setString(14, blankToNull(request.assessmentMethod()));
                statement.setString(15, blankToNull(request.syllabus()));
                statement.setString(16, blankToNull(request.description()));
                statement.setString(17, normalizeStatus(request.status()));
                statement.setInt(18, Boolean.TRUE.equals(request.publicVisible()) ? 1 : 0);
                statement.setInt(19, request.publicSortOrder() == null ? 0 : request.publicSortOrder());
        } catch (Exception exception) {
            throw new IllegalStateException("绑定课程参数失败", exception);
        }
    }

    private List<CourseMaterialResponse> listPublicMaterials(long courseId) {
        return listMaterialsByCourse(courseId, true);
    }

    private List<CourseMaterialResponse> listMaterialsByCourse(long courseId, boolean publicOnly) {
        String statusClause = publicOnly ? "AND m.status = 'ACTIVE' " : "";
        return jdbcTemplate.query("""
                        SELECT m.*, f.original_filename, f.public_url, f.content_type, f.file_extension, f.file_size
                        FROM course_material m
                        LEFT JOIN file_resource f ON f.id = m.file_id AND f.deleted = 0
                        WHERE m.course_id = ? AND m.deleted = 0
                        %s
                        ORDER BY m.section ASC, m.sort_order ASC, m.id ASC
                        """.formatted(statusClause),
                (rs, rowNum) -> CourseMaterialResponse.from(CourseMaterialRowMapper.map(rs)),
                courseId
        );
    }

    private CourseMaterialResponse materialDetail(long courseId, long materialId) {
        return jdbcTemplate.query("""
                        SELECT m.*, f.original_filename, f.public_url, f.content_type, f.file_extension, f.file_size
                        FROM course_material m
                        LEFT JOIN file_resource f ON f.id = m.file_id AND f.deleted = 0
                        WHERE m.id = ? AND m.course_id = ? AND m.deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> CourseMaterialResponse.from(CourseMaterialRowMapper.map(rs)),
                materialId,
                courseId
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("课程资料不存在"));
    }

    private List<CourseMaterialResponse> filterSection(List<CourseMaterialResponse> materials, String section) {
        return materials.stream().filter(item -> section.equals(item.section())).toList();
    }

    private void requireCourse(long courseId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course WHERE id = ? AND deleted = 0",
                Integer.class,
                courseId
        );
        if (count == null || count == 0) {
            throw new IllegalArgumentException("课程不存在");
        }
    }

    private Long requirePublicFile(Long fileId) {
        if (fileId == null || fileId <= 0) {
            throw new IllegalArgumentException("文件不能为空");
        }
        Integer count = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM file_resource
                        WHERE id = ? AND visibility = 'PUBLIC' AND status = 'ACTIVE' AND deleted = 0
                        """,
                Integer.class,
                fileId
        );
        if (count == null || count == 0) {
            throw new IllegalArgumentException("文件不存在或不是公开文件");
        }
        return fileId;
    }

    private void syncMaterialFile(long materialId, Long fileId) {
        fileReferenceService.syncReferences(
                "COURSE_MATERIAL",
                String.valueOf(materialId),
                "MATERIAL_FILE",
                fileId == null ? Collections.emptyList() : List.of(fileId)
        );
    }

    private void bindMaterial(
            PreparedStatement statement,
            long courseId,
            String section,
            String materialType,
            Long fileId,
            String externalUrl,
            String title,
            String description,
            Integer sortOrder,
            String status
    ) {
        try {
            statement.setLong(1, courseId);
            statement.setString(2, section);
            statement.setString(3, materialType);
            statement.setObject(4, fileId);
            statement.setString(5, externalUrl);
            statement.setString(6, title);
            statement.setString(7, blankToNull(description));
            statement.setInt(8, sortOrder == null ? 0 : sortOrder);
            statement.setString(9, status);
        } catch (Exception exception) {
            throw new IllegalStateException("绑定课程资料参数失败", exception);
        }
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeValue(String value, List<String> allowed, String message) {
        String normalized = StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : allowed.get(0);
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "ACTIVE";
        }

        String normalized = status.trim().toUpperCase();
        if (!"ACTIVE".equals(normalized) && !"ARCHIVED".equals(normalized)) {
            throw new IllegalArgumentException("课程状态不合法");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
