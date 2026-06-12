pipeline {
  agent any

  environment {
    API_IMAGE = 'simon-workplace-api'
    WEB_IMAGE = 'simon-workplace-web'
    IMAGE_TAG = "${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('API Test and Package') {
      steps {
        sh 'mvn -f simon-workplace-api/pom.xml -B test package'
      }
    }

    stage('Web Install and Build') {
      steps {
        sh 'npm ci --prefix simon-workplace-web'
        sh 'npm run build --prefix simon-workplace-web'
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker build -t ${API_IMAGE}:${IMAGE_TAG} -t ${API_IMAGE}:latest simon-workplace-api'
        sh 'docker build -t ${WEB_IMAGE}:${IMAGE_TAG} -t ${WEB_IMAGE}:latest simon-workplace-web'
      }
    }

    stage('Deploy Placeholder') {
      steps {
        echo 'Configure this stage with registry push and remote docker compose deployment.'
        echo 'Example: docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'simon-workplace-api/target/*.jar,simon-workplace-web/dist/**', allowEmptyArchive: true
    }
  }
}
