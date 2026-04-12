pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk   'JDK21'
    }

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO_NAME     = 'shopping-cart-localization'

        DOCKER_IMAGE_TAG        = "build-${env.BUILD_NUMBER}"
        DOCKER_IMAGE_TAG_LATEST = 'latest'

        SONAR_PROJECT_KEY       = 'shopping-cart-localization'
        SONAR_PROJECT_NAME      = 'shopping cart localization'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat 'mvn -B clean verify'
            }

            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat """
                        ${tool 'SonarScanner'}\\bin\\sonar-scanner ^
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} ^
                        -Dsonar.projectName="${SONAR_PROJECT_NAME}" ^
                        -Dsonar.sources=src/main/java ^
                        -Dsonar.tests=src/test/java ^
                        -Dsonar.java.binaries=target/classes ^
                        -Dsonar.java.test.binaries=target/test-classes ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml ^
                        -Dsonar.sourceEncoding=UTF-8
                    """
                }
            }
        }

        /*
        OPTIONAL
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        */

        stage('Build Docker Image') {
            steps {
                bat """
                    docker build ^
                        -t %DOCKER_USERNAME%/%DOCKERHUB_REPO_NAME%:%DOCKER_IMAGE_TAG% ^
                        -t %DOCKER_USERNAME%/%DOCKERHUB_REPO_NAME%:%DOCKER_IMAGE_TAG_LATEST% .
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
                        docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                        docker push %DOCKER_USERNAME%/%DOCKERHUB_REPO_NAME%:%DOCKER_IMAGE_TAG%
                        docker push %DOCKER_USERNAME%/%DOCKERHUB_REPO_NAME%:%DOCKER_IMAGE_TAG_LATEST%
                        docker logout
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded! Image: ${DOCKER_USERNAME}/${DOCKERHUB_REPO_NAME}:${DOCKER_IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed. Check logs above."
        }
        always {
            cleanWs()
        }
    }
}