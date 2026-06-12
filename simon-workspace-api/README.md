# Simon Workspace API

Spring Boot API service for Simon Workspace.

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
docker build -t simon-workspace-api:local .
docker run --rm -p 8080:8080 simon-workspace-api:local
```
