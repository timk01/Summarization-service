package taskplanner.summarizationservice.dto;

import java.util.List;

public record SummarizationRequest(
        List<TaskResponse> finishedTasks,
        List<TaskResponse> unfinishedTasks
) {
}