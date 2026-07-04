package com.simon.workspace.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class StoragePropertiesTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "app.storage.active-provider=CLOUDFLARE_R2",
                    "app.storage.providers.LOCAL.type=LOCAL",
                    "app.storage.providers.LOCAL.root=./data/files",
                    "app.storage.providers.TENCENT_COS.type=TENCENT_COS",
                    "app.storage.providers.TENCENT_COS.enabled=true",
                    "app.storage.providers.TENCENT_COS.bucket=tencent-bucket",
                    "app.storage.providers.ALIYUN_OSS.type=ALIYUN_OSS",
                    "app.storage.providers.ALIYUN_OSS.enabled=true",
                    "app.storage.providers.ALIYUN_OSS.bucket=aliyun-bucket",
                    "app.storage.providers.CLOUDFLARE_R2.type=CLOUDFLARE_R2",
                    "app.storage.providers.CLOUDFLARE_R2.enabled=true",
                    "app.storage.providers.CLOUDFLARE_R2.account-id=account-id",
                    "app.storage.providers.CLOUDFLARE_R2.bucket=r2-bucket"
            );

    @Test
    void bindsConfiguredStorageProviders() {
        contextRunner.run(context -> {
            StorageProperties properties = context.getBean(StorageProperties.class);

            assertThat(properties.activeProvider()).isEqualTo("CLOUDFLARE_R2");
            assertThat(properties.providers()).containsKeys("LOCAL", "TENCENT_COS", "ALIYUN_OSS", "CLOUDFLARE_R2");
            assertThat(properties.providers().get("TENCENT_COS").bucket()).isEqualTo("tencent-bucket");
            assertThat(properties.providers().get("ALIYUN_OSS").bucket()).isEqualTo("aliyun-bucket");
            assertThat(properties.providers().get("CLOUDFLARE_R2").endpoint())
                    .isEqualTo("https://account-id.r2.cloudflarestorage.com");
        });
    }

    @Configuration
    @EnableConfigurationProperties(StorageProperties.class)
    static class TestConfiguration {
    }
}
