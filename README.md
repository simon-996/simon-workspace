# Simon Workspace

Simon Workspace is a personal website and AI teaching workspace monorepo.

It contains:

- `simon-workspace-api`: Spring Boot API service.
- `simon-workspace-web`: Vue 3 frontend.
- `deploy`: Docker Compose, Nginx, database, and deployment configuration.
- `simon-workspace-doc.md`: product and architecture planning document.

## Development

Backend:

```bash
mvn -f simon-workspace-api/pom.xml test
```

The API uses Flyway for database migrations. When running the API outside tests, provide a MySQL database through these environment variables:

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
FLYWAY_ENABLED
```

Frontend:

```bash
npm install --prefix simon-workspace-web
npm run build --prefix simon-workspace-web
```

## Deployment Direction

The project is organized as one Git repository with separate frontend and backend applications. Jenkins will build both apps, create Docker images, and deploy them with Docker Compose.

## Docker Compose

Create an environment file from the example:

```bash
cp deploy/.env.example deploy/.env
```

Start the local deployment stack:

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
```

Services:

- Web: `http://localhost`
- API: `http://localhost:8080/api/health`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- MinIO console: `http://localhost:9001`
