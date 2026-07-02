package com.simon.workspace.site.dto;

import jakarta.validation.constraints.NotBlank;

public record SiteConfigRequest(
        @NotBlank
        String siteTitle,
        @NotBlank
        String ownerName,
        @NotBlank
        String heroTitle,
        String heroSubtitle,
        String ownerRole,
        String contactEmail,
        String githubUrl,
        Boolean profileVisible,
        Boolean blogVisible,
        Boolean projectsVisible,
        Boolean workspaceEntryVisible
) {
}
