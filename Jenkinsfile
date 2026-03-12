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
        withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY')]) {
            sh '''
                echo "Secret file path on host: $GCP_KEY"
                ls -l $GCP_KEY
                docker run --rm \
               -e GOOGLE_APPLICATION_CREDENTIALS=/credentials/service-account.json \
               -v $GCP_KEY:/credentials/service-account.json:ro \
               docker-java-app:app \
               java -jar app.jar
            '''
        }
    }
}
    stage('Docker Debug') {
    steps {
        withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY')]) {
            sh '''
                docker run --rm \
                -v $GCP_KEY:/credentials/service-account.json:ro \
                docker-java-app:app \
                ls -l /credentials
            '''
        }
    }
}
}
}
