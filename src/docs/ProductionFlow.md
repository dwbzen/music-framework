## ProductionFlow
The ProductionFlow class executes a workflow that produces a [musicXML](https://github.com/w3c/musicxml) file from random or fractal data (in JSON format).
The resulting musicXML file can be imported into music composition software such as [MuseScore](https://musescore.org/en), Sibelius or Finale.
ProductionFlow is driven primarily by configuration, in particular config.properties, orchestra.properties, and mongo.properties.
See the javadoc for a description of command line options.

**config.properties**

Overall score configuration including data sources for individual instruments are set in config.properties.
The scale(s) used to create the score is also configured for each instrument through the score.transformers.<instrument> parameter.
For example,

```Java
score.transformers.DoubleBass=org.dwbzen.music.transform.ScaleTransformer
score.transformers.DoubleBass.ScaleTransformer.scale=Hirajoshi Japan
score.transformers.DoubleBass.ScaleTransformer.root=A
score.transformers.DoubleBass.ScaleTransformer.preference=random
```

Other parameters include the following. Instruments are defined in orchestra.properties.
* score.instruments - list of instruments to score for
* score.title - this will be the title in score
* score.parts.<instrument>.partName - the part name to use for <instrument>. This also the assigned queue name for the instrument
* score.tempo - sets the initial tempo. Default is quarter note = 80
* score.key - key signature, default value is C-Major
* score.timeSignature - time signature, default is 4/4

There are many additional parameters for Apache MQ, MongoDB and score production. These are described in config.properties.

**orchestra.properties**

This describes all the available instruments which includes the following. This info is needed for musicXML file production.
* java class, for example `org.dwbzen.music.instrument.Violin`
* midi program to use for playback
* optional virtual-library for use with Sibelius

**mongo.properties**

MongoDB configuration. The existing parameters will work for a typical [MongoDB](https://www.mongodb.com/) installation.

