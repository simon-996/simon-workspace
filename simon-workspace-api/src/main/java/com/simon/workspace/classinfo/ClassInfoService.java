package com.simon.workspace.classinfo;

import com.simon.workspace.classinfo.dto.ClassInfoRequest;
import com.simon.workspace.classinfo.dto.ClassInfoResponse;
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
public class ClassInfoService {

    private final JdbcTemplate jdbcTemplate;

    public ClassInfoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ClassInfoResponse> list(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM class_info
                            WHERE deleted = 0
                              AND (class_name LIKE ? OR major LIKE ? OR grade LIKE ?)
                            ORDER BY updated_time DESC, id DESC
                            """,
                    (rs, rowNum) -> ClassInfoResponse.from(ClassInfoRowMapper.map(rs)),
                    like,
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM class_info
                        WHERE deleted = 0
                        ORDER BY updated_time DESC, id DESC
                        """,
                (rs, rowNum) -> ClassInfoResponse.from(ClassInfoRowMapper.map(rs))
        );
    }

    @Transactional
    public ClassInfoResponse create(ClassInfoRequest request) {
        validateUnique(null, request.className());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO class_info (class_name, major, grade, student_count, counselor, remark)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            bindClassInfo(statement, request);
            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return findRequired(id);
    }

    @Transactional
    public ClassInfoResponse update(long id, ClassInfoRequest request) {
        findRequired(id);
        validateUnique(id, request.className());

        int affected = jdbcTemplate.update("""
                        UPDATE class_info
                        SET class_name = ?, major = ?, grade = ?, student_count = ?, counselor = ?, remark = ?
                        WHERE id = ? AND deleted = 0
                        """,
                request.className().trim(),
                blankToNull(request.major()),
                blankToNull(request.grade()),
                request.studentCount(),
                blankToNull(request.counselor()),
                blankToNull(request.remark()),
                id
        );

        if (affected == 0) {
            throw new IllegalArgumentException("班级不存在");
        }

        return findRequired(id);
    }

    @Transactional
    public void delete(long id) {
        int affected = jdbcTemplate.update("UPDATE class_info SET deleted = 1 WHERE id = ? AND deleted = 0", id);
        if (affected == 0) {
            throw new IllegalArgumentException("班级不存在");
        }
    }

    private ClassInfoResponse findRequired(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM class_info
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> ClassInfoResponse.from(ClassInfoRowMapper.map(rs)),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("班级不存在"));
    }

    private void validateUnique(Long currentId, String className) {
        String normalized = className.trim();
        String sql = "SELECT COUNT(1) FROM class_info WHERE class_name = ? AND deleted = 0"
                + (currentId == null ? "" : " AND id <> ?");
        Long count = currentId == null
                ? jdbcTemplate.queryForObject(sql, Long.class, normalized)
                : jdbcTemplate.queryForObject(sql, Long.class, normalized, currentId);

        if (count != null && count > 0) {
            throw new IllegalArgumentException("班级名称已存在");
        }
    }

    private void bindClassInfo(PreparedStatement statement, ClassInfoRequest request) {
        try {
            statement.setString(1, request.className().trim());
            statement.setString(2, blankToNull(request.major()));
            statement.setString(3, blankToNull(request.grade()));
            statement.setObject(4, request.studentCount());
            statement.setString(5, blankToNull(request.counselor()));
            statement.setString(6, blankToNull(request.remark()));
        } catch (Exception exception) {
            throw new IllegalStateException("绑定班级参数失败", exception);
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
