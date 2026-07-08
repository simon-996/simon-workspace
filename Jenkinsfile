pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    choice(
      name: 'DEPLOY_TARGET',
      choices: ['all', 'api', 'web'],
      description: 'Choose all for first deployment, or api/web for a single-service deployment.'
    )
    string(name: 'APP_SERVER_HOST', defaultValue: '', description: 'Application server host, for example 1.2.3.4 or app.example.com.')
    string(name: 'APP_SERVER_USER', defaultValue: 'root', description: 'SSH user for the application server.')
    string(name: 'APP_SERVER_DIR', defaultValue: '/apps/simon-workspace', description: 'Deployment directory on the application server.')
    string(name: 'SSH_CREDENTIALS_ID', defaultValue: 'simon-workspace-app-server', description: 'Jenkins SSH private-key credential id.')
    string(name: 'VITE_API_BASE_URL', defaultValue: 'https://api.simon996.com/api', description: 'Frontend API base URL baked into the web dist.')
    booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run backend tests before packaging.')
  }

  environment {
    DEPLOY_ARCHIVE = "simon-workspace-${env.BUILD_NUMBER}.tgz"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Validate Parameters') {
      steps {
        sh '''
          set -eu
          test -n "${APP_SERVER_HOST}" || (echo "APP_SERVER_HOST is required" && exit 1)
          test -n "${APP_SERVER_USER}" || (echo "APP_SERVER_USER is required" && exit 1)
          test -n "${APP_SERVER_DIR}" || (echo "APP_SERVER_DIR is required" && exit 1)
          test -n "${SSH_CREDENTIALS_ID}" || (echo "SSH_CREDENTIALS_ID is required" && exit 1)
          test -n "${VITE_API_BASE_URL}" || (echo "VITE_API_BASE_URL is required" && exit 1)
        '''
      }
    }

    stage('Package API') {
      steps {
        sh '''
          set -eu
          if [ "${RUN_TESTS}" = "true" ]; then
            mvn -f simon-workspace-api/pom.xml -B package
          else
            mvn -f simon-workspace-api/pom.xml -B -DskipTests package
          fi
        '''
      }
    }

    stage('Build Web') {
      steps {
        sh '''
          set -eu
          npm ci --prefix simon-workspace-web
          VITE_API_BASE_URL="${VITE_API_BASE_URL}" npm run build --prefix simon-workspace-web
        '''
      }
    }

    stage('Create Deploy Bundle') {
      steps {
        sh '''
          set -eu
          rm -rf deploy-bundle "${DEPLOY_ARCHIVE}"
          mkdir -p deploy-bundle/api deploy-bundle/web deploy-bundle/deploy

          cp simon-workspace-api/target/simon-workspace-api-*.jar deploy-bundle/api/app.jar
          cp deploy/api.Dockerfile deploy-bundle/api/Dockerfile

          cp -R simon-workspace-web/dist deploy-bundle/web/dist
          cp simon-workspace-web/nginx.conf deploy-bundle/web/nginx.conf
          cp deploy/web.Dockerfile deploy-bundle/web/Dockerfile

          cp deploy/docker-compose.remote.yml deploy-bundle/deploy/docker-compose.yml
          cp deploy/deploy.sh deploy-bundle/deploy/deploy.sh
          cp deploy/.env.example deploy-bundle/deploy/.env.example

          tar -czf "${DEPLOY_ARCHIVE}" -C deploy-bundle .
        '''
      }
    }

    stage('Upload Bundle') {
      steps {
        sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
          sh '''
            set -eu
            ssh -o StrictHostKeyChecking=accept-new "${APP_SERVER_USER}@${APP_SERVER_HOST}" "mkdir -p '${APP_SERVER_DIR}' /tmp"
            scp "${DEPLOY_ARCHIVE}" "${APP_SERVER_USER}@${APP_SERVER_HOST}:/tmp/${DEPLOY_ARCHIVE}"
          '''
        }
      }
    }

    stage('Remote Deploy') {
      steps {
        sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
          sh '''
            set -eu
            ssh "${APP_SERVER_USER}@${APP_SERVER_HOST}" "APP_DIR='${APP_SERVER_DIR}' ARCHIVE='/tmp/${DEPLOY_ARCHIVE}' TARGET='${DEPLOY_TARGET}' bash -s" <<'REMOTE_SCRIPT'
              set -eu
              mkdir -p "$APP_DIR/deploy"
              ENV_BACKUP=""
              if [ -f "$APP_DIR/deploy/.env" ]; then
                ENV_BACKUP="/tmp/simon-workspace.env.$$"
                cp "$APP_DIR/deploy/.env" "$ENV_BACKUP"
              fi

              rm -rf "$APP_DIR/api" "$APP_DIR/web"
              mkdir -p "$APP_DIR"
              tar -xzf "$ARCHIVE" -C "$APP_DIR"

              if [ -n "$ENV_BACKUP" ] && [ -f "$ENV_BACKUP" ]; then
                mkdir -p "$APP_DIR/deploy"
                mv "$ENV_BACKUP" "$APP_DIR/deploy/.env"
              fi

              chmod +x "$APP_DIR/deploy/deploy.sh"
              "$APP_DIR/deploy/deploy.sh" "$TARGET"
              rm -f "$ARCHIVE"
REMOTE_SCRIPT
          '''
        }
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'simon-workspace-api/target/*.jar,simon-workspace-web/dist/**,*.tgz', allowEmptyArchive: true
    }
  }
}
