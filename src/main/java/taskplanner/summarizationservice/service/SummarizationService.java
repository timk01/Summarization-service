package taskplanner.summarizationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import taskplanner.summarizationservice.dto.summarizattion.request.SummarizationRequest;
import taskplanner.summarizationservice.dto.gigachat.request.ChatRequest;
import taskplanner.summarizationservice.dto.gigachat.request.Content;
import taskplanner.summarizationservice.dto.gigachat.request.Message;
import taskplanner.summarizationservice.dto.summarizattion.response.SummarizationResponse;
import taskplanner.summarizationservice.dto.token.TokenResponse;
import taskplanner.summarizationservice.dto.gigachat.response.ChatResponse;

import java.util.List;

@Slf4j
@Service
public class SummarizationService {

    private static final String AI_MODEL = "GigaChat-2";
    private static final String SYSTEM_ROLE = "system";
    private static final String USER_ROLE = "user";

    private final TokenService tokenService;
    private final RestClient restClient;
    private final PromptProcessor promptProcessor;

    public SummarizationService(TokenService tokenService,
                                @Qualifier("gigaChatRestClient")
                                RestClient restClient,
                                PromptProcessor promptProcessor) {
        this.tokenService = tokenService;
        this.restClient = restClient;
        this.promptProcessor = promptProcessor;
    }

    public SummarizationResponse getSummary(SummarizationRequest dto) {
        String userPrompt = promptProcessor.buildUserPrompt(dto);
        String systemPrompt = promptProcessor.buildSystemPrompt();

        ChatRequest chatRequest = buildChatRequest(systemPrompt, userPrompt);

        TokenResponse token = tokenService.getValidToken();

        ChatResponse body = restClient.post()
                .uri("/v2/chat/completions")
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

        return new SummarizationResponse(body.messages().getFirst().content().getFirst().text());
    }

    private ChatRequest buildChatRequest(String systemPrompt, String userPrompt) {
        return new ChatRequest(
                AI_MODEL,
                List.of(
                        new Message(
                                SYSTEM_ROLE,
                                List.of(
                                        new Content(systemPrompt)
                                )
                        ),
                        new Message(
                                USER_ROLE,
                                List.of(
                                        new Content(userPrompt)
                                )
                        )
                )
        );
    }
}
