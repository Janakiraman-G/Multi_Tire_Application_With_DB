pipeline {
    agent any
    
    tools {
        maven 'maven3'
        jdk 'jdk17'
    }
    
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
    }

    stages {
        stage('Git CheckOut') {
            steps {
                git branch: 'main', url: 'https://github.com/Janakiraman-G/Multi_Tire_Application_With_DB.git'
            }
        }
        stage('compile') {
            steps {
                sh "mvn compile"
            }
        }
        stage('Test') {
            steps {
                sh "mvn test"
            }
        }
        stage('Trivy') {
            steps {
                sh "trivy fs --format table -o fs.html ."
            }
        }
        stage('SonarQube') {
            steps {
                withSonarQubeEnv('sonar') {
                    sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectKey=bankapp1 -Dsonar.projectName=bankapp1 \
                        -Dsonar.java.binaries=target'''
                }
            }
        }
        stage('Build & Publish to Nexus') {
            steps {
                withMaven(globalMavenSettingsConfig: 'DevSecOps-janakiraman', maven: 'maven3', traceability: true) {
                    sh 'mvn deploy'
                    }
            }
        }
        stage('Building_Docker_image') {
            steps {
                script {
                withDockerRegistry(credentialsId: 'docker-cred') {
                    sh "docker build -t jabhuvan/bankapp1:latest ."
                    }
                }    
            }
        }
        stage('Trivy image scane') {
            steps {
                sh "trivy image --format table -o image.html jabhuvan/bankapp1:latest"
            }
        }
        stage('Docker_push') {
            steps {
                script {
                withDockerRegistry(credentialsId: 'docker-cred') {
                    sh "docker push jabhuvan/bankapp1:latest"
                    }
                }
            }
        }
        stage('Deploy_TO_K8s') {
            steps {
                    withKubeConfig(caCertificate: '', clusterName: 'devopsshack-cluster', contextName: '', credentialsId: 'k8-token', namespace: 'webapps', restrictKubeConfigAccess: false, serverUrl: 'https://152CA4C983FBE474A07A1C37165EFF9D.gr7.us-east-1.eks.amazonaws.com') {
                        sh "kubectl apply -f ds.yaml -n webapps"
                        sleep 30
                    }
             }
         }
         stage('Verify Deployments') {
            steps {
                    withKubeConfig(caCertificate: '', clusterName: 'devopsshack-cluster', contextName: '', credentialsId: 'k8-token', namespace: 'webapps', restrictKubeConfigAccess: false, serverUrl: 'https://152CA4C983FBE474A07A1C37165EFF9D.gr7.us-east-1.eks.amazonaws.com') {
                        sh "kubectl get svc -n webapps"
                    }
             }
         }
    }
}
