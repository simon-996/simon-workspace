package com.simon.workspace.generation;

import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.generation.dto.GenerationTaskResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class GenerationTaskService {

    private final JdbcTemplate jdbcTemplate;

    public GenerationTaskService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<GenerationTaskResponse> list(String keyword) {
        long ownerUserId = AuthContextHolder.requireUser().id();
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            return jdbcTemplate.query("""
                            SELECT *
                            FROM generate_task
                            WHERE owner_user_id = ? AND deleted = 0
                              AND (task_name LIKE ? OR task_type LIKE ? OR status LIKE ?)
                            ORDER BY created_time DESC, id DESC
                            """,
                    (rs, rowNum) -> GenerationTaskResponse.from(GenerationTaskRowMapper.map(rs)),
                    ownerUserId,
                    like,
                    like,
                    like
            );
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM generate_task
                        WHERE owner_user_id = ? AND deleted = 0
                        ORDER BY created_time DESC, id DESC
                        """,
                (rs, rowNum) -> GenerationTaskResponse.from(GenerationTaskRowMapper.map(rs)),
                ownerUserId
        );
    }

    public GenerationTaskResponse detail(long id) {
        long ownerUserId = AuthContextHolder.requireUser().id();
        return jdbcTemplate.query("""
                        SELECT *
                        FROM generate_task
                        WHERE id = ? AND owner_user_id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> GenerationTaskResponse.from(GenerationTaskRowMapper.map(rs)),
                id,
                ownerUserId
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("生成任务不存在或无权访问"));
    }
}
