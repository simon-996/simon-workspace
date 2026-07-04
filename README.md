# Simon Workspace

Simon Workspace is a personal homepage and teaching workspace monorepo.

访客看到的是简洁的个人主页；授权账号登录后进入课程、班级、学期、模板、文件、生成记录、权限和站点配置工作台。

## Structure

- `simon-workspace-api`: Spring Boot API.
- `simon-workspace-web`: Vue 3 + Vite frontend.
- `deploy`: Docker, Nginx, and deployment config.
- `docs/design-style.md`: current visual style baseline.
- `docs/roadmap`: phase Todo documents.
- `simon-workspace-doc.md`: product and architecture summary.

## Development

Backend tests:

```bash
mvn -f simon-workspace-api/pom.xml test
```

Frontend build:

```bash
npm install --prefix simon-workspace-web
npm run build --prefix simon-workspace-web
```

## Runtime Config

The API uses Flyway for database migrations. For local dev, use `application-dev.yml`. For production, provide MySQL and Redis through environment variables:

```text
MYSQL_URL
MYSQL_USERNAME
MYSQL_PASSWORD
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
FLYWAY_ENABLED
```

The frontend reads the backend address from:

```text
VITE_API_BASE_URL
```

## Deployment Direction

The project is one Git repository with separate frontend and backend applications. Current production port plan:

```text
Web: 9526
API: 9527
```

MySQL and Redis are remote services and are not deployed by Docker Compose.

Create an environment file:

```bash
cp deploy/.env.example deploy/.env
```

Start:

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
```

Services:

- Web: `http://localhost:9526`
- API: `http://localhost:9527/api/health`
- MySQL: remote service configured by `MYSQL_URL`
- Redis: remote service configured by `REDIS_HOST`, `REDIS_PORT`, and `REDIS_PASSWORD`

## Documentation

Use these first:

- [Design style](docs/design-style.md)
- [Roadmap overview](docs/roadmap/00-overview.md)
- [Product summary](simon-workspace-doc.md)
