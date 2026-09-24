# Summarization Service

Сервис формирования ежедневного summary-отчёта по задачам пользователя.

Summarization Service получает данные о завершённых и незавершённых задачах от **Scheduler** через Kafka, формирует prompt для GigaChat, получает текстовый отчёт и возвращает его обратно Scheduler в рамках Kafka request-reply взаимодействия.

## Схема работы

```text
Scheduler
→ SCHEDULER_SUMMARIZATION_REQUESTS
→ Summarization Service
→ подготовка prompt
→ получение / переиспользование GigaChat access token
→ GigaChat API
→ SummarizationResponse
→ Kafka reply
→ Scheduler
```

Сервис не предоставляет пользовательский HTTP API и запускается как non-web Spring Boot application.

## Kafka

Входящий топик:

```text
SCHEDULER_SUMMARIZATION_REQUESTS
```

Сервис принимает `SummarizationRequest`:

```json
{
  "from": "2026-09-23T20:00:00Z",
  "to": "2026-09-24T20:00:00Z",
  "finishedTasks": [
    {
      "header": "Finished task",
      "text": "Task text",
      "status": "FINISHED",
      "finishedAt": "2026-09-24T20:30:00+03:00"
    }
  ],
  "unfinishedTasks": [
    {
      "header": "Unfinished task",
      "text": "Task text",
      "status": "CREATED"
    }
  ]
}
```

Результат возвращается Scheduler через Kafka request-reply:

```json
{
  "report": "Generated daily summary"
}
```

Reply topic передаётся в Kafka-запросе со стороны Scheduler.

## Формирование prompt

`PromptProcessor` преобразует `SummarizationRequest` в пользовательский prompt для GigaChat.

В prompt передаются:

- период отчёта;
- завершённые задачи с временем завершения;
- незавершённые задачи с текущим статусом.

Дата и время форматируются в часовом поясе:

```text
Europe/Moscow
```

Формат даты и времени:

```text
yyyy.MM.dd HH:mm
```

System prompt ограничивает модель входными данными и задаёт формат итогового отчёта.

## GigaChat

Для суммаризации используется базовая "лайт" модель (можно выбрать другую):

```text
GigaChat-2
```

Запрос выполняется через:

```text
POST /v2/chat/completions
```

Перед запросом сервис получает валидный access token через GigaChat OAuth API.

### Работа с токеном

`TokenService` кеширует полученный token и переиспользует его, пока он остаётся валидным.

Токен обновляется заранее — за 2 минуты до `expiresAt`.

При `ResourceAccessException` или server-side HTTP error получение токена повторяется один раз. Если повторная попытка также завершается ошибкой, исключение передаётся выше.

## Конфигурация

Для запуска требуется GigaChat authorization key:

```env
AUTH_KEY=your_auth_token
```

Пример находится в `.env.example`.

Текущая локальная конфигурация Kafka:

```text
localhost:9092
```

GigaChat endpoints:

```text
https://ngw.devices.sberbank.ru:9443
https://api.giga.chat
```

## Технологии

- Java 21
- Spring Boot
- Spring Kafka
- Spring `RestClient`
- GigaChat API
- Gradle
- Lombok
- JUnit 5
- Mockito

## Тесты

Unit-тестами покрыта собственная логика сервиса:

- формирование user prompt и преобразование времени;
- получение и кеширование GigaChat token;
- обновление истёкшего token;
- повторная попытка получения token при временной ошибке.

Kafka transport и работа Spring infrastructure отдельно unit-тестами не проверяются.
