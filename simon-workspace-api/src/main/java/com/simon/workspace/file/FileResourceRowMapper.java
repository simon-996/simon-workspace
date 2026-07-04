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
                rs.getString("storage_provider"),
                rs.getString("object_key"),
                rs.getString("visibility"),
                rs.getString("public_url"),
                rs.getLong("file_size"),
                rs.getString("content_type"),
                rs.getString("file_extension"),
                rs.getString("status"),
                rs.getTimestamp("orphaned_time") == null ? null : rs.getTimestamp("orphaned_time").toLocalDateTime(),
                rs.getTimestamp("created_time").toLocalDateTime(),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }
}
