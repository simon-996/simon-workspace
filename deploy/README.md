# Jenkins Remote Deployment

This deployment path is for a Jenkins server that builds the project, uploads the built artifacts to an application server, and runs Docker Compose on that application server.

The application server does not need Maven or Node.js. It only needs Docker, Docker Compose, SSH access, and the production `.env` file.

## Application Server Layout

Jenkins deploys to `/apps/simon-workspace` by default:

```text
/apps/simon-workspace/
├── api/
│   ├── Dockerfile
│   └── app.jar
├── web/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── dist/
└── deploy/
    ├── .env
    ├── .env.example
    ├── docker-compose.yml
    └── deploy.sh
```

## First-Time Application Server Setup

Create the directory:

```bash
mkdir -p /apps/simon-workspace/deploy
```

Install Docker and confirm Compose works:

```bash
docker version
docker compose version
```

Create the production environment file:

```bash
cp /apps/simon-workspace/deploy/.env.example /apps/simon-workspace/deploy/.env
```

If `.env.example` is not on the server yet, copy it from this repository first or let Jenkins run once, then edit:

```bash
vim /apps/simon-workspace/deploy/.env
```

The `.env` file is not overwritten by Jenkins. Keep real MySQL, Redis, storage, and CORS values there.

## Jenkins Requirements

Install these Jenkins plugins:

- Pipeline
- Git
- SSH Agent

Configure these tools on the Jenkins agent:

- JDK 17
- Maven 3.9+
- Node.js 22+
- npm
- tar, ssh, scp

Add an SSH private-key credential that can log in to the application server. The default credential id used by the pipeline is:

```text
simon-workspace-app-server
```

## Jenkins Job

Create a Pipeline job:

1. New Item -> Pipeline.
2. Choose "Pipeline script from SCM".
3. SCM: Git.
4. Repository URL: this repository.
5. Credentials: Git credentials if the repository is private.
6. Branch: `*/master`.
7. Script Path: `Jenkinsfile`.
8. Save.

The first build will show parameters. Fill:

```text
DEPLOY_TARGET=all
APP_SERVER_HOST=<your application server ip or domain>
APP_SERVER_USER=root
APP_SERVER_DIR=/apps/simon-workspace
SSH_CREDENTIALS_ID=simon-workspace-app-server
VITE_API_BASE_URL=https://api.simon996.com/api
RUN_TESTS=true
```

After the first successful deployment, use:

```text
DEPLOY_TARGET=api
```

to deploy only the backend, or:

```text
DEPLOY_TARGET=web
```

to deploy only the frontend.

## Manual Server Deployment

If the bundle is already on the application server, you can run:

```bash
/apps/simon-workspace/deploy/deploy.sh all
/apps/simon-workspace/deploy/deploy.sh api
/apps/simon-workspace/deploy/deploy.sh web
```

`api` and `web` use `docker compose up -d --build --no-deps` so the other service is not restarted.
