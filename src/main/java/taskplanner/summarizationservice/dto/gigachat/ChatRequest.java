package taskplanner.summarizationservice.dto.gigachat;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages
) {
}
