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
                bat 'mvn clean package -DskipTests'
            }
        }
        stage('Docker Build') {
            steps {
                bat 'docker build -t docker-java-app:app .'
            }
        }
        stage('Docker Run') {
            steps {
                withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY')]) {
                    bat '''
                        echo Secret file path on host: %GCP_KEY%
                        dir %GCP_KEY%
                        docker run --rm ^
                          -e GOOGLE_APPLICATION_CREDENTIALS=/app/key.json ^
                          -v "%GCP_KEY%:/app/key.json:ro" ^
                          docker-java-app:app
                    '''
                }
            }
        }
    }
}
