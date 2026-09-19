package taskplanner.summarizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import taskplanner.summarizationservice.response.TokenResponse;

import java.time.Clock;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private static final long TOKEN_EXPIRATION_GAP_MS = 2 * 60 * 1000L;

    private final RestClient restClient;
    private final Clock clock;

    @Value("${auth.key}")
    private String authKey;

    private TokenResponse token;

    public synchronized TokenResponse getValidToken() {
        if (token == null || shouldRefreshToken()) {
            token = requestTokenWithRetry();
        }

        return token;
    }

    private boolean shouldRefreshToken() {
        return clock.millis() >= token.expiresAt() - TOKEN_EXPIRATION_GAP_MS;
    }

   private TokenResponse requestTokenWithRetry() {
        try {
            return requestToken();
        } catch (ResourceAccessException | HttpServerErrorException exception) {
            log.warn("Failed to get GigaChat token. Retrying once.", exception);

            return requestToken();
        }
   }

    private TokenResponse requestToken() {
        TokenResponse renewToken = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v2/oauth")
                        .build()
                )
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("RqUID", UUID.randomUUID().toString())
                .header("Authorization", "Basic " + authKey)
                .body("scope=GIGACHAT_API_PERS")
                .retrieve()
                .body(TokenResponse.class);

        log.info(
                "GigaChat token received successfully: expiresAt={}",
                renewToken.expiresAt()
        );

        return renewToken;
    }
}