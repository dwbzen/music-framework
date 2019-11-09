# music-framework

A Java framework encapsulating musical structure, notation and theory for purposes of composition and analysis.

https://github.com/dwbzen/music-framework

git init
git add README.md
git commit -m "first commit"
git remote add origin https://github.com/dwbzen/music-framework.git
git push -u origin master

## Build instructions
### commonlib
* gradlew build uploadArchives
* gradlew sonarqube (optional, specify build.gradle.sonarqube as the build file)

### music-framework
* gradlew build uploadArchives
* gradlew sonarqube (optional, specify build.gradle.sonarqube as the build file)
