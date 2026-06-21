package com.simon.workspace.file;

import com.simon.workspace.file.model.FileResource;

import java.sql.ResultSet;
import java.sql.SQLException;

final class FileResourceRowMapper {

    private FileResourceRowMapper() {
    }

    static FileResource map(ResultSet rs) throws SQLException {
        return new FileResource(
                rs.getLong("id"),
                rs.getLong("owner_user_id"),
                rs.getString("source_type"),
                rs.getString("original_filename"),
                rs.getString("storage_path"),
                rs.getLong("file_size"),
                rs.getString("content_type"),
                rs.getString("file_extension"),
                rs.getString("status"),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }
}
