package taskplanner.summarizationservice.dto.summarizattion.request;

import java.time.Instant;
import java.util.List;

public record SummarizationRequest(
        Instant from,
        Instant to,
        List<TaskRequest> finishedTasks,
        List<TaskRequest> unfinishedTasks
) {
}