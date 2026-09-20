package taskplanner.summarizationservice.dto.gigachat.response;

import taskplanner.summarizationservice.dto.gigachat.request.Message;

import java.util.List;

public record ChatResponse(
        List<Message> messages
) {
}
