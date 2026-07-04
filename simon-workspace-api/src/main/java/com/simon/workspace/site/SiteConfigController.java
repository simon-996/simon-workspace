package com.simon.workspace.site;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.site.dto.SiteConfigRequest;
import com.simon.workspace.site.dto.SiteConfigResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/site/config")
@SaCheckPermission("site:config")
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    public SiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping
    public ApiResponse<SiteConfigResponse> detail() {
        return ApiResponse.ok(siteConfigService.getPublicConfig());
    }

    @PutMapping
    public ApiResponse<SiteConfigResponse> update(@Valid @RequestBody SiteConfigRequest request) {
        return ApiResponse.ok(siteConfigService.update(request));
    }
}
