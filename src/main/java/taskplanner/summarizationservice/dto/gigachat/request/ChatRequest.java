package taskplanner.summarizationservice.dto.gigachat.request;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages
) {
}
