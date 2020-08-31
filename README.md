# music-framework

A Java framework encapsulating musical structure, notation and theory for purposes of composition and analysis.

GitHub project: [music-framework](https://github.com/dwbzen/music-framework)

## Build instructions
**Github Projects**

* Clone the latest [commonlib](https://github.com/dwbzen/commonlib) and [music-framework](https://github.com/dwbzen/music-framework) repos from Github
    * Recommend cloning into C:\Compile along with commonlib and text-processing projects
    
**commonlib**

* gradlew build uploadArchives

**music-framework**

* gradlew build uploadArchives

## Required Components
* MongoDB - download and install the latest version (4.4.0) of the [MongoDB Community Server](https://www.mongodb.com/try/download/community)
* Apache ActiveMQ - download and install [ActiveMQ5](http://activemq.apache.org/components/classic/download/). To install as a Widows service, run "InstallWindowsService.bat" in <install folder>/bin/win64.

## eclipse project setup

* Download and install the version of the JDK referenced in build.gradle
* Download and install latest eclipse Java IDE (2020-06)
* Start eclipse add the JDK for Java 14 under Installed JREs, and make it the default
* Import the commonlib gradle project and build, then
* Import the music-framework gradle project and build

# Additional Documentation
The src/docs folder contains documentation on the musical structures used and music generation.
* [ReferenceData](ReferenceData.md) - describes chords, scales, songs and other JSON reference data

* [Resources](Resources.md) - links to additional resources, and external resources and web sites that provided much of the raw data.

* [ProductionFlow](ProductionFlow.md) - running ```ProductionFlow``` to produce musicXML from fractal and random data.

* [Utilities](Utilities.md) - utilities for chord/scale maintenance, music analysis, and chord/melody production from Markov Chains.

