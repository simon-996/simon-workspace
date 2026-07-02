package com.simon.workspace.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"prod", "test"})
@TestPropertySource(properties = {
        "MYSQL_URL=jdbc:mysql://db.example.com:3306/simon_workspace?useSSL=true",
        "MYSQL_USERNAME=simon",
        "MYSQL_PASSWORD=secret",
        "APP_CORS_ALLOWED_ORIGINS=https://www.simon996.com"
})
class DeploymentConfigurationTests {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void datasourceUrlCanUsePublicJdbcUrlOverride() {
        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:mysql://db.example.com:3306/simon_workspace?useSSL=true");
    }

    @Test
    void corsAllowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header("Origin", "https://www.simon996.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://www.simon996.com"));
    }
}
