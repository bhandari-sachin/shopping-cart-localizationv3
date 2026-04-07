pipeline {
    agent any

    environment {
        // Ensure Jenkins can find Docker on Windows
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"

        // Docker Hub credentials (set in Jenkins credentials store)
        DOCKERHUB_CREDENTIALS_ID = '11de06b8-c29b-4e4c-bf92-2d6a8d92868e'

        // Docker image repository
        DOCKER_IMAGE_REPO = 'sachinbhandari/shopping-cart-localization'

        // Image tags
        DOCKER_IMAGE_TAG_LATEST = 'latest'
        DOCKER_IMAGE_TAG_BUILD  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Check Docker') {
            steps {
                bat 'docker --version'
                bat 'docker info'
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/bhandari-sachin/shopping-cart-localization.git'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Generate Coverage Report') {
            steps {
                bat 'mvn -B jacoco:report'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                recordCoverage(
                    tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                    id: 'jacoco',
                    name: 'JaCoCo Coverage',
                    sourceCodeRetention: 'EVERY_BUILD'
                )
            }
        }

        stage('Build Docker Image') {
            steps {
                bat """
                    docker build ^
                        -t %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG_BUILD% ^
                        -t %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% .
                """
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                        echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                        docker push %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG_BUILD%
                        docker push %DOCKER_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST%
                        docker logout
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished: ${currentBuild.currentResult}"
        }
    }
}