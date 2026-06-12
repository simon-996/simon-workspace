# Simon Workplace API

Spring Boot API service for Simon Workplace.

## Local Commands

```bash
mvn test
mvn spring-boot:run
```

Health check:

```text
GET /api/health
```

## Docker

```bash
docker build -t simon-workplace-api:local .
docker run --rm -p 8080:8080 simon-workplace-api:local
```
