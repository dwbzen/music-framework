# music-framework

A Java framework encapsulating musical structure, notation and theory for purposes of composition and analysis.

GitHub project: [music-framework](https://github.com/dwbzen/music-framework)

## Build instructions
### Github Projects
* Clone the latest [commonlib](https://github.com/dwbzen/commonlib) and [music-framework](https://github.com/dwbzen/music-framework) repos from Github
    * Recommend cloning into C:\Compile along with commonlib and text-processing projects
    
### commonlib
* gradlew build uploadArchives

### music-framework
* gradlew build uploadArchives

## Required Components
* MongoDB - download and install the latest version (4.4.0) of the [MongoDB Community Server](https://www.mongodb.com/try/download/community)
* Apache ActiveMQ - download and install [ActiveMQ5](http://activemq.apache.org/components/classic/download/). To install as a Widows service, run "InstallWindowsService.bat" in <install folder>/bin/win64.

## eclipse project setup

* Download and install the version of the JDK referenced in build.gradle
* Download and install latest eclipse Java IDE (2020-03)
* Spin up eclipse and add the JDK for Java 14 under Installed JREs, and make it the default
* Import the commonlib gradle project and build, then
* Import the music-framework gradle project and build

## Reference Data
Music reference files are in the resources/data/music and resources/data/music/songs folders.
These consist of chords, scales and songs all in JSON format. 
Briefly here is a catalog of the contents:
### Scales and Scale Formulas

### Chords and Chord Formulas
There are currently 57 cord formulas. See chords.md for details. Two different JSON formats are used.
The JSON files with underscores in the name are for importing into MongoDB. Each line (or top-level element)
is valid JSON, but the file as a whole is not. 
The other files can be deserialized/serialized by Java classes.
These can also be imported into Mathematica arrays.
* allChordFormulas.json - org.dwbzen.music.element.song.ChordFormulas class
* chord_formulas.json - all the chord formulas sorted by name. Can be imported into MongoDB
* chord_formulas_import.json - all the chord formulas sorted by group. This can also be imported into MongoDB.
* allChords.json - org.dwbzen.music.element.song.ChordLibrary class. This has the spellings of all the chord formulas in all keys.
* all_chords.json - same content as allChords.json. Structure is a JSON array with the chord root as the key.

### Rhythm Scales

### Songs

### Other


