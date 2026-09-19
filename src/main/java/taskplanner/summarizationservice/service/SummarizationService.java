package taskplanner.summarizationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import taskplanner.summarizationservice.response.gigachat.ChatResponse;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;

import static taskplanner.summarizationservice.service.Prompt.SYSTEM_PROMPT;

@Slf4j
@RequiredArgsConstructor
@Service
public class SummarizationService {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @EventListener(ApplicationReadyEvent.class)
    public void testReport() {
        getSummary();
    }

    private UserTasks userTasks = new UserTasks(
            List.of(
                    new TaskResponse(
                            "Изучить GigaChat API",
                            "Разобраться с OAuth и получить access token",
                            TaskStatus.FINISHED,
                            OffsetDateTime.parse("2026-09-18T10:30:00Z")
                    ),
                    new TaskResponse(
                            "Проверить первый summary",
                            "Отправить тестовые данные в GigaChat и получить отчёт",
                            TaskStatus.FINISHED,
                            OffsetDateTime.parse("2026-09-18T18:15:00Z")
                    )
            ),
            List.of(
                    new TaskResponse(
                            "Написать Summarization",
                            "Сделать интеграцию с GigaChat",
                            TaskStatus.CREATED,
                            null
                    ),
                    new TaskResponse(
                            "Настроить Kafka RPC",
                            "Сделать request/reply между Scheduler и Summarization",
                            TaskStatus.IN_PROCESS,
                            null
                    )
            )
    );

    public void getSummary() {
        String json = objectMapper.writeValueAsString(userTasks);

        String prompt = """
                Период: 17.09.2026 - 18.09.2026

                Данные пользователя:
                %s
                """.formatted(json); //""".formatted(from, to, json); //пощзже, когда придут инстанты

        ChatRequest chatRequest = new ChatRequest(
                "GigaChat-2", //GigaChat-2-Max
                List.of(
                        new Message(
                                "system",
                                List.of(
                                        new Content(SYSTEM_PROMPT)
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

        TokenResponse token = tokenService.getValidToken();

        ChatResponse body = restClient.post()
                .uri(
                        "https://api.giga.chat/v2/chat/completions" //hardcode

                )
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token.accessToken())
                .body(chatRequest)
                .retrieve()
                .body(ChatResponse.class);

        log.info(
                "GigaChat summary received successfully: body={}",
                body
        );
    }
}
