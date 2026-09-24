package taskplanner.summarizationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import taskplanner.summarizationservice.dto.token.TokenResponse;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private RestClient restClient;

    @Mock
    private Clock clock;

    @Mock
    private RestClient.RequestBodyUriSpec requestUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        when(restClient.post())
                .thenReturn(requestUriSpec);

        when(requestUriSpec.uri("/api/v2/oauth"))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.header(
                "Content-Type",
                "application/x-www-form-urlencoded"
        )).thenReturn(requestBodySpec);

        when(requestBodySpec.header(
                "Accept",
                "application/json"
        )).thenReturn(requestBodySpec);

        when(requestBodySpec.header(
                eq("RqUID"),
                anyString()
        )).thenReturn(requestBodySpec);

        ReflectionTestUtils.setField(
                tokenService,
                "authKey",
                "test-auth-key"
        );

        when(requestBodySpec.header(
                "Authorization",
                "Basic test-auth-key"
        )).thenReturn(requestBodySpec);

        when(requestBodySpec.body("scope=GIGACHAT_API_PERS"))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);
    }

    @Test
    void getValidTokenIsSucceededWhenTokenDoesNotExist() {
        TokenResponse expected = new TokenResponse(
                "token-1",
                1_000_000L
        );

        when(responseSpec.body(TokenResponse.class))
                .thenReturn(expected);

        TokenResponse actual = tokenService.getValidToken();

        assertThat(actual).isEqualTo(expected);

        verify(restClient).post();
        verify(responseSpec).body(TokenResponse.class);
    }

    @Test
    void getValidTokenReturnsExistingTokenWhenTokenIsStillValid() {
        TokenResponse expected = new TokenResponse(
                "token-1",
                1_000_000L
        );

        when(responseSpec.body(TokenResponse.class))
                .thenReturn(expected);

        TokenResponse first = tokenService.getValidToken();

        when(clock.millis())
                .thenReturn(1_000_000L - Duration.ofMinutes(10).toMillis());

        TokenResponse second = tokenService.getValidToken();

        assertThat(second).isEqualTo(first);

        verify(restClient, times(1)).post();
    }

    @Test
    void getValidTokenRefreshesExpiredToken() {
        long expiration = 1_000_000L;

        TokenResponse firstToken = new TokenResponse(
                "token-1",
                expiration
        );

        when(responseSpec.body(TokenResponse.class))
                .thenReturn(firstToken);

        TokenResponse first = tokenService.getValidToken();

        long expired = expiration + Duration.ofMinutes(35).toMillis();

        when(clock.millis())
                .thenReturn(expired);

        TokenResponse secondToken = new TokenResponse(
                "token-2",
                expired
        );

        when(responseSpec.body(TokenResponse.class))
                .thenReturn(secondToken);

        TokenResponse second = tokenService.getValidToken();

        assertThat(first).isEqualTo(firstToken);
        assertThat(second).isEqualTo(secondToken);

        assertThat(second).isNotEqualTo(first);
        verify(restClient, times(2)).post();
    }

    @Test
    void getValidTokenRetriesAfterRequestFailure() {
        TokenResponse expected = new TokenResponse(
                "token-after-retry",
                1_000_000L
        );

        when(responseSpec.body(TokenResponse.class))
                .thenThrow(new ResourceAccessException("Connection failed"))
                .thenReturn(expected);

        TokenResponse actual = tokenService.getValidToken();

        assertThat(actual).isEqualTo(expected);

        verify(restClient, times(2)).post();
    }

    @Test
    void getValidTokenThrowsExceptionWhenRetryAlsoFails() {
        when(responseSpec.body(TokenResponse.class))
                .thenThrow(new ResourceAccessException("Connection failed"))
                .thenThrow(new ResourceAccessException("Connection failed again"));

        assertThatThrownBy(() -> tokenService.getValidToken())
                .isInstanceOf(ResourceAccessException.class)
                .hasMessage("Connection failed again");

        verify(restClient, times(2)).post();
    }
}