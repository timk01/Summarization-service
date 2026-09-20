package taskplanner.summarizationservice.dto.gigachat.request;

import java.util.List;

public record Message(
        String role,
        List<Content> content
) {
}
