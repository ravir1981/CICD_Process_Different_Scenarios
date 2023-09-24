pipeline {
    agent any
    tools{
        maven 'maven-3.9.4'
    }
    environment {
        registry = '840667225127.dkr.ecr.us-east-1.amazonaws.com/hellojavarepo'
        registryCredential = 'jenkins-ecr-login-credential'
        dockerimage = ''
    }
    stages{
        stage("Checkout the project") {
           steps{
               git branch: 'master', url: 'https://github.com/ravir1981/springboot-maven-micro.git'
           } 
        }
        stage("Build the package"){
            steps {
                sh 'mvn clean package'
            }
        }
        stage("Sonar Quality Check"){
		    steps{
		        script{
		            withSonarQubeEnv(installationName: 'sonar-9', credentialsId: 'jenkins-sonar') {
		                sh 'mvn sonar:sonar'
                    }
                    timeout(time: 1, unit: 'HOURS') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
	    	        }
	    	    }
            }
        }
        stage('Building the Image') {
            steps {
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                }
            }
        }
        stage ('Deploy the Image to Amazon ECR') {
            steps {
                script {
                    docker.withRegistry("http://" + registry, "ecr:us-east-1:" + registryCredential ) {
                        dockerImage.push()
                    }
                }
            }
        }
    }
    post {
        success {
            mail bcc: '', body: 'Pipeline build successfully', cc: '', from: 'ravir81@gmail.com', replyTo: '', subject: 'The Pipeline success', to: 'ravir81@gmail.com'
        }
        failure {  
            mail bcc: '', body: 'Pipeline build not success', cc: '', from: 'ravir81@gmail.com', replyTo: '', subject: 'The Pipeline failed', to: 'ravir81@gmail.com'
         } 
    }
}