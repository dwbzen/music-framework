package test;

import music.element.Chord;
import music.element.Duration;
import music.element.Note;
import music.element.Pitch;


public class ChordTest {
	
	static String[] chordNotes = {"C3", "E3", "G3", "Bb3", "C3" };
	public static void main(String[] args) {
		Chord chord = new Chord();
		for(String s:chordNotes) {
			Pitch p = Pitch.fromString(s);
			Duration dur = new Duration(6);
			Note note = new Note(p, dur);
			chord.addNote(note);
		}
	
		System.out.println(chord.toJson());
	
		System.out.println(chord);	// has unison interval
		Chord chord2 = new Chord(chord.removeUnisonNotes());
		System.out.println(chord2);	// has unison interval removed
	}
}
