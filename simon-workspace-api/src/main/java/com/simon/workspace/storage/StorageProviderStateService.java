package com.simon.workspace.storage;

import com.simon.workspace.storage.dto.StorageProviderResponse;
import com.simon.workspace.storage.model.StorageProviderState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StorageProviderStateService {

    private final JdbcTemplate jdbcTemplate;
    private final StorageProviderRegistry registry;

    public StorageProviderStateService(JdbcTemplate jdbcTemplate, StorageProviderRegistry registry) {
        this.jdbcTemplate = jdbcTemplate;
        this.registry = registry;
    }

    public List<StorageProviderResponse> listProviders() {
        Map<String, RowState> rows = providerRows();
        String activeCode = activeProvider().code();
        return registry.providers().stream()
                .map(provider -> toState(provider, rows.get(provider.code()), provider.code().equals(activeCode)))
                .map(StorageProviderResponse::from)
                .toList();
    }

    public WorkspaceStorageProvider activeProvider() {
        return providerRows().values().stream()
                .filter(RowState::active)
                .map(row -> registry.exists(row.providerCode()) ? registry.provider(row.providerCode()) : null)
                .filter(provider -> provider != null && provider.enabled() && provider.configured())
                .findFirst()
                .orElseGet(registry::fallbackActiveProvider);
    }

    @Transactional
    public StorageProviderResponse activate(String providerCode) {
        WorkspaceStorageProvider provider = registry.provider(normalizeCode(providerCode));
        if (!provider.enabled() || !provider.configured()) {
            throw new IllegalArgumentException("存储未启用或配置不完整，不能切换");
        }
        jdbcTemplate.update("UPDATE storage_provider_state SET active = 0 WHERE deleted = 0");
        ensureRow(provider);
        jdbcTemplate.update("""
                        UPDATE storage_provider_state
                        SET enabled = 1, active = 1
                        WHERE provider_code = ? AND deleted = 0
                        """,
                provider.code()
        );
        return listProviders().stream()
                .filter(item -> item.providerCode().equals(provider.code()))
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public StorageProviderResponse testConnection(String providerCode) {
        WorkspaceStorageProvider provider = registry.provider(normalizeCode(providerCode));
        ensureRow(provider);
        StorageConnectionTestResult result = provider.testConnection();
        jdbcTemplate.update("""
                        UPDATE storage_provider_state
                        SET last_test_status = ?, last_test_message = ?, last_test_time = CURRENT_TIMESTAMP
                        WHERE provider_code = ? AND deleted = 0
                        """,
                result.success() ? "SUCCESS" : "FAILED",
                trimMessage(result.message()),
                provider.code()
        );
        return listProviders().stream()
                .filter(item -> item.providerCode().equals(provider.code()))
                .findFirst()
                .orElseThrow();
    }

    private Map<String, RowState> providerRows() {
        List<RowState> rows = jdbcTemplate.query("""
                        SELECT provider_code, provider_type, display_name, enabled, active,
                               last_test_status, last_test_message, last_test_time
                        FROM storage_provider_state
                        WHERE deleted = 0
                        ORDER BY id ASC
                        """,
                (rs, rowNum) -> mapRow(rs)
        );
        Map<String, RowState> result = new LinkedHashMap<>();
        rows.forEach(row -> result.put(normalizeCode(row.providerCode()), row));
        return result;
    }

    private StorageProviderState toState(WorkspaceStorageProvider provider, RowState row, boolean active) {
        return new StorageProviderState(
                provider.code(),
                provider.type(),
                row == null ? displayName(provider.type()) : row.displayName(),
                provider.configured(),
                provider.enabled(),
                active,
                endpointOf(provider),
                bucketOf(provider),
                publicBaseUrlOf(provider),
                row == null ? null : row.lastTestStatus(),
                row == null ? null : row.lastTestMessage(),
                row == null ? null : row.lastTestTime()
        );
    }

    private void ensureRow(WorkspaceStorageProvider provider) {
        jdbcTemplate.update("""
                        INSERT INTO storage_provider_state (provider_code, provider_type, display_name, enabled, active)
                        VALUES (?, ?, ?, ?, 0)
                        ON DUPLICATE KEY UPDATE
                            provider_type = VALUES(provider_type),
                            display_name = VALUES(display_name),
                            enabled = VALUES(enabled),
                            deleted = 0
                        """,
                provider.code(),
                provider.type().name(),
                displayName(provider.type()),
                provider.enabled()
        );
    }

    private RowState mapRow(ResultSet rs) throws SQLException {
        return new RowState(
                rs.getString("provider_code"),
                StorageProviderType.valueOf(rs.getString("provider_type")),
                rs.getString("display_name"),
                rs.getBoolean("enabled"),
                rs.getBoolean("active"),
                rs.getString("last_test_status"),
                rs.getString("last_test_message"),
                rs.getTimestamp("last_test_time") == null ? null : rs.getTimestamp("last_test_time").toLocalDateTime()
        );
    }

    private String endpointOf(WorkspaceStorageProvider provider) {
        StorageProperties.Provider config = providerConfig(provider.code());
        return config == null ? null : config.endpoint();
    }

    private String bucketOf(WorkspaceStorageProvider provider) {
        StorageProperties.Provider config = providerConfig(provider.code());
        return config == null ? null : config.bucket();
    }

    private String publicBaseUrlOf(WorkspaceStorageProvider provider) {
        StorageProperties.Provider config = providerConfig(provider.code());
        return config == null ? null : config.publicBaseUrl();
    }

    private StorageProperties.Provider providerConfig(String code) {
        return registry.providerConfig(code);
    }

    private String displayName(StorageProviderType type) {
        return switch (type) {
            case LOCAL -> "本地存储";
            case TENCENT_COS -> "腾讯云 COS";
            case ALIYUN_OSS -> "阿里云 OSS";
            case CLOUDFLARE_R2 -> "Cloudflare R2";
        };
    }

    private String normalizeCode(String providerCode) {
        return StringUtils.hasText(providerCode) ? providerCode.trim().toUpperCase(Locale.ROOT) : "LOCAL";
    }

    private String trimMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }
        String trimmed = message.trim();
        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }

    private record RowState(
            String providerCode,
            StorageProviderType providerType,
            String displayName,
            boolean enabled,
            boolean active,
            String lastTestStatus,
            String lastTestMessage,
            LocalDateTime lastTestTime
    ) {
    }
}
