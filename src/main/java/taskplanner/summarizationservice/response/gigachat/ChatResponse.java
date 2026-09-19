package taskplanner.summarizationservice.response.gigachat;

import taskplanner.summarizationservice.dto.gigachat.Message;

import java.util.List;

public record ChatResponse(
        List<Message> messages
) {
}
