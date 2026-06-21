package com.simon.workspace.course;

import com.simon.workspace.course.model.Course;

import java.sql.ResultSet;
import java.sql.SQLException;

final class CourseRowMapper {

    private CourseRowMapper() {
    }

    static Course map(ResultSet rs) throws SQLException {
        return new Course(
                rs.getLong("id"),
                rs.getString("course_name"),
                rs.getString("course_code"),
                rs.getString("major"),
                rs.getString("grade"),
                rs.getInt("total_hours"),
                (Integer) rs.getObject("theory_hours"),
                (Integer) rs.getObject("experiment_hours"),
                (Integer) rs.getObject("weekly_hours"),
                rs.getBigDecimal("credit"),
                rs.getString("textbook"),
                rs.getString("course_goal"),
                rs.getString("key_point"),
                rs.getString("difficult_point"),
                rs.getString("assessment_method"),
                rs.getString("syllabus"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }
}
