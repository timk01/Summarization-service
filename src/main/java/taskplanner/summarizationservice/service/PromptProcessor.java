package taskplanner.summarizationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import taskplanner.summarizationservice.config.SummarizationMainConfig;
import taskplanner.summarizationservice.dto.SummarizationRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Component
public class PromptProcessor {

    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(ZoneId.of(SummarizationMainConfig.TIME_ZONE));

    private static final String SYSTEM_PROMPT = """
            Сформируй краткий ежедневный отчёт пользователя
            на основании предоставленных данных о задачах.

            Не придумывай задачи, статусы, даты, время или другие факты,
            отсутствующие во входных данных.

            Для завершённых задач указывай время завершения
            в формате YYYY.MM.DD HH:MM, например: 2026.09.18 10:30.

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
            - Починить кран | Срочно | FINISHED | 2026-09-18T09:15:00Z

            unfinishedTasks:
            - Сходить в магазин | Купить продукты | IN_PROCESS
            - Отправить письмо | Подготовить и отправить письмо | CREATED

            Пример правильного отчёта:

            Период:
            18.09.2026 - 19.09.2026

            Завершённые задачи:
            1. Починить кран. Срочно. 2026.09.18 09:15

            Незавершённые задачи:
            1. Сходить в магазин. Купить продукты. IN_PROCESS
            2. Отправить письмо. Подготовить и отправить письмо. CREATED

            Не добавляй текст до или после отчёта.
            Не добавляй служебные символы, JSON-разметку или комментарии.
            """;

    private final ObjectMapper objectMapper;

    public String buildUserPrompt(SummarizationRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);

            return """
                    Период: %s - %s

                    Данные пользователя:
                    %s
                    """.formatted(
                    DATE_FORMATTER.format(request.from()),
                    DATE_FORMATTER.format(request.to()),
                    json
            );
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }
}
