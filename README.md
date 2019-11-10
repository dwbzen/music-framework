# music-framework

A Java framework encapsulating musical structure, notation and theory for purposes of composition and analysis.

https://github.com/dwbzen/music-framework

## Build instructions
### commonlib
* gradlew build uploadArchives
* gradlew sonarqube (optional, specify build.gradle.sonarqube as the build file)

### music-framework
* gradlew build uploadArchives
* gradlew sonarqube (optional, specify build.gradle.sonarqube as the build file)

## eclipse project setup
* Clone the latest [commonlib](https://github.com/dwbzen/commonlib) and [music-framework](https://github.com/dwbzen/music-framework) repos from Github
    * Recommend cloning in C:\Compile along with commonlib and text-processing projects
* Download and install the version of the JDK referenced in build.gradle (Java 13)
* Download and install latest eclipse Java IDE (2019-09)
* Spin up eclipse and add the JDK for Java 13 under Installed JREs, and make it the default
* Import the text-processing gradle project