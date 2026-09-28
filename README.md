# Summarization Service

Сервис формирования ежедневного summary-отчёта по задачам пользователя.

Summarization Service получает данные о завершённых и незавершённых задачах от Scheduler через Kafka, формирует prompt для GigaChat, получает текстовый отчёт и возвращает его обратно Scheduler.

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
- Docker
- Docker Compose

## Роль в системе

Summarization Service отвечает только за формирование текстового summary.

```text
┌───────────┐       Kafka request        ┌───────────────────────┐        request        ┌──────────┐
│ Scheduler │ ─────────────────────────► │ Summarization Service │ ────────────────────► │ GigaChat │
│           │ ◄───────────────────────── │                       │ ◄──────────────────── │          │
└───────────┘        Kafka reply         └───────────────────────┘        response       └──────────┘
```

Scheduler передаёт сервису данные о задачах пользователя. Summarization Service формирует prompt, отправляет его в GigaChat и возвращает Scheduler готовый `SummarizationResponse`.

Сервис не занимается получением задач из Task Planner и не отправляет email самостоятельно.

## Kafka

Запросы от Scheduler поступают через Kafka-топик:

```text
SCHEDULER_SUMMARIZATION_REQUESTS
```

Сервис принимает `SummarizationRequest` с периодом отчёта, завершёнными и незавершёнными задачами пользователя.

Результат возвращается Scheduler через Kafka request-reply в виде `SummarizationResponse`.

## GigaChat

Для формирования summary используется GigaChat API.

Сервис самостоятельно получает и переиспользует access token для запросов к GigaChat.

Для запуска необходим authorization key:

```env
AUTH_KEY=...
```

Пример находится в `.env.example`.

## Переменные окружения

Основные переменные окружения:

```env
AUTH_KEY=...
KAFKA_BOOTSTRAP_SERVERS=...
```

Реальные секреты не должны попадать в Git.

## Запуск всего проекта

Summarization Service входит в общий Docker Compose-стек проекта.

Общий `compose.yml` находится в репозитории Task Planner и позволяет запустить сервис вместе с остальными компонентами приложения, PostgreSQL и Kafka из готовых Docker-образов.

Инструкция по запуску всего проекта находится в README Task Planner.

## Локальная разработка

Summarization Service можно запускать локально отдельно от общего Docker Compose-стека.

Для локального запуска необходимо передать `AUTH_KEY` через environment variables. Kafka при локальном запуске доступна по адресу:

```text
localhost:9094
```

## Тесты

Unit-тестами покрыта основная собственная логика сервиса:

- формирование prompt;
- преобразование времени;
- получение и кеширование GigaChat access token;
- обновление токена;
- повторная попытка получения токена при временной ошибке.

Запуск:

```bash
./gradlew test
```

## CI/CD

При push в `main` GitHub Actions запускает тесты, собирает Docker-образ и публикует его в Docker Hub.
