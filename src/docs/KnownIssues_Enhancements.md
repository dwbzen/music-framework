
## Known Issues

* Have not tested for time signatures other than 4/4.

### Chordal texture implementation
* The way ScalePart handles chords need to be factored. Currently, the raw data points are<br>
first converted to notes and the duration set from addFactors.<br> 
This can result in 2 or more notes joined by a tie. For example assuming 480 base units per measure<br>
300 units are represented by 2 notes: a half tied to an eighth.<br>
ScorePart creates chords by picking off notes one at a time and this is complicated by<br>
tied notes which is meaningless in this context. Also handling end of the measure processing<br>
is complicated (and currently doesn't work) by tied notes.<p>
Chord processing needs to work off it's own List<Note> separate from the List used by Monophonic instruments.<br>
Instead of tieing notes, add `List<Duration> factors ` attribute to the Note.<br>
To further simplify processing, create separate scoreInstrument() methods for Monophonic and Polyphonic instruments.
Namely, scoreInstument -> scoreMonophonicTextureInstrument() or scorePolyphonicTextureInstrument().
* ScoreAnalysis doesn't work on polyphonic instruments (viz. it doesn't parse the chord notes correctly).<br>
For these instruments (such as PianoLFChords) set -analysis false on the command line.<p>
* Test that MusicXMLHelper is handling chord ties correctly - both within and across bar lines.

## Planned Enhancements
* ScaleExportManager - add "musicXML" as an output format. This would consist of a 2-octave range ascending and descending<br>
starting with the specified root note(s) for each exported scale. The resulting musicxml file could then be<br>
imported into MuseScore to hear how the scale(s) sound.<p>
A further enhancement could add chords to scales of 8 notes or less.<p>
* Add MelodyCollector and MelodyProducer to cp package. These will be the melody equivalents of HarmonyChordCollector and ChordProgressionProducer.<p>
* Add an option for including rhythmic elements in MarkovChain creation for chords & melodies.<br>
Essentially build Markov Chains on melody/chords alone, rhythm alone (i.e. durations), and melody/rhythm, chord/rhythm.<br>
The result could be fed into a Producer.<p>
* Add a musicXML to/from songJSON conversion. Recall that songJSON is an alternative to musicXML that consists of musical<br>
elements only. That is, formatting is not included. This really simplifies creating song.json files which can be<br>
the input to Collector/Producer processing. This could be done explicitly or implicitly when providing a musicXML<br>
file for Collector/Producer processing.
