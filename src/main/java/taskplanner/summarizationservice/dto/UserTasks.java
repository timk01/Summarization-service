package taskplanner.summarizationservice.dto;

import java.util.List;

public record UserTasks(
        List<TaskResponse> finishedTasks,
        List<TaskResponse> unfinishedTasks
) {
}