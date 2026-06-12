# Simon Workspace API

Spring Boot API service for Simon Workspace.

## Local Commands

```bash
mvn test
mvn spring-boot:run
```

`mvn test` uses the `test` profile and does not require a local MySQL instance. `mvn spring-boot:run` runs Flyway migrations on startup and requires MySQL connection variables:

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
FLYWAY_ENABLED
```

Migration files live in:

```text
src/main/resources/db/migration
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
