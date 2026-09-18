package taskplanner.summarizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import taskplanner.summarizationservice.dto.TaskResponse;
import taskplanner.summarizationservice.dto.TaskStatus;
import taskplanner.summarizationservice.dto.UserTasks;
import taskplanner.summarizationservice.dto.gigachat.ChatRequest;
import taskplanner.summarizationservice.dto.gigachat.Content;
import taskplanner.summarizationservice.dto.gigachat.Message;
import taskplanner.summarizationservice.response.TokenResponse;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private List<UserTasks> userTasks = List.of(
            new UserTasks(
                    165L,
                    "tim_1789499105@mail.ru",
                    List.of(
                            new TaskResponse(
                                    21L,
                                    "Изучить GigaChat API",
                                    "Разобраться с OAuth и получить access token",
                                    TaskStatus.FINISHED,
                                    OffsetDateTime.parse("2026-09-18T10:30:00Z"),
                                    165L
                            )
                    ),
                    List.of()
            ),

            new UserTasks(
                    166L,
                    "user166@mail.ru",
                    List.of(),
                    List.of(
                            new TaskResponse(
                                    23L,
                                    "Написать Summarization",
                                    "Сделать интеграцию с GigaChat",
                                    TaskStatus.CREATED,
                                    null,
                                    166L
                            )
                    )
            ),

            new UserTasks(
                    167L,
                    "user167@mail.ru",
                    List.of(),
                    List.of(
                            new TaskResponse(
                                    24L,
                                    "Настроить Kafka RPC",
                                    "Сделать request/reply между Scheduler и Summarization",
                                    TaskStatus.IN_PROCESS,
                                    null,
                                    167L
                            )
                    )
            )
    );

    private String task = """
            Ты формируешь краткий ежедневный отчёт пользователя
            на основании предоставленных данных о задачах.
                        
            Не придумывай задачи и факты, отсутствующие во входных данных.
                        
            Формат отчёта:
                        
            Период:
            ...
                        
            Завершенные задачи:
            ...
                        
            Незавершенные задачи:
            ...
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${auth.key}")
    private String authKey;

    @EventListener(ApplicationReadyEvent.class)
    public void testReport() {
        getToken();
    }

    public void getToken() {
        TokenResponse token = restClient.post()
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
                token.expiresAt()
        );

        //String string = token.accessToken();
        //System.out.println(string);
        Long l = token.expiresAt();

        getSummary(token);
    }

    public void getSummary(TokenResponse token) {
        String json = objectMapper.writeValueAsString(userTasks);

        String prompt = """
        Период: 17.09.2026 - 18.09.2026

        Данные пользователя:
        %s
        """.formatted(json); //""".formatted(from, to, json); //пощзже, когда придут инстанты

        ChatRequest chatRequest = new ChatRequest(
                "GigaChat-2-Max",
                List.of(
                        new Message(
                                "system",
                                List.of(
                                        new Content(task)
                                )
                        ),
                        new Message(
                                "user",
                                List.of(
                                        new Content(prompt)
                                )
                        )
                )
        );

        String body = restClient.post()
                .uri(
                        "https://api.giga.chat/v2/chat/completions" //hardcode

                )
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token.accessToken())
                .body(chatRequest)
                .retrieve()
                .body(String.class);

        log.info(
                "GigaChat summary received successfully: body={}",
                body
        );

    }
}