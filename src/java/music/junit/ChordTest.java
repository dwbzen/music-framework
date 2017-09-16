package music.junit;

import music.element.Chord;
import music.element.Duration;
import music.element.Dynamics;
import music.element.Key;
import music.element.Note;
import music.element.Pitch;
import music.element.Step;
import music.element.Tempo;

import org.mongodb.morphia.Morphia;
import com.mongodb.DBObject;

public class ChordTest {
	
	public static void main(String[] args) {
		if(args.length > 0) {
			Morphia morphia = new Morphia();
			morphia.map(Pitch.class).map(Note.class).map(Duration.class).map(Key.class).map(Dynamics.class).map(Tempo.class);
			Chord chord = new Chord();
			for(String s:args) {
				Pitch p = Pitch.fromString(s);
				Duration dur = new Duration(6);
				Note note = new Note(p, dur);
				chord.addNote(note);
			}
			DBObject dbo = morphia.toDBObject(chord);
			System.out.println(dbo.toString());
			System.out.println(chord);
			for(Note note : chord.getNotes()) {
				if(note.getPitch().getStep().equals(Step.G)) {
					note.getPitch().setStep(Step.C);
				}
			}
			System.out.println(chord);	// has unison interval
			Chord chord2 = new Chord(chord.removeUnisonNotes());
			System.out.println(chord2);	// has unison interval removed
		}
	}
}
