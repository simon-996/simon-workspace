package com.simon.workspace.classinfo;

import com.simon.workspace.classinfo.model.ClassInfo;

import java.sql.ResultSet;
import java.sql.SQLException;

final class ClassInfoRowMapper {

    private ClassInfoRowMapper() {
    }

    static ClassInfo map(ResultSet rs) throws SQLException {
        return new ClassInfo(
                rs.getLong("id"),
                rs.getString("class_name"),
                rs.getString("major"),
                rs.getString("grade"),
                (Integer) rs.getObject("student_count"),
                rs.getString("counselor"),
                rs.getString("remark"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }
}
