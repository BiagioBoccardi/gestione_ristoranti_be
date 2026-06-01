pipeline {
    agent any

    environment {
        FRONTEND_DIR   = 'frontend'
        IMAGE_BACKEND  = "biagioboccardi/gestione-ristorante-backend"
        IMAGE_FRONTEND = "biagioboccardi/gestione-ristorante-frontend"
    }

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ── 1. CHECKOUT (Git) ─────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.GIT_BRANCH} — Commit: ${env.GIT_COMMIT?.take(8)}"
            }
        }

        // ── 2. BUILD & TEST BACKEND ───────────────────────────────────────────
        stage('Build & Test Backend') {
            steps {
                sh 'mvn clean package -B'
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // ── 3. SECURITY SCAN (Snyk) ───────────────────────────────────────────
        stage('Security Scan (Snyk)') {
            steps {
                withCredentials([string(credentialsId: 'snyk-token', variable: 'SNYK_TOKEN')]) {
                    sh '''
                        snyk auth "$SNYK_TOKEN"
                        snyk test --severity-threshold=high --file=pom.xml \
                                  --project-name=gestione-ristorante-backend || true
                    '''
                    dir(env.FRONTEND_DIR) {
                        sh '''
                            snyk test --severity-threshold=high \
                                      --project-name=gestione-ristorante-frontend || true
                        '''
                    }
                }
            }
        }

        // ── 4. SONARCLOUD ANALYSIS ────────────────────────────────────────────
        stage('SonarCloud Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -B -DskipTests'
                }
            }
        }

        stage('SonarCloud Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        // ── 5. BUILD & TEST FRONTEND ──────────────────────────────────────────
        stage('Build & Test Frontend') {
            steps {
                dir(env.FRONTEND_DIR) {
                    sh 'npm ci --prefer-offline'
                    sh 'npm test -- --run'
                    sh 'npm run build'
                }
            }
        }

        // ── 6. DOCKER BUILD ───────────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                withCredentials([
                    string(credentialsId: 'postgres-user',     variable: 'POSTGRES_USER'),
                    string(credentialsId: 'postgres-password', variable: 'POSTGRES_PASSWORD'),
                    string(credentialsId: 'jwt-secret',        variable: 'JWT_SECRET'),
                    string(credentialsId: 'mail-password',     variable: 'MAIL_PASSWORD')
                ]) {
                    sh 'docker compose build --pull'
                }
            }
        }

        // ── 7. PUSH TO DOCKER HUB ─────────────────────────────────────────────
        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                        docker tag gestione_ristoranti_be-backend:latest \
                                   "$DOCKER_USER/gestione-ristorante-backend:latest"
                        docker tag gestione_ristoranti_be-backend:latest \
                                   "$DOCKER_USER/gestione-ristorante-backend:${BUILD_NUMBER}"
                        docker push "$DOCKER_USER/gestione-ristorante-backend:latest"
                        docker push "$DOCKER_USER/gestione-ristorante-backend:${BUILD_NUMBER}"

                        docker tag gestione_ristoranti_be-frontend:latest \
                                   "$DOCKER_USER/gestione-ristorante-frontend:latest"
                        docker tag gestione_ristoranti_be-frontend:latest \
                                   "$DOCKER_USER/gestione-ristorante-frontend:${BUILD_NUMBER}"
                        docker push "$DOCKER_USER/gestione-ristorante-frontend:latest"
                        docker push "$DOCKER_USER/gestione-ristorante-frontend:${BUILD_NUMBER}"

                        docker logout
                    '''
                }
            }
        }

        // ── 8. DEPLOY ─────────────────────────────────────────────────────────
        stage('Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'postgres-user',     variable: 'POSTGRES_USER'),
                    string(credentialsId: 'postgres-password', variable: 'POSTGRES_PASSWORD'),
                    string(credentialsId: 'jwt-secret',        variable: 'JWT_SECRET'),
                    string(credentialsId: 'mail-password',     variable: 'MAIL_PASSWORD')
                ]) {
                    sh 'docker compose up -d --remove-orphans'
                }
            }
        }

        // ── 9. SMOKE TEST ─────────────────────────────────────────────────────
        stage('Smoke Test') {
            steps {
                sh '''
                    echo "Attesa avvio servizi (30s)..."
                    sleep 30

                    FE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/)
                    [ "$FE" = "200" ] || { echo "Frontend non risponde: $FE"; exit 1; }
                    echo "Frontend OK ($FE)"

                    API=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/menu/categorie)
                    { [ "$API" = "200" ] || [ "$API" = "401" ]; } || { echo "API non risponde: $API"; exit 1; }
                    echo "API OK ($API)"

                    HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/api/actuator/health)
                    [ "$HEALTH" = "200" ] || { echo "Health endpoint non risponde: $HEALTH"; exit 1; }
                    echo "Health OK ($HEALTH)"
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completata — build #${BUILD_NUMBER} pubblicata su Docker Hub"
        }
        failure {
            echo 'Pipeline fallita — ultimi log dei container:'
            sh 'docker compose logs --tail=80 || true'
        }
        cleanup {
            cleanWs()
        }
    }
}
