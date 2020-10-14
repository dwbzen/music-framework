
## Known Issues

* Time signatures other than 4/4 have not been tested.

### Chordal texture implementation
* The way ScalePart handles chords need to be refactored. Currently, the raw data points are<br>
first converted to notes and the duration set from addFactors.<br> 
This can result in 2 or more notes joined by a tie. For example assuming 480 base units per measure<br>
300 units are represented by 2 notes: a half tied to an eighth.<br>
ScorePart creates chords by picking off notes one at a time and this is complicated by<br>
tied notes which is meaningless in a chordal context. Also handling end of the measure processing<br>
is complicated (and currently doesn't work) with tied notes.<p>

* ScoreAnalysis doesn't work on polyphonic instruments (viz. it doesn't parse the chord notes correctly).<br>
For these instruments (such as PianoLFChords) set -analysis false on the command line.</p>

* Test that MusicXMLHelper is handling chord ties correctly - both within and across bar lines.

## Planned Enhancements
* ScaleExportManager. Add "musicXML" as an output format. This would consist of a 2-octave range ascending and descending<br>
starting with the specified root note(s) for each exported scale. The resulting musicXML file could then be<br>
imported into MuseScore to hear how the scale(s) sound. A further enhancement could add chords to scales of 8 notes or less.</p>

* ChordManager. Add "musicXML" as an output format for `-export chords` option. This would output a musicXML<br>
file that could be imported into notation software in order to hear what the chords sound like.

* Add MelodyCollector and MelodyProducer to cp (collector-producer) package. These will be the melody equivalents of HarmonyChordCollector and ChordProgressionProducer.<p>

* Add an option for including rhythmic elements in MarkovChain creation for chords & melodies.<br>
Essentially build Markov Chains on melody/chords alone, rhythm alone (i.e. durations), and melody/rhythm, chord/rhythm.<br>
The result could be fed into a Producer.</p>

* Add a musicXML to/from songJSON conversion. Recall that songJSON is an alternative to musicXML that consists of musical<br>
elements only. That is, formatting is not included. This really simplifies creating song.json files which can be<br>
the input to Collector/Producer processing. This could be done explicitly or implicitly when providing a musicXML<br>
file for Collector/Producer processing.</p>

* Allow for occasional rests. There is a configuration parameter score.restProbability, given as %, but it's not currently used.<br>
Instruments should have the ability to override this global value as in `<instrument_name>.restProbability`<br>
For example `Flute.restProbability=15`</p>

* Add a help option (-h or --help) to the following programs: ChordManager, ChordProgressionProducer, HarmonyChordCollector,<br>
ProductionFlow, ScaleExportManager, and also MelodyCollector and MelodyProducer when those classes are completed.</p>

* Create a single script to import scales, chords, songs and fractal files into MongoDB. This would encapsulate the steps in [Imports.md](Imports.md)</p>

## Refactor This
* Simplify the instrument properties in orchestration.properties. The `music.instrument.` and `score.instrument` prefixes can be dropped.<br>
For example, `music.instrument.Flute.instrument-sound` becomes `Flute.instrument-sound`<br>
and `score.instruments.BassClarinet.class` is simply `BassClarinet.class`. Also the "vocal"  for SATB parts can also be dropped<br>
so `music.instrument.vocal.Bass.midiProgram` becomes `Bass.midiProgram`. </p>

* Chord processing (ScorePart class) needs to work off it's own List<Note> separate from the List used by Monophonic instruments.<br>
Instead of tieing notes, add `List<Duration> factors ` attribute to the Note.<br>
To further simplify processing, create separate scoreInstrument() methods for Monophonic and Polyphonic instruments.<br>
Namely, scoreInstument -> scoreMonophonicTextureInstrument() or scorePolyphonicTextureInstrument().</p>

* Refactor Melody, SongNote to use the NoteType enum instead of a String. NoteType encapsulates<br>
the type of note (as in whole, half, eighth, etc.) and the number of dots (up to 3 depending on the type).</p>

