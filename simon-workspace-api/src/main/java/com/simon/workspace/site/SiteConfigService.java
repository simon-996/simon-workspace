package com.simon.workspace.site;

import com.simon.workspace.site.dto.SiteConfigRequest;
import com.simon.workspace.site.dto.SiteConfigResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class SiteConfigService {

    private static final long DEFAULT_CONFIG_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public SiteConfigService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SiteConfigResponse getPublicConfig() {
        return findConfig();
    }

    @Transactional
    public SiteConfigResponse update(SiteConfigRequest request) {
        String siteTitle = required(request.siteTitle(), "站点标题不能为空");
        String ownerName = required(request.ownerName(), "所有者名称不能为空");
        String heroTitle = required(request.heroTitle(), "首页标题不能为空");

        jdbcTemplate.update("""
                        UPDATE site_config
                        SET site_title = ?,
                            owner_name = ?,
                            hero_title = ?,
                            hero_subtitle = ?,
                            owner_role = ?,
                            contact_email = ?,
                            github_url = ?,
                            profile_visible = ?,
                            blog_visible = ?,
                            projects_visible = ?,
                            workspace_entry_visible = ?
                        WHERE id = ? AND deleted = 0
                        """,
                siteTitle,
                ownerName,
                heroTitle,
                blankToNull(request.heroSubtitle()),
                blankToNull(request.ownerRole()),
                blankToNull(request.contactEmail()),
                blankToNull(request.githubUrl()),
                bool(request.profileVisible(), true),
                bool(request.blogVisible(), true),
                bool(request.projectsVisible(), true),
                bool(request.workspaceEntryVisible(), false),
                DEFAULT_CONFIG_ID
        );

        return findConfig();
    }

    private SiteConfigResponse findConfig() {
        return jdbcTemplate.query("""
                        SELECT id, site_title, owner_name, hero_title, hero_subtitle, owner_role,
                               contact_email, github_url, profile_visible, blog_visible, projects_visible,
                               workspace_entry_visible, updated_time
                        FROM site_config
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> mapConfig(rs),
                DEFAULT_CONFIG_ID
        ).stream().findFirst().orElseThrow(() -> new IllegalStateException("站点配置不存在"));
    }

    private SiteConfigResponse mapConfig(ResultSet rs) throws SQLException {
        return new SiteConfigResponse(
                String.valueOf(rs.getLong("id")),
                rs.getString("site_title"),
                rs.getString("owner_name"),
                rs.getString("hero_title"),
                rs.getString("hero_subtitle"),
                rs.getString("owner_role"),
                rs.getString("contact_email"),
                rs.getString("github_url"),
                rs.getBoolean("profile_visible"),
                rs.getBoolean("blog_visible"),
                rs.getBoolean("projects_visible"),
                rs.getBoolean("workspace_entry_visible"),
                rs.getTimestamp("updated_time").toLocalDateTime()
        );
    }

    private String required(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private boolean bool(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }
}
