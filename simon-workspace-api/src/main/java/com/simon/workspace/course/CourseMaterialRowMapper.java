package com.simon.workspace.course;

import com.simon.workspace.course.model.CourseMaterial;

import java.sql.ResultSet;
import java.sql.SQLException;

final class CourseMaterialRowMapper {

    private CourseMaterialRowMapper() {
    }

    static CourseMaterial map(ResultSet rs) throws SQLException {
        return new CourseMaterial(
                rs.getLong("id"),
                rs.getLong("course_id"),
                rs.getString("section"),
                rs.getString("material_type"),
                (Long) rs.getObject("file_id"),
                rs.getString("external_url"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("sort_order"),
                rs.getString("status"),
                getNullableString(rs, "original_filename"),
                getNullableString(rs, "public_url"),
                getNullableString(rs, "content_type"),
                getNullableString(rs, "file_extension"),
                (Long) rs.getObject("file_size"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    private static String getNullableString(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getString(column);
        } catch (SQLException ignored) {
            return null;
        }
    }
}
