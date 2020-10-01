
# Chord Formulas - including common chord symbols
[Chord (music) on Wikipedia](https://en.wikipedia.org/wiki/Chord_%28music%29)

JSON structure essentially the same as scale formulas with a couple of additions.
"symbols" are the chord symbol(s). Common symbols include:
  * M = major
  * m = minor
  * -, b or dim = diminished/lowered
  * +, # or aug = augmented/raised
  * digits = scale degrees
  * add = added note(s)
  * sus = suspended - missing 3rd
  * # or + = raise degree one step
  * b or - = lower degree one step

When there is more than one symbol the most common is given first.
* "chordSize" refers to the length of the formula - as in scales
* "formula" is the #steps between the notes of the chord - as in scales
* "intervals" is the interval between the notes of the chord
* "chordSize" is the number of notes in the chord

For 13th chords, the 11th is omitted from the spelling.
For example a C13 is C, E, G, Bb, D, A omitting the F.

"formulaNumber" is a 3-byte hex (12 bits) where each bit corresponds to the scale degree-1 
of the notes in the chord, with the root note at "C". This is the absolute formulaNumber expressed in Hex.
meaning pitches are normalized to a single octave range
      
"spellingNumber" is similar but uses the actual chord spelling over a 2-octave range.

For example, C Dominant ninth (formula "9") spelling is C, E ,G, Bb, D. 
scale degree-1 == 10,7,4,2,0 as binary:  0100 1001 0101 or HEX(495)

To find the relative chord number, rotate left by the degree-1 of the desired root.
So E9 would be rotateLeft(0100 1001 0101 , 4) == 1001 0101 0100 or HEX(954)
In the chord formulas definitions the formulaNumber is given in Decimal (145 instead of 0X91 for example)

# MusicXML
The resources Chord_Test.mscz and Chord_Test.musicxml provide examples of how chords and ties
between chords are represented in musicXML. The first note of the chord has no
special XML elements. Subsequent notes include a `</chord>` element between the `<note>` and `<pitch>` elements.

**Ties between chords**
Each note of the chord tied to the same note in the next chord includes notations and tie elements indicating the start of the tie:

```
<tie type="start"/>
<notations>
  <tied type="start"/>
</notations>
```
Each note of the tied chord includes notations and tie elements indicating the end of the tie:

```
<tie type="stop"/>
<notations>
  <tied type="stop"/>
</notations>
```

