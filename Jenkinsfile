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
                          -e GOOGLE_APPLICATION_CREDENTIALS=/credentials/service-account.json ^
                          -v "%GCP_KEY%\\kafka-pipeline-project-136237569977.json:/credentials/service-account.json:ro" ^
                          docker-java-app:app ^
                          java -jar app.jar
                    '''
                }
            }
        }
        stage('Docker Debug') {
            steps {
                withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY')]) {
                    bat '''
                        docker run --rm ^
                          -v "%GCP_KEY%\\kafka-pipeline-project-136237569977.json:/credentials/service-account.json:ro" ^
                          docker-java-app:app ^
                          ls -l /credentials
                    '''
                }
            }
        }
    }
}
