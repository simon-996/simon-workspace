package com.simon.workspace.semester;

import com.simon.workspace.semester.model.Semester;
import com.simon.workspace.semester.model.SemesterCalendar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

final class SemesterRowMapper {

    private SemesterRowMapper() {
    }

    static Semester mapSemester(ResultSet rs) throws SQLException {
        return new Semester(
                rs.getLong("id"),
                rs.getString("academic_year"),
                rs.getString("semester_name"),
                rs.getObject("start_date", LocalDate.class),
                rs.getObject("end_date", LocalDate.class),
                rs.getInt("total_weeks"),
                (Integer) rs.getObject("exam_week"),
                rs.getString("holiday_json"),
                rs.getString("adjustment_json"),
                rs.getString("remark"),
                rs.getString("status"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    static SemesterCalendar mapCalendar(ResultSet rs) throws SQLException {
        return new SemesterCalendar(
                rs.getLong("id"),
                rs.getLong("semester_id"),
                rs.getInt("week_no"),
                rs.getObject("start_date", LocalDate.class),
                rs.getObject("end_date", LocalDate.class),
                rs.getBoolean("is_exam_week"),
                rs.getBoolean("is_holiday"),
                rs.getString("holiday_note"),
                rs.getString("adjustment_note"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }
}
