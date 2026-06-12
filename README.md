# Simon Workplace

Simon Workplace is a personal website and AI teaching workspace monorepo.

It contains:

- `simon-workplace-api`: Spring Boot API service.
- `simon-workplace-web`: Vue 3 frontend.
- `deploy`: Docker Compose, Nginx, database, and deployment configuration.
- `simon-workplace-doc.md`: product and architecture planning document.

## Development

Backend:

```bash
mvn -f simon-workplace-api/pom.xml test
```

Frontend:

```bash
npm install --prefix simon-workplace-web
npm run build --prefix simon-workplace-web
```

## Deployment Direction

The project is organized as one Git repository with separate frontend and backend applications. Jenkins will build both apps, create Docker images, and deploy them with Docker Compose.
