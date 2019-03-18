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
                bat 'gradlew.bat -b build.gradle.sonarqube clean'
                bat 'gradlew.bat -b build.gradle.sonarqube build'
                bat 'gradlew.bat -b build.gradle.sonarqube test'
            }
        }
        stage('SonarQube analysis') {
            // requires SonarQube Scanner for Gradle 2.1+
            // It's important to add --info because of SONARJNKNS-281
            steps {
				echo 'Running code analysis'
                bat 'gradlew.bat -b build.gradle.sonarqube sonarqube'
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
