package com.simon.workspace.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerInitializationScriptTests {

    private static final Path SCRIPT = Path.of("scripts/init-owner.sql");

    @Test
    void requiresExplicitCredentialsAndUsesTheApplicationPasswordFormat() throws IOException {
        String script = readScript();

        assertThat(script)
                .contains("SET @owner_username = 'CHANGE_ME_USERNAME';")
                .contains("SET @owner_nickname = 'CHANGE_ME_NICKNAME';")
                .contains("SET @owner_email = 'CHANGE_ME_EMAIL';")
                .contains("SET @owner_password = 'CHANGE_ME_PASSWORD';")
                .contains("CONCAT('sha256:', SHA2(@owner_password, 256))");
    }

    @Test
    void createsOnlyANewEnabledUserAndBindsTheOwnerRole() throws IOException {
        String script = readScript();

        assertThat(script)
                .contains("START TRANSACTION;")
                .contains("INSERT INTO `user`")
                .contains("'ENABLED'")
                .contains("INSERT INTO `user_role`")
                .contains("role_code = 'OWNER'")
                .contains("USERNAME_EXISTS")
                .contains("OWNER_CREATED")
                .contains("COMMIT;")
                .doesNotContain("UPDATE `user`");
    }

    private String readScript() throws IOException {
        assertThat(SCRIPT).as("owner initialization script should exist").exists();
        return Files.readString(SCRIPT);
    }
}
