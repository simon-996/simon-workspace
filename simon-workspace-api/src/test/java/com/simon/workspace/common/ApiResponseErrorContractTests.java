package com.simon.workspace.common;

import com.simon.workspace.common.error.ErrorCode;
import com.simon.workspace.common.error.FieldErrorItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseErrorContractTests {

    @Test
    void exposesStableErrorFieldsForFrontendTranslation() {
        ApiResponse<Void> response = ApiResponse.fail(
                ErrorCode.AUTH_UNAUTHORIZED,
                "trace-123",
                Map.of("permission", "course:manage"),
                List.of()
        );

        assertThat(response.code()).isEqualTo(40101);
        assertThat(response.errorCode()).isEqualTo("AUTH_UNAUTHORIZED");
        assertThat(response.message()).isEqualTo("Unauthorized");
        assertThat(response.traceId()).isEqualTo("trace-123");
        assertThat(response.params()).containsEntry("permission", "course:manage");
        assertThat(response.fieldErrors()).isEmpty();
        assertThat(response.data()).isNull();
    }

    @Test
    void exposesFieldErrorsForFormLevelRendering() {
        FieldErrorItem fieldError = new FieldErrorItem(
                "courseName",
                "VALIDATION_FIELD_INVALID",
                "Invalid field",
                Map.of("field", "courseName")
        );

        ApiResponse<Void> response = ApiResponse.fail(
                ErrorCode.VALIDATION_FAILED,
                "trace-456",
                Map.of(),
                List.of(fieldError)
        );

        assertThat(response.code()).isEqualTo(40001);
        assertThat(response.errorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.fieldErrors()).containsExactly(fieldError);
    }
}
