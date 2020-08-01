pipeline {
    agent any
    stages {
        stage('SCM') {
            steps {
                 git 'https://github.com/dwbzen/music-framework.git'
            }
        }
        stage('Build') {
            steps {
				echo 'Building'
                bat 'gradlew.bat -b build.gradle clean'
                bat 'gradlew.bat -b build.gradle build'
                bat 'gradlew.bat -b build.gradle test'
            }
        }
        stage('Deploy') {
            steps {
				echo 'Deploying'
                bat 'gradlew.bat uploadArchives'
            }
        }
    }
}
