# Task Time Tracker API

Task Time Tracker API — REST-сервис на Spring Boot для учета задач и рабочего времени сотрудников.

Сервис позволяет:
- регистрировать и авторизовывать сотрудников;
- создавать задачи и менять их статус;
- фиксировать временные интервалы, затраченные сотрудником на задачу;
- получать запись времени по идентификатору;
- получать список записей времени сотрудника по фильтрам и пагинации;
- получать сводную информацию по рабочему времени сотрудника;
- ускорять чтение данных через Redis по паттерну Cache Aside.

---

## Что реализовано

### Основной функционал
- создание сотрудника;
- авторизация сотрудника и выдача JWT;
- получение сотрудника по ID;
- диагностический эндпоинт текущей аутентификации;
- создание задачи;
- получение задачи по ID;
- изменение статуса задачи;
- создание записи о затраченном времени;
- получение записи времени по ID;
- получение списка записей времени сотрудника за период;
- получение агрегированной информации по времени сотрудника за период.

---

### Дополнительно
- Bearer Authentication через JWT;
- валидация входных DTO;
- глобальная обработка исключений;
- MyBatis для работы с БД;
- Liquibase для миграций;
- Swagger UI и OpenAPI (на русском языке);
- unit-тесты;
- интеграционные тесты DAO- и service-слоя с Testcontainers;
- Redis-кеширование по паттерну Cache Aside.

---

## Инициализация данных

### DefaultEmployeesInitializer

В проекте реализован `DefaultEmployeesInitializer`, который автоматически создает пользователей при старте приложения:

**ADMIN**
- login: `admin`
- password: `admin_password`

**USER**
- login: `pavlov`
- password: `pavlov_password`

---

### Seed-данные (Liquibase)

Дополнительные тестовые данные (сотрудники, задачи, записи времени) загружаются автоматически через Liquibase.

Файлы с seed-данными находятся в директории:

`src/main/resources/db/changelog/sql`

Используются следующие файлы:
- `004-seed-employees.sql`
- `005-seed-tasks.sql`
- `006-seed-time-records.sql`

Таким образом после запуска приложения база уже содержит тестовые данные, с которыми можно сразу работать.

---

## Технологии

- Java 21
- Spring Boot 3.5.12
- Spring Web
- Spring Validation
- Spring Security
- MyBatis Spring Boot Starter 3.0.4
- MapStruct 1.6.3
- Liquibase
- PostgreSQL
- Redis
- JWT (jjwt 0.13.0)
- SpringDoc OpenAPI / Swagger UI 2.8.9
- JUnit 5
- Mockito
- Testcontainers
- Maven
- Docker Compose

---

## Паттерн Cache Aside

Cache Aside реализован в сервисах:
- `EmployeeService`
- `TaskService`
- `TimeRecordService`

Логика работы:
1. При чтении сервис сначала проверяет Redis.
2. Если записи в кеше нет, данные читаются из PostgreSQL.
3. После чтения из БД значение сохраняется в Redis с TTL.
4. При недоступности Redis приложение продолжает работать через БД.
5. При создании или обновлении данных кеш обновляется вручную.

---

## REST API

### Сотрудники
- `POST /api/employee` — регистрация сотрудника
- `POST /api/employee/auth` — авторизация и получение JWT
- `GET /api/employee/{id}` — получить сотрудника по ID
- `GET /api/employee/debug/auth` — показать данные текущей аутентификации

---

### Задачи
- `POST /api/tasks` — создать задачу
- `GET /api/tasks/{id}` — получить задачу по ID
- `PATCH /api/tasks/{id}/status` — изменить статус задачи

---

### Учет времени
- `POST /api/time-records` — создать запись времени
- `GET /api/time-records/{id}` — получить запись времени по ID
- `POST /api/time-records/search` — получить список записей времени сотрудника по фильтрам
- `POST /api/time-records/info` — получить сводную информацию по времени сотрудника

---

## Роли и доступ

### Без авторизации
- `POST /api/employee`
- `POST /api/employee/auth`
- `GET /api/employee/debug/auth`
- Swagger UI / OpenAPI

---

### USER
- `GET /api/tasks/{id}`
- `PATCH /api/tasks/{id}/status`
- `POST /api/time-records`
- `GET /api/time-records/{id}`
- `POST /api/time-records/search`
- `POST /api/time-records/info`

---

### ADMIN
- все возможности USER
- `GET /api/employee/{id}`
- `POST /api/tasks`

В Swagger/OpenAPI указано, какие методы доступны только пользователю, только администратору и обеим ролям одновременно.

---

## Сценарий работы с API

1. Зарегистрироваться  
   `POST /api/employee`

2. Авторизоваться  
   `POST /api/employee/auth`

3. Получить JWT токен

4. Вставить токен:
   - в Postman → Authorization → Bearer Token
   - или в Swagger через кнопку Authorize

5. Использовать защищенные эндпоинты:
   - создавать задачи (ADMIN)
   - создавать записи времени (USER)
   - получать данные и статистику

Также можно не регистрировать нового пользователя, а использовать уже созданных тестовых пользователей:
- `admin / admin_password`
- `pavlov / pavlov_password`

---

## Конфигурация

Все значения вынесены в `.env`.

### Основные переменные окружения
- `SPRING_PROFILES_ACTIVE`
- `TIME_TRACKER_APPLICATION_NAME`
- `TIME_TRACKER_SERVER_PORT`
- `POSTGRES_TIME_TRACKER_DB_PORT`
- `POSTGRES_TIME_TRACKER_DB`
- `POSTGRES_TIME_TRACKER_USER`
- `POSTGRES_TIME_TRACKER_PASSWORD`
- `TIME_TRACKER_DB_URL`
- `TIME_TRACKER_DB_DRIVER_CLASS_NAME`
- `TIME_TRACKER_DB_SCHEMA`
- `TIME_TRACKER_LIQUIBASE_ENABLED`
- `TIME_TRACKER_REDIS_HOST`
- `TIME_TRACKER_REDIS_PORT`
- `TIME_TRACKER_REDIS_DB`
- `TIME_TRACKER_REDIS_TIMEOUT`
- `TIME_TRACKER_CACHE_TYPE`
- `JWT_SECRET_KEY`
- `JWT_LIFETIME_MS`
- `TIME_TRACKER_DEFAULT_PAGE_NUMBER`
- `TIME_TRACKER_DEFAULT_PAGE_SIZE`
- `TIME_TRACKER_CACHE_DEFAULT_TTL`
- `TIME_TRACKER_CACHE_TASKS_TTL`
- `TIME_TRACKER_CACHE_EMPLOYEES_TTL`
- `TIME_TRACKER_CACHE_TIME_RECORDS_TTL`
- `TIME_TRACKER_OPENAPI_SERVER_URL`
- `APP_TIMEZONE`
- `LOGGING_LEVEL_ORG_HIBERNATE_SQL`
- `LOGGING_LEVEL_ORG_HIBERNATE_BIND`

### Пример `.env`

```env
SPRING_PROFILES_ACTIVE=dev
TIME_TRACKER_APPLICATION_NAME=task_time_tracker_api
TIME_TRACKER_SERVER_PORT=8090

POSTGRES_TIME_TRACKER_DB_PORT=5544
POSTGRES_TIME_TRACKER_DB=tt_tracker_db
POSTGRES_TIME_TRACKER_USER=tt_user
POSTGRES_TIME_TRACKER_PASSWORD=tt_pass_2026
TIME_TRACKER_DB_URL=jdbc:postgresql://localhost:5544/tt_tracker_db
TIME_TRACKER_DB_DRIVER_CLASS_NAME=org.postgresql.Driver
TIME_TRACKER_DB_SCHEMA=public
TIME_TRACKER_LIQUIBASE_ENABLED=true

TIME_TRACKER_REDIS_HOST=localhost
TIME_TRACKER_REDIS_PORT=6385
TIME_TRACKER_REDIS_DB=1
TIME_TRACKER_REDIS_TIMEOUT=2s
TIME_TRACKER_CACHE_TYPE=redis

JWT_SECRET_KEY=YlQ3bU1oQ3lWZlZzV3Z6Z0dRZ2R0c1lXb3VtQ2ZyQ1JXQk1vT2h6R2t3aWJ0bHk3U0p6T1B6eVY2Z2x4Z0l3Yk5nY2l3TnZVQ1h2TnF3dEo
JWT_LIFETIME_MS=86400000

TIME_TRACKER_DEFAULT_PAGE_NUMBER=0
TIME_TRACKER_DEFAULT_PAGE_SIZE=5
TIME_TRACKER_CACHE_DEFAULT_TTL=45s
TIME_TRACKER_CACHE_TASKS_TTL=45s
TIME_TRACKER_CACHE_EMPLOYEES_TTL=45s
TIME_TRACKER_CACHE_TIME_RECORDS_TTL=45s

TIME_TRACKER_OPENAPI_SERVER_URL=http://localhost:8090

APP_TIMEZONE=Europe/Moscow
LOGGING_LEVEL_ORG_HIBERNATE_SQL=INFO
LOGGING_LEVEL_ORG_HIBERNATE_BIND=INFO

---

## Сборка проекта

```bash
./mvnw clean package
```

**Windows PowerShell:**

```bash
.\mvnw.cmd clean package
```

---

## Локальный запуск сервисов без Docker

Перед запуском нужно отдельно поднять PostgreSQL и Redis и выставить переменные окружения из `.env`.

**macOS/Linux:**

```bash
./mvnw spring-boot:run
```

**Windows PowerShell:**

```bash
.\mvnw.cmd spring-boot:run
```

---

## Swagger/UI

- Swagger UI — `http://localhost:8090/swagger-ui.html`
- OpenAPI JSON — `http://localhost:8090/v3/api-docs`
- OpenAPI YAML — `http://localhost:8090/openapi.yaml`

---

## Compose команды

- Запуск общего стека:

  ```bash
  docker compose --env-file .env up --build -d
  ```

- Остановка контейнеров:

  ```bash
  docker compose --env-file .env stop
  ```

- Запуск ранее остановленных контейнеров:

  ```bash
  docker compose --env-file .env start
  ```

- Остановка и удаление контейнеров:

  ```bash
  docker compose --env-file .env down
  ```

- Остановка с удалением томов:

  ```bash
  docker compose --env-file .env down -v
  ```

- Удаление с orphan-контейнерами:

  ```bash
  docker compose --env-file .env down --remove-orphans
  ```

---

## Запуск через Docker Compose

В проекте уже есть `Dockerfile`, `.env` и `docker-compose.yml`.

### 1. Подготовить `.env`

Создайте `.env` в корне проекта и укажите значения всех переменных окружения.

### 2. Запустить контейнеры

```bash
docker compose --env-file .env up --build -d
```

Если нужно повторно инициализировать базу данных с демо-данными на чистом volume:

```bash
docker compose --env-file .env down -v
docker compose --env-file .env up --build -d
```

### 3. Остановить контейнеры

```bash
docker compose --env-file .env down
```

После запуска будут доступны:

- приложение: `http://localhost:8090`
- Swagger UI: `http://localhost:8090/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8090/v3/api-docs`
- OpenAPI YAML: `http://localhost:8090/openapi.yaml`

---

## Как проверить работоспособность

1. Запустить PostgreSQL, Redis и приложение.
2. Открыть Swagger UI по адресу `http://localhost:8090/swagger-ui.html`.
3. Выполнить регистрацию сотрудника через `POST /api/employee` или использовать одного из тестовых пользователей.
4. Выполнить авторизацию через `POST /api/employee/auth` и получить JWT.
5. Передать JWT как Bearer Token.
6. Создать задачу, добавить запись времени и проверить получение данных.
