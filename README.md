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

## Resource Links
The source for all the scales (common and theoretical) is the website [2384 Unique Musical Scales](http://www.harmonics.com/scales/index.html).
Scales in a variety of different formats can be downloaded from that site. I used the [ScaleCoding2011.xls](http://www.lucytune.com/scales/ScaleCodingJan2011.xls) spreadsheet as the primary source for scales.
It also has links to other sites and is a great source of musical theory info on scales, chords and modes.
In particular [LucyTuning](http://www.lucytune.com/new_to_lt/pitch_05.html) is a great resource.
Article copyright - Charles E. H. Lucy, visit [LucyTuning](http://www.lucytune.com), [Harmonics.com](http://www.harmonics.com/lucy/) and [lullabies](http://www.lullabies.co.uk) for more original content like this.

The above sites reference 12edo which stands for 12 equal divisions of the octave a.k.a. equal temperament or 12et which is the predominant
tuning system used today. For more information on this topic, see the [Equal temperament](https://en.wikipedia.org/wiki/Equal_temperament) Wikipedia article.

My contribution was to create Java/JSON structures and a MongoDB from this raw data and then using this data to create musical scores from fractal data.

## ProductionFlow
The ProductionFlow class executes a workflow that produces a [musicXML](https://github.com/w3c/musicxml) file from random or fractal data (in JSON format).
The resulting musicXML file can be imported into music composition software such as [MuseScore](https://musescore.org/en), Sibelius or Finale.
ProductionFlow is driven primarily by configuration, in particular config.properties, orchestra.properties, and mongo.properties.
See the javadoc for a description of command line options.

### config.properties
Overall score configuration including data sources for individual instruments are set in config.properties.
The scale(s) used to create the score is also configured for each instrument through the score.transformers.<instrument> parameter.
For example,

`score.transformers.DoubleBass=org.dwbzen.music.transform.ScaleTransformer`

`score.transformers.DoubleBass.ScaleTransformer.scale=Hirajoshi Japan`

`score.transformers.DoubleBass.ScaleTransformer.root=A`

`score.transformers.DoubleBass.ScaleTransformer.preference=random`
Other parameters include the following. Instruments are defined in orchestra.properties.
* score.instruments - list of instruments to score for
* score.title - this will be the title in score
* score.parts.<instrument>.partName - the part name to use for <instrument>. This also the assigned queue name for the instrument
* score.tempo - sets the initial tempo. Default is quarter note = 80
* score.key - key signature, default value is C-Major
* score.timeSignature - time signature, default is 4/4

There are many additional parameters for Apache MQ, MongoDB and score production. These are described in config.properties.

### orchestra.properties
This describes all the available instruments which includes the following. This info is needed for musicXML file production.
* java class, for example `org.dwbzen.music.instrument.Violin`
* midi program to use for playback
* optional virtual-library for use with Sibelius

### mongo.properties
MongoDB configuration. The existing parameters will work for a typical [MongoDB](https://www.mongodb.com/) installation.






