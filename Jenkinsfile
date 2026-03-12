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
                sh 'docker build --no-cache -t docker-java-app:app .'
            }
        }

        stage('Docker Run') {
    steps {
        withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY')]) {
            sh '''
                echo "Secret file path inside Jenkins container: $GCP_KEY"
                cp $GCP_KEY ./gcp-key.json   # copy to workspace

                docker run --rm \
                  -e GOOGLE_APPLICATION_CREDENTIALS=/app/gcp-key.json \
                  -v $(pwd)/gcp-key.json:/app/gcp-key.json:ro \
                  docker-java-app:app
            '''
        }
    }
}
    }
}
