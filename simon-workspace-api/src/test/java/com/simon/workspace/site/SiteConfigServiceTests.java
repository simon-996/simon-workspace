package com.simon.workspace.site;

import com.simon.workspace.site.dto.SiteConfigRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class SiteConfigServiceTests {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final SiteConfigService service = new SiteConfigService(jdbcTemplate);

    @Test
    void updateRejectsBlankSiteTitle() {
        SiteConfigRequest request = new SiteConfigRequest(
                " ",
                "Simon",
                "AI teaching workspace",
                "Teaching notes and personal projects.",
                "软件教师 / 独立开发者",
                "simon@example.com",
                "https://github.com/simon-996",
                true,
                true,
                true,
                false
        );

        assertThatThrownBy(() -> service.update(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("站点标题不能为空");
    }
}
