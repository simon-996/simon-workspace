package com.simon.workspace.site.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SiteConfigRequest(
        @NotBlank
        String siteTitle,
        @NotBlank
        String ownerName,
        @NotBlank
        String heroTitle,
        String heroSubtitle,
        String profileBio,
        List<SiteTechStackItem> techStack,
        String ownerRole,
        String contactEmail,
        String githubUrl,
        Boolean profileVisible,
        Boolean blogVisible,
        Boolean courseVisible,
        Boolean projectsVisible,
        Boolean workspaceEntryVisible
) {
}
