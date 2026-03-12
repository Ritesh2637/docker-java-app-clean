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
                // Fresh rebuild without cache
                sh 'docker build --no-cache -t docker-java-app:app .'
            }
        }

       stage('Docker Run') {
    steps {
        withCredentials([file(credentialsId: 'gcp-key', variable: 'GCP_KEY')]) {
            sh '''
                echo "Secret file path on host: $GCP_KEY"
                ls -l $GCP_KEY

                # Mount the actual secret file correctly
                docker run --rm \
                  -e GOOGLE_APPLICATION_CREDENTIALS=/app/key.json \
                  -v ${GCP_KEY}:/app/key.json:ro \
                  docker-java-app:app
            '''
        }
    }
}
    }
}
