pipeline {
    agent any

    environment {
        SONAR_HOST_URL = "${env.SONAR_HOST_URL ?: 'http://sonarqube:9000'}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compilacion y Pruebas Unitarias (Surefire + JaCoCo)') {
            steps {
                sh 'mvn clean test jacoco:report -B'
            }
        }

        stage('Analisis de Calidad (SonarQube)') {
            steps {
                sh '''
                    if [ -n "${SONAR_TOKEN:-}" ]; then
                        mvn sonar:sonar -Dsonar.host.url="${SONAR_HOST_URL}" -Dsonar.token="${SONAR_TOKEN}" -B
                    else
                        echo "SONAR_TOKEN no definido; omitiendo envio a SonarQube"
                    fi
                '''
            }
        }

        stage('Seguridad SAST (Semgrep)') {
            steps {
                sh '''
                    mkdir -p reportes
                    semgrep scan --config auto --timeout 30 --json --output reportes/semgrep-backend.json .
                '''
            }
        }

        stage('Empaquetado JAR') {
            steps {
                sh 'mvn package -DskipTests -B'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/site/jacoco/**, target/surefire-reports/**, reportes/**', allowEmptyArchive: true
        }
    }
}
