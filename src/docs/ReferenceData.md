# Reference Data
Music reference files are in the resources/data/music and resources/data/music/songs folders.
These consist of chords, scales and songs all in JSON format. 
Briefly here is a catalog of the contents:

**Scales and Scale Formulas**

There are 2182 scale formulas: 610 common (or named) scales, and 1574 so-called theoretical scales.
See [Scales.md](Scales.md) for detailed information, and [Resources.md](Resources.md) for links to source data.

**Chords and Chord Formulas**

There are currently 57 cord formulas. See [Chords.md](Chords.md) for detailed field descriptions. 
Two different JSON formats are used.
The JSON files with underscores in the name are for importing into MongoDB. 
Each line (or top-level element) is valid JSON, but the file as a whole is not. 
The other files can be deserialized/serialized by Java classes.
These can also be imported into Mathematica arrays.
* allChordFormulas.json - org.dwbzen.music.element.song.ChordFormulas class
* chord_formulas.json - all the chord formulas sorted by name. Can be imported into MongoDB
* chord_formulas_import.json - all the chord formulas sorted by group. This can also be imported into MongoDB.
* allChords.json - org.dwbzen.music.element.song.ChordLibrary class. This has the spellings of all the chord formulas in all keys.
* all_chords.json - same content as allChords.json. Structure is a JSON array with the chord root as the key.

**Rhythm Scales**

Rhythm scales are used in ProductionFlow (which creates a score from fractal data) to quantitize a raw duration to a note value.


**Songs**

*TODO*

**Fractals**

*TODO*

