# ShareIt

Backend-сервис для аренды вещей между пользователями.

Проект реализует REST API для управления пользователями, вещами, бронированиями, запросами на вещи и отзывами после завершённого бронирования.

## Стек

- Java 17
- Spring Boot 3.2.4
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Lombok
- MapStruct
- QueryDSL
- Bean Validation
- Docker / Docker Compose

## Возможности

- Создание и управление пользователями
- Добавление, редактирование и поиск вещей
- Создание запросов на вещи
- Бронирование вещей
- Подтверждение или отклонение бронирований владельцем
- Получение списка бронирований пользователя
- Добавление отзывов после завершённого бронирования
- Поиск доступных вещей по текстовому запросу

## Архитектура

Проект построен по многослойной архитектуре:

- `controller` — REST endpoints
- `service` — бизнес-логика
- `repository` — работа с базой данных
- `model` — JPA-сущности
- `dto` — объекты передачи данных
- `mapper` — преобразование entity ↔ DTO
- `exception` — централизованная обработка ошибок

## Основные сущности

- `User` — пользователь сервиса
- `Item` — вещь для аренды
- `Booking` — бронирование вещи
- `ItemRequest` — запрос на вещь
- `Comment` — отзыв пользователя

## Примеры API

### Пользователи

```http
POST /users
GET /users
GET /users/{userId}
PATCH /users/{userId}
DELETE /users/{userId}
```

### Вещи

```http
POST /items
GET /items/{itemId}
GET /items
PATCH /items/{itemId}
GET /items/search?text=drill
```

### Бронирования

```http
POST /bookings
PATCH /bookings/{bookingId}?approved=true
GET /bookings/{bookingId}
GET /bookings
GET /bookings/owner
```

### Запросы на вещи

```http
POST /requests
GET /requests
GET /requests/all
GET /requests/{requestId}
```

## Запуск проекта

### Через Maven

```bash
mvn clean package
mvn spring-boot:run
```

### Через Docker Compose

```bash
docker-compose up --build
```

## Конфигурация

Приложение использует PostgreSQL. Параметры подключения задаются в `application.properties` или через переменные окружения при запуске в Docker.

## Что демонстрирует проект

- разработку REST API на Spring Boot;
- работу с PostgreSQL через Spring Data JPA и Hibernate;
- проектирование многослойной backend-архитектуры;
- реализацию бизнес-логики бронирования;
- использование DTO, mapper-слоя и валидации;
- контейнеризацию приложения через Docker;
- работу с Maven, Git и Pull Request workflow.
