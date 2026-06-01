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
                bat 'mvn clean package -B'
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
                    bat 'snyk auth %SNYK_TOKEN%'
                    bat 'snyk test --severity-threshold=high --file=pom.xml --project-name=gestione-ristorante-backend || exit 0'
                    dir(env.FRONTEND_DIR) {
                        bat 'snyk test --severity-threshold=high --project-name=gestione-ristorante-frontend || exit 0'
                    }
                }
            }
        }

        // ── 4. SONARCLOUD ANALYSIS ────────────────────────────────────────────
        stage('SonarCloud Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar -B -DskipTests'
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
                    bat 'npm ci --prefer-offline'
                    bat 'npm test -- --run'
                    bat 'npm run build'
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
                    bat 'docker compose build --pull'
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
                    powershell '''
                        $env:DOCKER_PASS | docker login -u $env:DOCKER_USER --password-stdin
                        docker tag gestione_ristoranti_be-backend:latest "$($env:DOCKER_USER)/gestione-ristorante-backend:latest"
                        docker tag gestione_ristoranti_be-backend:latest "$($env:DOCKER_USER)/gestione-ristorante-backend:$($env:BUILD_NUMBER)"
                        docker push "$($env:DOCKER_USER)/gestione-ristorante-backend:latest"
                        docker push "$($env:DOCKER_USER)/gestione-ristorante-backend:$($env:BUILD_NUMBER)"
                        docker tag gestione_ristoranti_be-frontend:latest "$($env:DOCKER_USER)/gestione-ristorante-frontend:latest"
                        docker tag gestione_ristoranti_be-frontend:latest "$($env:DOCKER_USER)/gestione-ristorante-frontend:$($env:BUILD_NUMBER)"
                        docker push "$($env:DOCKER_USER)/gestione-ristorante-frontend:latest"
                        docker push "$($env:DOCKER_USER)/gestione-ristorante-frontend:$($env:BUILD_NUMBER)"
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
                    bat 'docker compose up -d --remove-orphans'
                }
            }
        }

        // ── 9. SMOKE TEST ─────────────────────────────────────────────────────
        stage('Smoke Test') {
            steps {
                powershell '''
                    Write-Host "Attesa avvio servizi (30s)..."
                    Start-Sleep -Seconds 30

                    try {
                        $FE = (Invoke-WebRequest -Uri "http://localhost/" -UseBasicParsing).StatusCode
                    } catch {
                        $FE = $_.Exception.Response.StatusCode.value__
                    }
                    if ($FE -ne 200) { Write-Error "Frontend non risponde: $FE"; exit 1 }
                    Write-Host "Frontend OK ($FE)"

                    try {
                        $API = (Invoke-WebRequest -Uri "http://localhost/api/menu/categorie" -UseBasicParsing).StatusCode
                    } catch {
                        $API = $_.Exception.Response.StatusCode.value__
                    }
                    if ($API -ne 200 -and $API -ne 401) { Write-Error "API non risponde: $API"; exit 1 }
                    Write-Host "API OK ($API)"

                    try {
                        $HEALTH = (Invoke-WebRequest -Uri "http://localhost/api/actuator/health" -UseBasicParsing).StatusCode
                    } catch {
                        $HEALTH = $_.Exception.Response.StatusCode.value__
                    }
                    if ($HEALTH -ne 200) { Write-Error "Health endpoint non risponde: $HEALTH"; exit 1 }
                    Write-Host "Health OK ($HEALTH)"
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
            bat 'docker compose logs --tail=80 || exit 0'
        }
        cleanup {
            cleanWs()
        }
    }
}
