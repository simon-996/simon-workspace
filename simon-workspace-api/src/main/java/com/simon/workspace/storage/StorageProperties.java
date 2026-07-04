package com.simon.workspace.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Map;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        String activeProvider,
        Map<String, Provider> providers
) {

    public StorageProperties {
        activeProvider = StringUtils.hasText(activeProvider) ? activeProvider.trim() : "LOCAL";
        providers = providers == null ? Map.of() : Map.copyOf(providers);
    }

    public Provider provider(String providerCode) {
        return providers.get(providerCode);
    }

    public Provider activeProviderConfig() {
        return provider(activeProvider);
    }

    public record Provider(
            StorageProviderType type,
            boolean enabled,
            String root,
            String endpoint,
            String region,
            String bucket,
            String publicBaseUrl,
            String secretId,
            String secretKey,
            String accessKeyId,
            String accessKeySecret,
            String secretAccessKey,
            String accountId
    ) {
        public Provider {
            type = type == null ? StorageProviderType.LOCAL : type;
        }

        @Override
        public String endpoint() {
            if (StringUtils.hasText(endpoint)) {
                return endpoint.trim();
            }
            if (type == StorageProviderType.CLOUDFLARE_R2 && StringUtils.hasText(accountId)) {
                return "https://" + accountId.trim() + ".r2.cloudflarestorage.com";
            }
            if (type == StorageProviderType.TENCENT_COS && StringUtils.hasText(region)) {
                return "https://cos." + region.trim() + ".myqcloud.com";
            }
            if (type == StorageProviderType.ALIYUN_OSS && StringUtils.hasText(region)) {
                return "https://oss-" + region.trim() + ".aliyuncs.com";
            }
            return endpoint;
        }

        public String publicBaseUrl() {
            return StringUtils.hasText(publicBaseUrl) ? publicBaseUrl.trim() : null;
        }
    }
}
