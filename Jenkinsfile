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
                // Run container and print output
                sh 'docker run --rm docker-java-app:app'
            }
        }
    }
}
