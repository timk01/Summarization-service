package taskplanner.summarizationservice.dto;

import java.time.Instant;
import java.util.List;

public record SummarizationRequest(
        Instant from,
        Instant to,
        List<TaskResponse> finishedTasks,
        List<TaskResponse> unfinishedTasks
) {
}