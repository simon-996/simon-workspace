package com.simon.workspace.file;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class FileResourceControllerPermissionTests {

    @Test
    void uploadDoesNotRequireFileManagePermission() throws NoSuchMethodException {
        assertThat(FileResourceController.class.getAnnotation(SaCheckPermission.class)).isNull();
        assertThat(FileResourceController.class
                .getMethod("upload", MultipartFile.class, String.class, String.class)
                .getAnnotation(SaCheckPermission.class)).isNull();
    }

    @Test
    void fileCenterOperationsStillRequireFileManagePermission() throws NoSuchMethodException {
        assertThat(FileResourceController.class
                .getMethod("list", String.class)
                .getAnnotation(SaCheckPermission.class).value()).containsExactly("file:manage");
        assertThat(FileResourceController.class
                .getMethod("detail", long.class)
                .getAnnotation(SaCheckPermission.class).value()).containsExactly("file:manage");
        assertThat(FileResourceController.class
                .getMethod("download", long.class)
                .getAnnotation(SaCheckPermission.class).value()).containsExactly("file:manage");
        assertThat(FileResourceController.class
                .getMethod("delete", long.class)
                .getAnnotation(SaCheckPermission.class).value()).containsExactly("file:manage");
    }
}
