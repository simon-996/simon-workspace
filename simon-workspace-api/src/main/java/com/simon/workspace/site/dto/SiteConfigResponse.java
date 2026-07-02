package com.simon.workspace.site.dto;

import java.time.LocalDateTime;

public record SiteConfigResponse(
        String id,
        String siteTitle,
        String ownerName,
        String heroTitle,
        String heroSubtitle,
        String ownerRole,
        String contactEmail,
        String githubUrl,
        boolean profileVisible,
        boolean blogVisible,
        boolean projectsVisible,
        boolean workspaceEntryVisible,
        LocalDateTime updatedTime
) {
}
