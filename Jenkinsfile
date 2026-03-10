pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // GitHub se code pull karega
                git branch: 'main',
                    url: 'https://github.com/riteshmishra/docker-java-app.git'
            }
        }

        stage('Build') {
            steps {
                // Maven build karega shaded JAR banane ke liye
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                // Docker image banayega
                sh 'docker build -t docker-java-app:app .'
            }
        }
    }
}