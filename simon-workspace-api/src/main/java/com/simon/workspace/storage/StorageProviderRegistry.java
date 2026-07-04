package com.simon.workspace.storage;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class StorageProviderRegistry {

    private static final String LOCAL = "LOCAL";

    private final StorageProperties properties;
    private final Map<String, WorkspaceStorageProvider> providers;

    public StorageProviderRegistry(StorageProperties properties) {
        this.properties = properties;
        this.providers = buildProviders(properties);
    }

    public List<WorkspaceStorageProvider> providers() {
        return List.copyOf(providers.values());
    }

    public WorkspaceStorageProvider provider(String code) {
        WorkspaceStorageProvider provider = providers.get(normalizeCode(code));
        if (provider == null) {
            throw new IllegalArgumentException("未知存储：" + code);
        }
        return provider;
    }

    public StorageProperties.Provider providerConfig(String code) {
        return properties.provider(normalizeCode(code));
    }

    public WorkspaceStorageProvider fallbackActiveProvider() {
        String activeCode = normalizeCode(properties.activeProvider());
        WorkspaceStorageProvider active = providers.get(activeCode);
        if (active != null && active.enabled() && active.configured()) {
            return active;
        }
        return provider(LOCAL);
    }

    public boolean exists(String code) {
        return providers.containsKey(normalizeCode(code));
    }

    private Map<String, WorkspaceStorageProvider> buildProviders(StorageProperties properties) {
        Map<String, WorkspaceStorageProvider> result = new LinkedHashMap<>();
        Map<String, StorageProperties.Provider> configured = properties.providers();

        StorageProperties.Provider local = configured.getOrDefault(LOCAL,
                new StorageProperties.Provider(StorageProviderType.LOCAL, true, "./data/files",
                        null, null, null, null, null, null, null, null, null, null));
        result.put(LOCAL, new LocalWorkspaceStorageProvider(LOCAL, local));

        configured.forEach((code, provider) -> {
            String normalizedCode = normalizeCode(code);
            if (LOCAL.equals(normalizedCode)) {
                return;
            }
            result.put(normalizedCode, switch (provider.type()) {
                case LOCAL -> new LocalWorkspaceStorageProvider(normalizedCode, provider);
                case TENCENT_COS, ALIYUN_OSS, CLOUDFLARE_R2 ->
                        new S3CompatibleWorkspaceStorageProvider(normalizedCode, provider);
            });
        });

        return result;
    }

    private String normalizeCode(String code) {
        return code == null ? LOCAL : code.trim().toUpperCase(Locale.ROOT);
    }
}
