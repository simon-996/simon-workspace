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

    private String readMigration(String path) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertThat(inputStream)
                    .as("migration file %s should exist", path)
                    .isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
