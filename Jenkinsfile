pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Docker Build') {
            steps {
                sh 'docker build -t docker-java-app:app .'
            }
        }
     stage('Docker Run') {
    steps {
        withCredentials([file(credentialsId: 'gcp-key', variable: 'GOOGLE_APPLICATION_CREDENTIALS')]) {
            sh '''
                docker run --rm \
                -e GOOGLE_APPLICATION_CREDENTIALS=/app/key.json \
                -v ${GOOGLE_APPLICATION_CREDENTIALS}:/app/key.json:ro \
                docker-java-app:app
            '''
        }
    }
}
    }
}
