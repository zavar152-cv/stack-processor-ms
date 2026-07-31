# Zorth Microservices

Микросервисная платформа для компиляции и исполнения программ на языке Zorth. Учебный проект развивает [автономный транслятор и стековый процессор](https://github.com/zavar152-cv/stack-processor) и [монолитный Web API](https://github.com/zavar152-cv/stack-processor-spring) до распределённой архитектуры на Spring Boot и Spring Cloud.

Проект командный. Ярослав Абузов разрабатывал backend-компоненты; полный список участников сохранён в истории Git.

## Что реализовано

- API Gateway и единая внешняя точка входа;
- Eureka service discovery;
- централизованная конфигурация через Spring Cloud Config;
- отдельные сервисы пользователей и аутентификации;
- JWT и ролевая модель доступа;
- разделение трансляции Zorth и исполнения машинного кода;
- синхронное взаимодействие сервисов через OpenFeign;
- события уведомлений через Apache Kafka;
- WebSocket/STOMP для обработки файловых запросов;
- email-уведомления;
- PostgreSQL, JPA и R2DBC;
- Resilience4j Circuit Breaker;
- OpenAPI для HTTP-сервисов;
- тесты с JUnit, WireMock, Testcontainers и REST Assured.

## Сервисы

| Модуль | Назначение |
| --- | --- |
| `gateway` | маршрутизация запросов и проверка JWT |
| `config-server` | централизованная конфигурация сервисов |
| `eureka-server` | регистрация и обнаружение экземпляров |
| `user-service` | пользователи, роли и история запросов |
| `auth-service` | вход и валидация токенов |
| `zorth-translator` | компиляция Zorth и хранение листингов |
| `zorth-processor` | исполнение машинного кода и результаты |
| `file-service` | загрузка и скачивание исходных файлов |
| `ws-service` | STOMP/WebSocket-канал обработки файлов |
| `notification-service` | Kafka consumer и email-уведомления |

## Поток запроса

```text
Client
  ↓
API Gateway ──→ Auth Service ──→ User Service
  ↓
Zorth Translator ──Feign──→ Zorth Processor
  │                         │
  ├── PostgreSQL            └── R2DBC / PostgreSQL
  └── Kafka ──→ Notification Service ──→ Email

File Service ⇄ WebSocket Service ⇄ Zorth Translator
```

Все сервисы получают адреса и параметры через Config Server и регистрируются в Eureka.

## Требования

- Java 17;
- Docker Engine с Compose v2;
- Maven Wrapper из репозитория;
- доступ к Maven-репозиториям с артефактами Zorth.

## Запуск

Создайте локальный файл с секретами:

```bash
cp .env.example .env
```

Замените все значения в `.env`, затем запустите инфраструктуру и сервисы:

```bash
docker compose up
```

Основные адреса:

- API Gateway — `http://localhost:8765`;
- Eureka dashboard — `http://localhost:8761`;
- Config Server — `http://localhost:8888`;
- Kafka для host-приложений — `localhost:29092`.

Compose-файл ссылается на заранее собранные образы участников проекта. Для полностью воспроизводимой сборки их следует заменить локальными image/build-секциями или опубликовать версии образов в доступном registry.

## Сборка и тесты

```bash
./mvnw test
```

Часть тестов использует Testcontainers, поэтому требуется работающий Docker daemon. Каждый сервис также можно тестировать отдельно, например:

```bash
./mvnw -pl auth-service test
./mvnw -pl zorth-translator test
```

## Безопасность

- `.env` исключён из Git;
- JWT-ключ, пароль администратора и SMTP credentials передаются через environment variables;
- значения в `.env.example` являются только шаблоном;
- ранее опубликованные JWT- и Gmail-секреты необходимо считать скомпрометированными и заменить у провайдера;
- перед публичным развёртыванием нужно включить TLS и защищённый Kafka listener.

## Статус

Учебный командный проект по высоконагруженным системам. Он демонстрирует декомпозицию монолита, discovery, gateway, реактивный доступ к данным и асинхронные события; production-ready эксплуатация потребует наблюдаемости, управления секретами и доработки deployment-конфигурации.
