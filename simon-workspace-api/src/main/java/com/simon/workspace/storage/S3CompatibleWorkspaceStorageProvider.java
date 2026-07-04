package com.simon.workspace.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

final class S3CompatibleWorkspaceStorageProvider implements WorkspaceStorageProvider {

    private final String code;
    private final StorageProperties.Provider properties;
    private volatile S3Client client;

    S3CompatibleWorkspaceStorageProvider(String code, StorageProperties.Provider properties) {
        this.code = code;
        this.properties = properties;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public StorageProviderType type() {
        return properties.type();
    }

    @Override
    public boolean enabled() {
        return properties.enabled();
    }

    @Override
    public boolean configured() {
        return StringUtils.hasText(properties.endpoint())
                && StringUtils.hasText(properties.bucket())
                && StringUtils.hasText(accessKey())
                && StringUtils.hasText(secretKey());
    }

    @Override
    public StoredObject store(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            StorageVisibility visibility
    ) {
        requireConfigured();
        LocalDate today = LocalDate.now();
        String objectKey = "files/%d/%02d/%s%s".formatted(
                today.getYear(),
                today.getMonthValue(),
                UUID.randomUUID(),
                extensionWithDotOf(originalFilename)
        );

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("simon-workspace-upload-", ".tmp");
            long fileSize = Files.copy(inputStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            PutObjectRequest.Builder request = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey);
            if (StringUtils.hasText(contentType)) {
                request.contentType(contentType.trim());
            }
            client().putObject(request.build(), RequestBody.fromFile(tempFile));
            return new StoredObject(code, type(), objectKey, objectKey, fileSize, publicUrl(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("暂存上传文件失败", exception);
        } catch (S3Exception exception) {
            throw new IllegalStateException("上传到对象存储失败：" + exception.awsErrorDetails().errorMessage(), exception);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Temporary upload cleanup can be retried by the OS temp cleanup.
                }
            }
        }
    }

    @Override
    public StorageObjectDownload download(String objectKey) {
        requireConfigured();
        try {
            ResponseInputStream<GetObjectResponse> response = client().getObject(GetObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(objectKey)
                    .build());
            return new StorageObjectDownload(new InputStreamResource(response), response.response().contentLength());
        } catch (S3Exception exception) {
            throw new IllegalArgumentException("文件不存在或已被移除", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        requireConfigured();
        client().deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build());
    }

    @Override
    public StorageConnectionTestResult testConnection() {
        if (!configured()) {
            return StorageConnectionTestResult.fail("存储配置不完整，请检查 endpoint、bucket 和密钥");
        }

        try {
            client().headBucket(HeadBucketRequest.builder().bucket(properties.bucket()).build());
            return StorageConnectionTestResult.ok("连接成功");
        } catch (Exception exception) {
            return StorageConnectionTestResult.fail("连接失败：" + rootMessage(exception));
        }
    }

    @Override
    public String publicUrl(String objectKey) {
        String baseUrl = properties.publicBaseUrl();
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(objectKey)) {
            return null;
        }
        return baseUrl.replaceAll("/+$", "") + "/" + objectKey.replaceAll("^/+", "");
    }

    private S3Client client() {
        S3Client existing = client;
        if (existing != null) {
            return existing;
        }

        synchronized (this) {
            if (client == null) {
                client = S3Client.builder()
                        .endpointOverride(URI.create(properties.endpoint()))
                        .region(Region.of(region()))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey(), secretKey())
                        ))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                        .build();
            }
            return client;
        }
    }

    private void requireConfigured() {
        if (!enabled()) {
            throw new IllegalStateException("存储未启用：" + code);
        }
        if (!configured()) {
            throw new IllegalStateException("存储配置不完整：" + code);
        }
    }

    private String accessKey() {
        if (StringUtils.hasText(properties.accessKeyId())) {
            return properties.accessKeyId().trim();
        }
        if (StringUtils.hasText(properties.secretId())) {
            return properties.secretId().trim();
        }
        return "";
    }

    private String secretKey() {
        if (StringUtils.hasText(properties.secretAccessKey())) {
            return properties.secretAccessKey().trim();
        }
        if (StringUtils.hasText(properties.accessKeySecret())) {
            return properties.accessKeySecret().trim();
        }
        if (StringUtils.hasText(properties.secretKey())) {
            return properties.secretKey().trim();
        }
        return "";
    }

    private String region() {
        if (StringUtils.hasText(properties.region())) {
            return properties.region().trim();
        }
        if (properties.type() == StorageProviderType.CLOUDFLARE_R2) {
            return "auto";
        }
        return "us-east-1";
    }

    private String extensionWithDotOf(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        String extension = filename.substring(dotIndex);
        return extension.length() > 32 ? "" : extension;
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return StringUtils.hasText(current.getMessage()) ? current.getMessage() : current.getClass().getSimpleName();
    }
}
