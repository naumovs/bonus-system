# Loyalty Service Prototype

Это учебный проект, представляющий собой прототип сервиса лояльности, который начисляет бонусные баллы за транзакции согласно заданным правилам.

## Технологии и библиотеки

- **ZIO** - функциональная библиотека для Scala
- **ZIO HTTP** - HTTP сервер
- **Quill** - библиотека для работы с базой данных
- **PostgreSQL** - реляционная база данных
- **Tofu Logging** - логирование
- **Typesafe Config** - конфигурация
- **ujson** - работа с JSON

## Архитектура приложения

Приложение построено по слоистой архитектуре:

1. **API слой** (`loyalty.api`):
    - Обрабатывает HTTP запросы
    - Валидирует входные данные
    - Преобразует ошибки в HTTP ответы

2. **Сервисный слой** (`loyalty.core.service`):
    - Содержит бизнес-логику
    - Координирует работу репозиториев
    - Обрабатывает транзакции и начисляет бонусы

3. **Репозитории** (`loyalty.core.repository`):
    - Работают с базой данных
    - Реализуют CRUD операции
    - Содержат правила начисления бонусов

4. **Доменный слой** (`loyalty.core.domain`):
    - Определяет основные сущности (Транзакция, Бонусы и т.д.)
    - Содержит типы ошибок

## Примеры запросов для тестирования API

### Создание транзакции
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "id": {"value": "tx1"},
    "clientId": {"value": "client1"},
    "amount": 1000.0,
    "currency": "RUB",
    "category": "TRAVEL",
    "source": "POS",
    "status": "CONFIRMED"
  }'
```

### Получение баланса клиента
```bash
curl -X GET http://localhost:8080/clients/client1/balance
```

### Получение истории операций
```bash
curl -X GET http://localhost:8080/clients/client1/history
```

### Отмена транзакции
```bash
curl -X PATCH http://localhost:8080/transactions/tx1/cancel \
  -H "Content-Type: application/json" \
  -d '{"reason": "Возврат билета"}'
```

## Настройка базы данных

Перед запуском необходимо настроить подключение к PostgreSQL в файле `application.conf`:

```conf
persistence {
  dataSource {
    url = "jdbc:postgresql://localhost:5432/loyalty"
    user = "username"
    password = "password"
  }
}
```

И выполнить SQL скрипты для создания таблиц (см. `V1_init.sql` и `V1_dict.sql`).