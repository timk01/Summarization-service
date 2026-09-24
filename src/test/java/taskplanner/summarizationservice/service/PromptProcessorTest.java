package taskplanner.summarizationservice.service;

import org.junit.jupiter.api.Test;
import taskplanner.summarizationservice.dto.summarizattion.request.SummarizationRequest;
import taskplanner.summarizationservice.dto.summarizattion.request.TaskRequest;
import taskplanner.summarizationservice.dto.summarizattion.request.TaskStatusRequestEnum;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptProcessorTest {

    private final PromptProcessor promptProcessor = new PromptProcessor();

    @Test
    void buildUserPromptIsSucceeded() {
        SummarizationRequest request = new SummarizationRequest(
                Instant.parse("2026-09-23T20:00:00Z"),
                Instant.parse("2026-09-24T20:00:00Z"),
                List.of(
                        new TaskRequest(
                                "Finished task 1",
                                "Finished task text 1",
                                TaskStatusRequestEnum.FINISHED,
                                OffsetDateTime.parse("2026-09-24T18:30:00Z")
                        ),
                        new TaskRequest(
                                "Finished task 2",
                                "Finished task text 2",
                                TaskStatusRequestEnum.FINISHED,
                                OffsetDateTime.parse("2026-09-24T18:45:00Z")
                        )
                ),
                List.of(
                        new TaskRequest(
                                "Unfinished task 1",
                                "Unfinished task text 1",
                                TaskStatusRequestEnum.CREATED,
                                null
                        ),
                        new TaskRequest(
                                "Unfinished task 2",
                                "Unfinished task text 2",
                                TaskStatusRequestEnum.IN_PROCESS,
                                null
                        )
                )
        );

        String expected = """
                Период: 2026.09.23 23:00 - 2026.09.24 23:00

                finishedTasks:
                - Finished task 1 ___ Finished task text 1 ___ FINISHED ___ 2026.09.24 21:30
                - Finished task 2 ___ Finished task text 2 ___ FINISHED ___ 2026.09.24 21:45

                unfinishedTasks:
                - Unfinished task 1 ___ Unfinished task text 1 ___ CREATED
                - Unfinished task 2 ___ Unfinished task text 2 ___ IN_PROCESS
                """;

        String actual = promptProcessor.buildUserPrompt(request);

        assertThat(actual).isEqualTo(expected);
    }
}