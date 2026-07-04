package com.simon.workspace.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

final class LocalWorkspaceStorageProvider implements WorkspaceStorageProvider {

    private final String code;
    private final StorageProperties.Provider properties;
    private final Path root;

    LocalWorkspaceStorageProvider(String code, StorageProperties.Provider properties) {
        this.code = code;
        this.properties = properties;
        this.root = Paths.get(StringUtils.hasText(properties.root()) ? properties.root() : "./data/files")
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public StorageProviderType type() {
        return StorageProviderType.LOCAL;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public boolean configured() {
        return true;
    }

    @Override
    public StoredObject store(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            StorageVisibility visibility
    ) {
        LocalDate today = LocalDate.now();
        Path targetDirectory = root
                .resolve("files")
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .normalize();

        if (!targetDirectory.startsWith(root)) {
            throw new IllegalStateException("文件存储目录不合法");
        }

        String objectKey = "files/%d/%02d/%s%s".formatted(
                today.getYear(),
                today.getMonthValue(),
                UUID.randomUUID(),
                extensionWithDotOf(originalFilename)
        );
        Path targetPath = root.resolve(objectKey).normalize();
        if (!targetPath.startsWith(targetDirectory)) {
            throw new IllegalStateException("文件存储路径不合法");
        }

        try {
            Files.createDirectories(targetDirectory);
            long fileSize = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return new StoredObject(code, type(), objectKey, objectKey, fileSize, publicUrl(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("保存文件失败", exception);
        }
    }

    @Override
    public StorageObjectDownload download(String objectKey) {
        Path path = resolveObjectKey(objectKey);
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("文件不存在或已被移除");
        }

        try {
            Resource resource = new UrlResource(path.toUri());
            return new StorageObjectDownload(resource, Files.size(path));
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("文件路径不合法", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("读取文件失败", exception);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolveObjectKey(objectKey));
        } catch (IOException exception) {
            throw new IllegalStateException("删除文件失败", exception);
        }
    }

    @Override
    public StorageConnectionTestResult testConnection() {
        try {
            Files.createDirectories(root);
            return StorageConnectionTestResult.ok("本地存储目录可用：" + root);
        } catch (IOException exception) {
            return StorageConnectionTestResult.fail("本地存储目录不可用：" + exception.getMessage());
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

    private Path resolveObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("文件 Key 不能为空");
        }
        Path path = root.resolve(objectKey).normalize();
        if (!path.startsWith(root)) {
            throw new IllegalStateException("文件路径不合法");
        }
        return path;
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
}
