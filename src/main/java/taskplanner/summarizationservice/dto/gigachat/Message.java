package taskplanner.summarizationservice.dto.gigachat;

import java.util.List;

public record Message(
        String role,
        List<Content> content
) {
}
