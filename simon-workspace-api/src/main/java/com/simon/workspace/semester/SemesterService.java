package com.simon.workspace.semester;

import com.simon.workspace.semester.dto.SemesterCalendarResponse;
import com.simon.workspace.semester.dto.SemesterRequest;
import com.simon.workspace.semester.dto.SemesterResponse;
import com.simon.workspace.semester.model.Semester;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class SemesterService {

    private final JdbcTemplate jdbcTemplate;

    public SemesterService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SemesterResponse> list(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM semester
                            WHERE deleted = 0
                              AND (academic_year LIKE ? OR semester_name LIKE ?)
                            ORDER BY start_date DESC, id DESC
                            """,
                    (rs, rowNum) -> SemesterResponse.from(SemesterRowMapper.mapSemester(rs)),
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM semester
                        WHERE deleted = 0
                        ORDER BY start_date DESC, id DESC
                        """,
                (rs, rowNum) -> SemesterResponse.from(SemesterRowMapper.mapSemester(rs))
        );
    }

    public SemesterResponse detail(long id) {
        return SemesterResponse.from(findRequired(id));
    }

    @Transactional
    public SemesterResponse create(SemesterRequest request) {
        validateRequest(request);
        validateUnique(null, request);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO semester (
                                academic_year, semester_name, start_date, end_date, total_weeks,
                                exam_week, holiday_json, adjustment_json, remark, status
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            bindSemester(statement, request);
            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return detail(id);
    }

    @Transactional
    public SemesterResponse update(long id, SemesterRequest request) {
        findRequired(id);
        validateRequest(request);
        validateUnique(id, request);

        int affected = jdbcTemplate.update("""
                        UPDATE semester
                        SET academic_year = ?, semester_name = ?, start_date = ?, end_date = ?,
                            total_weeks = ?, exam_week = ?, holiday_json = ?, adjustment_json = ?,
                            remark = ?, status = ?
                        WHERE id = ? AND deleted = 0
                        """,
                request.academicYear().trim(),
                request.semesterName().trim(),
                request.startDate(),
                request.endDate(),
                request.totalWeeks(),
                request.examWeek(),
                blankToNull(request.holidayJson()),
                blankToNull(request.adjustmentJson()),
                blankToNull(request.remark()),
                normalizeStatus(request.status()),
                id
        );

        if (affected == 0) {
            throw new IllegalArgumentException("学期不存在");
        }

        return detail(id);
    }

    @Transactional
    public List<SemesterCalendarResponse> generateCalendar(long id) {
        Semester semester = findRequired(id);

        jdbcTemplate.update("UPDATE semester_calendar SET deleted = 1 WHERE semester_id = ? AND deleted = 0", id);

        for (int weekNo = 1; weekNo <= semester.totalWeeks(); weekNo++) {
            LocalDate startDate = semester.startDate().plusWeeks(weekNo - 1L);
            LocalDate endDate = startDate.plusDays(6);
            if (semester.endDate() != null && endDate.isAfter(semester.endDate())) {
                endDate = semester.endDate();
            }

            boolean examWeek = semester.examWeek() != null && semester.examWeek() == weekNo;
            jdbcTemplate.update("""
                            INSERT INTO semester_calendar (
                                semester_id, week_no, start_date, end_date, is_exam_week,
                                is_holiday, holiday_note, adjustment_note
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    semester.id(),
                    weekNo,
                    startDate,
                    endDate,
                    examWeek,
                    false,
                    null,
                    null
            );
        }

        return calendar(id);
    }

    public List<SemesterCalendarResponse> calendar(long id) {
        findRequired(id);
        return jdbcTemplate.query("""
                        SELECT *
                        FROM semester_calendar
                        WHERE semester_id = ? AND deleted = 0
                        ORDER BY week_no ASC
                        """,
                (rs, rowNum) -> SemesterCalendarResponse.from(SemesterRowMapper.mapCalendar(rs)),
                id
        );
    }

    private Semester findRequired(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM semester
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> SemesterRowMapper.mapSemester(rs),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("学期不存在"));
    }

    private void validateRequest(SemesterRequest request) {
        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("结束日期不能早于开学日期");
        }

        if (request.examWeek() != null && (request.examWeek() < 1 || request.examWeek() > request.totalWeeks())) {
            throw new IllegalArgumentException("考试周必须在总周数范围内");
        }
    }

    private void validateUnique(Long currentId, SemesterRequest request) {
        String sql = "SELECT COUNT(1) FROM semester WHERE academic_year = ? AND semester_name = ? AND deleted = 0"
                + (currentId == null ? "" : " AND id <> ?");
        Long count = currentId == null
                ? jdbcTemplate.queryForObject(sql, Long.class, request.academicYear().trim(), request.semesterName().trim())
                : jdbcTemplate.queryForObject(sql, Long.class, request.academicYear().trim(), request.semesterName().trim(), currentId);

        if (count != null && count > 0) {
            throw new IllegalArgumentException("学年和学期名称已存在");
        }
    }

    private void bindSemester(PreparedStatement statement, SemesterRequest request) {
        try {
            statement.setString(1, request.academicYear().trim());
            statement.setString(2, request.semesterName().trim());
            statement.setObject(3, request.startDate());
            statement.setObject(4, request.endDate());
            statement.setInt(5, request.totalWeeks());
            statement.setObject(6, request.examWeek());
            statement.setString(7, blankToNull(request.holidayJson()));
            statement.setString(8, blankToNull(request.adjustmentJson()));
            statement.setString(9, blankToNull(request.remark()));
            statement.setString(10, normalizeStatus(request.status()));
        } catch (Exception exception) {
            throw new IllegalStateException("绑定学期参数失败", exception);
        }
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "PLANNED";
        }

        String normalized = status.trim().toUpperCase();
        if (!"PLANNED".equals(normalized) && !"ACTIVE".equals(normalized) && !"CLOSED".equals(normalized)) {
            throw new IllegalArgumentException("学期状态不合法");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
