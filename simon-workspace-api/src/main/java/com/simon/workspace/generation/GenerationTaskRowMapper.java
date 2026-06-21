package com.simon.workspace.generation;

import com.simon.workspace.generation.model.GenerationTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

final class GenerationTaskRowMapper {

    private GenerationTaskRowMapper() {
    }

    static GenerationTask map(ResultSet rs) throws SQLException {
        return new GenerationTask(
                rs.getLong("id"),
                rs.getLong("owner_user_id"),
                rs.getString("task_type"),
                rs.getString("task_name"),
                longObject(rs, "course_id"),
                longObject(rs, "class_id"),
                longObject(rs, "semester_id"),
                longObject(rs, "template_id"),
                rs.getString("status"),
                rs.getString("input_json"),
                rs.getString("result_summary"),
                rs.getString("failure_reason"),
                localDateTime(rs, "started_time"),
                localDateTime(rs, "finished_time"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    private static LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static Long longObject(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.longValue();
    }
}
