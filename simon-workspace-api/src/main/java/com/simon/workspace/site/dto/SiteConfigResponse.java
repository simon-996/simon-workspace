package com.simon.workspace.site.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SiteConfigResponse(
        String id,
        String siteTitle,
        String ownerName,
        String heroTitle,
        String heroSubtitle,
        String ownerRole,
        String profileBio,
        List<SiteTechStackItem> techStack,
        String contactEmail,
        String githubUrl,
        boolean profileVisible,
        boolean blogVisible,
        boolean courseVisible,
        boolean projectsVisible,
        boolean workspaceEntryVisible,
        LocalDateTime updatedTime
) {
}
