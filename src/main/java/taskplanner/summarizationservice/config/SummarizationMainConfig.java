package taskplanner.summarizationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class SummarizationMainConfig {

    public static final String TIME_ZONE = "Europe/Moscow";

    @Value("${gigachat.auth-url}")
    private String authUrl;

    @Value("${gigachat.api-url}")
    private String apiUrl;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(TIME_ZONE));
    }

    @Bean
    public RestClient tokenRestClient() {
        return RestClient.builder()
                .baseUrl(authUrl)
                .build();
    }

    @Bean
    public RestClient gigaChatRestClient() {
        return RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }
}
