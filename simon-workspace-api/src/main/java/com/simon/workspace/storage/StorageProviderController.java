package com.simon.workspace.storage;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.simon.workspace.common.ApiResponse;
import com.simon.workspace.storage.dto.StorageProviderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/storage/providers")
@SaCheckPermission("file:manage")
public class StorageProviderController {

    private final StorageProviderStateService storageProviderStateService;

    public StorageProviderController(StorageProviderStateService storageProviderStateService) {
        this.storageProviderStateService = storageProviderStateService;
    }

    @GetMapping
    public ApiResponse<List<StorageProviderResponse>> list() {
        return ApiResponse.ok(storageProviderStateService.listProviders());
    }

    @PutMapping("/{code}/activate")
    public ApiResponse<StorageProviderResponse> activate(@PathVariable String code) {
        return ApiResponse.ok(storageProviderStateService.activate(code));
    }

    @PostMapping("/{code}/test")
    public ApiResponse<StorageProviderResponse> test(@PathVariable String code) {
        return ApiResponse.ok(storageProviderStateService.testConnection(code));
    }
}
