package com.simon.workspace.template;

import com.simon.workspace.template.model.TemplateField;
import com.simon.workspace.template.model.TemplateFile;

import java.sql.ResultSet;
import java.sql.SQLException;

final class TemplateRowMapper {

    private TemplateRowMapper() {
    }

    static TemplateFile mapTemplate(ResultSet rs) throws SQLException {
        return new TemplateFile(
                rs.getLong("id"),
                rs.getString("template_name"),
                rs.getString("template_type"),
                rs.getString("original_filename"),
                rs.getString("storage_path"),
                rs.getLong("file_size"),
                rs.getString("content_type"),
                rs.getString("description"),
                rs.getString("status"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    static TemplateField mapField(ResultSet rs) throws SQLException {
        return new TemplateField(
                rs.getLong("id"),
                rs.getLong("template_id"),
                rs.getString("field_key"),
                rs.getString("field_label"),
                rs.getString("field_type"),
                rs.getBoolean("required"),
                rs.getString("default_value"),
                rs.getInt("sort_order"),
                rs.getString("remark"),
                rs.getString("status"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }
}
