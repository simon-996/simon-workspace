package com.simon.workspace.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class InitialSchemaMigrationTests {

    @Test
    void initialMigrationCreatesFoundationTables() throws IOException {
        String migration = readMigration("db/migration/V1__init_schema.sql");

        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `user`");
        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `role`");
        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `permission`");
        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `user_role`");
        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `role_permission`");
        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `login_log`");
    }

    @Test
    void storageMigrationTracksProvidersAndFileReferences() throws IOException {
        String migration = readMigration("db/migration/V11__storage_providers_and_file_references.sql");

        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `storage_provider_state`");
        assertThat(migration).contains("CREATE TABLE IF NOT EXISTS `file_reference`");
        assertThat(migration).contains("ADD COLUMN `storage_provider`");
        assertThat(migration).contains("ADD COLUMN `object_key`");
        assertThat(migration).contains("ADD COLUMN `visibility`");
        assertThat(migration).contains("ADD COLUMN `orphaned_time`");
        assertThat(migration).contains("'LOCAL'");
        assertThat(migration).contains("'TENCENT_COS'");
        assertThat(migration).contains("'ALIYUN_OSS'");
        assertThat(migration).contains("'CLOUDFLARE_R2'");
    }

    private String readMigration(String path) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(inputStream)
                    .as("migration file %s should exist", path)
                    .isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
