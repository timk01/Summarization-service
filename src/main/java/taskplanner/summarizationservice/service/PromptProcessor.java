package taskplanner.summarizationservice.service;

import org.springframework.stereotype.Component;
import taskplanner.summarizationservice.config.SummarizationMainConfig;
import taskplanner.summarizationservice.dto.summarizattion.request.SummarizationRequest;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class PromptProcessor {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.of(SummarizationMainConfig.TIME_ZONE));

    private static final String SYSTEM_PROMPT = """
            Сформируй краткий ежедневный отчёт пользователя
            на основании предоставленных данных о задачах.

            Не придумывай задачи, статусы, даты, время или другие факты,
            отсутствующие во входных данных.

            Для завершённых задач указывай время завершения
            в формате YYYY.MM.DD HH:MM. например: 2026.09.18 10:30.

            Для незавершённых задач указывай текущий статус,
            если он предоставлен.

            Используй следующий формат отчёта:

            Период:
            ...

            Завершённые задачи:
            1. Название задачи. Описание. Время завершения.
            2. ...

            Незавершённые задачи:
            1. Название задачи. Описание. Статус.
            2. ...

            Пример входных данных:

            Период: 18.09.2026 - 19.09.2026

            finishedTasks:
            - Починить кран ___ Срочно ___ FINISHED ___ 2026.09.18 10:30

            unfinishedTasks:
            - Сходить в магазин ___ Купить продукты ___ IN_PROCESS
            - Отправить письмо ___ Подготовить и отправить письмо ___ CREATED

            Пример правильного отчёта:

            Период:
            18.09.2026 - 19.09.2026

            Завершённые задачи:
            1. Починить кран. Срочно. 2026.09.18 10:30

            Незавершённые задачи:
            1. Сходить в магазин. Купить продукты. IN_PROCESS
            2. Отправить письмо. Подготовить и отправить письмо. CREATED

            Не добавляй текст до или после отчёта.
            Не добавляй служебные символы, JSON-разметку или комментарии.
            """;

    public String buildUserPrompt(SummarizationRequest request) {
        String finishedTasks = prepareFinishedTasks(request);
        String unfinishedTasks = prepareUnfinishedTasks(request);

        return """
                Период: %s - %s

                finishedTasks:
                %s

                unfinishedTasks:
                %s
                """.formatted(
                DATE_TIME_FORMATTER.format(request.from()),
                DATE_TIME_FORMATTER.format(request.to()),
                finishedTasks,
                unfinishedTasks
        );
    }

    private String prepareFinishedTasks(SummarizationRequest request) {
        return request.finishedTasks().stream()
                .map(task -> "- %s ___ %s ___ %s ___ %s".formatted(
                        task.header(),
                        task.text(),
                        task.status(),
                        DATE_TIME_FORMATTER.format(task.finishedAt().toInstant())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String prepareUnfinishedTasks(SummarizationRequest request) {
        return request.unfinishedTasks().stream()
                .map(task -> "- %s ___ %s ___ %s".formatted(
                        task.header(),
                        task.text(),
                        task.status()
                ))
                .collect(Collectors.joining("\n"));
    }

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }
}
