package taskplanner.summarizationservice.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_at") Long expiresAt
) {
}
