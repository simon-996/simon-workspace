package com.simon.workspace.site;

import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.site.dto.SiteConfigResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/site")
public class PublicSiteController {

    private final SiteConfigService siteConfigService;

    public PublicSiteController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping
    public ApiResponse<SiteConfigResponse> config() {
        return ApiResponse.ok(siteConfigService.getPublicConfig());
    }
}
