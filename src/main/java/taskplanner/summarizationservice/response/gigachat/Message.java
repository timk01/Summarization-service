package taskplanner.summarizationservice.response.gigachat;

import taskplanner.summarizationservice.dto.gigachat.Content;

import java.util.List;

public record Message(
        String role,
        List<Content> content
) {
}
