package com.simon.workspace.common.error;

import com.simon.workspace.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void mapsBusinessExceptionToStableErrorResponse() throws Exception {
        mockMvc.perform(get("/business-error"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(40901))
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Conflict"))
                .andExpect(jsonPath("$.params.reason").value("lastOwner"))
                .andExpect(jsonPath("$.traceId", not(emptyString())));
    }

    @Test
    void mapsValidationExceptionToFieldErrors() throws Exception {
        mockMvc.perform(post("/validation-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].errorCode").value("VALIDATION_FIELD_INVALID"))
                .andExpect(jsonPath("$.traceId", not(emptyString())));
    }

    @RestController
    static class TestController {

        @GetMapping("/business-error")
        ApiResponse<Void> businessError() {
            throw new BusinessException(ErrorCode.CONFLICT, Map.of("reason", "lastOwner"));
        }

        @PostMapping("/validation-error")
        ApiResponse<Void> validationError(@Valid @RequestBody ValidationRequest request) {
            return ApiResponse.ok(null);
        }
    }

    record ValidationRequest(@NotBlank String name) {
    }
}
