package taskplanner.summarizationservice.dto.gigachat.response;

import taskplanner.summarizationservice.dto.gigachat.request.Content;

import java.util.List;

public record Message(
        String role,
        List<Content> content
) {
}
