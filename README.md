# CrptApi

CrptApi — это библиотека на языке Java для взаимодействия с API системы мониторинга, предоставляемой CRPT (Центр Развития Перспективных Технологий). Библиотека поддерживает управление запросами с ограничением частоты запросов (rate limiting) и автоматической обработкой ошибок.



## Функциональность
CrptApi предоставляет интерфейс для взаимодействия с API системы мониторинга CRPT. Основные функции включают:

- Создание документов: Метод createDocument отправляет запрос на создание документа в API CRPT.
- Управление частотой запросов: Реализован механизм ограничения частоты запросов.
- Обработка ошибок: Автоматическая обработка ошибок с использованием десериализации JSON ответов от API CRPT.

## Используемое API
CrptApi взаимодействует с API системы мониторинга CRPT по следующему URL:
```
https://ismp.crpt.ru/api/v3/lk/documents
```


## Особенности

- Поддержка ограничений на количество запросов (rate limiting).
- Автоматическая обработка ошибок с использованием механизма исключений.
- Использование современных возможностей Java 17, таких как records и HttpClient.
- Простая и удобная сериализация и десериализация JSON с использованием Jackson ObjectMapper.

## Требования

- Java 17 или выше
- Maven (для сборки проекта)

## Установка

Для установки библиотеки добавьте следующую зависимость в ваш `pom.xml`:

```xml
<dependency>
    <groupId>com.kstrinadka</groupId>
    <artifactId>crptapi</artifactId>
    <version>1.0.0</version>
</dependency>
```


## Модели данных 

### DocumentRequest
```json
{
  "signature": "example_signature",
  "document": {
    "description": {
      "participantInn": "1234567890"
    },
    "docId": "docId",
    "docStatus": "docStatus",
    "docType": "docType",
    "importRequest": true,
    "ownerInn": "ownerInn",
    "participantInn": "participantInn",
    "producerInn": "producerInn",
    "productionDate": "productionDate",
    "productionType": "productionType",
    "products": [
      {
        "certificateDocument": "certDoc",
        "certificateDocumentDate": "certDate",
        "certificateDocumentNumber": "certNum",
        "ownerInn": "ownerInn",
        "producerInn": "prodInn",
        "productionDate": "prodDate",
        "tnvedCode": "tnved",
        "uitCode": "uit",
        "uituCode": "uitu"
      }
    ],
    "regDate": "regDate",
    "regNumber": "regNumber"
  }
}

```

### Document
```json
{
  "description": {
    "participantInn": "1234567890"
  },
  "docId": "docId",
  "docStatus": "docStatus",
  "docType": "docType",
  "importRequest": true,
  "ownerInn": "ownerInn",
  "participantInn": "participantInn",
  "producerInn": "producerInn",
  "productionDate": "productionDate",
  "productionType": "productionType",
  "products": [
    {
      "certificateDocument": "certDoc",
      "certificateDocumentDate": "certDate",
      "certificateDocumentNumber": "certNum",
      "ownerInn": "ownerInn",
      "producerInn": "prodInn",
      "productionDate": "prodDate",
      "tnvedCode": "tnved",
      "uitCode": "uit",
      "uituCode": "uitu"
    }
  ],
  "regDate": "regDate",
  "regNumber": "regNumber"
}

```

### Description
```json
{
  "participantInn": "1234567890"
}

```

### Product
```json
{
  "certificateDocument": "certDoc",
  "certificateDocumentDate": "certDate",
  "certificateDocumentNumber": "certNum",
  "ownerInn": "ownerInn",
  "producerInn": "prodInn",
  "productionDate": "prodDate",
  "tnvedCode": "tnved",
  "uitCode": "uit",
  "uituCode": "uitu"
}

```

### ErrorResponse
```json
{
  "code": "code",
  "error": "error",
  "message": "message"
}
```

