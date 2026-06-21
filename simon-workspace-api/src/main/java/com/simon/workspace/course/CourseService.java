package com.simon.workspace.course;

import com.simon.workspace.course.dto.CourseRequest;
import com.simon.workspace.course.dto.CourseResponse;
import com.simon.workspace.course.model.Course;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Service
public class CourseService {

    private final JdbcTemplate jdbcTemplate;

    public CourseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public CourseResponse detail(long id) {
        return CourseResponse.from(findRequired(id));
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
                                key_point, difficult_point, assessment_method, syllabus, description, status
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                            assessment_method = ?, syllabus = ?, description = ?, status = ?
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
                id
        );

        if (affected == 0) {
            throw new IllegalArgumentException("课程不存在");
        }

        return detail(id);
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
        } catch (Exception exception) {
            throw new IllegalStateException("绑定课程参数失败", exception);
        }
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
