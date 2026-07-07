package bg.tu_sofia.carpooling.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * RFC 7807 Problem Details for HTTP APIs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
        String type,
        String title,
        int status,
        String detail,
        String traceId,
        Instant timestamp,
        String path,
        List<FieldError> fieldErrors
) {

    public record FieldError(
            String field,
            String message,
            Object rejectedValue
    ) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type = "about:blank";
        private String title;
        private int status;
        private String detail;
        private String traceId;
        private Instant timestamp = Instant.now();
        private String path;
        private List<FieldError> fieldErrors;

        public Builder type(String type) { this.type = type; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder status(int status) { this.status = status; return this; }
        public Builder detail(String detail) { this.detail = detail; return this; }
        public Builder traceId(String traceId) { this.traceId = traceId; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder fieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; return this; }

        public ProblemDetail build() {
            return new ProblemDetail(type, title, status, detail, traceId, timestamp, path, fieldErrors);
        }
    }
}
