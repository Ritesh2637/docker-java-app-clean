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

                        # Mount the secret file directly as /gcp-key.json
                        docker run --rm \
                          -e GOOGLE_APPLICATION_CREDENTIALS=/gcp-key.json \
                          -v ${GCP_KEY}:/gcp-key.json:ro \
                          docker-java-app:app
                    '''
                }
            }
        }
    }
}
