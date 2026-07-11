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

## Initialize an OWNER Account

After Flyway has applied the database migrations, edit the four variables at the top of:

```text
scripts/init-owner.sql
```

Run it against the target database:

```bash
mysql -h <host> -P <port> -u <user> -p <database> < scripts/init-owner.sql
```

The script creates a new enabled user and grants the `OWNER` role. Existing OWNER accounts do not block creating another OWNER, but an existing username is never modified or granted additional privileges.

Expected result:

```text
OWNER_CREATED
```

Immediately restore the placeholders after execution so the real password cannot be committed:

```bash
git restore scripts/init-owner.sql
```

Other result codes are `CONFIG_REQUIRED`, `OWNER_ROLE_MISSING`, `USERNAME_EXISTS`, and `OWNER_NOT_CREATED`.

Health check:

```text
GET /api/health
```

## Docker

```bash
docker build -t simon-workspace-api:local .
docker run --rm -p 8080:8080 simon-workspace-api:local
```
