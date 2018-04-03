package junit;

import org.junit.Test;
import music.element.song.Melody;
import music.element.song.Notation;
import music.element.song.SongNote;

public class MelodyTest {

	@Test
	public void testMelody() {
		
		Melody melody = new Melody();
		Notation notation = new Notation("eighth");
		SongNote songNote = new SongNote("Eb5", notation);
		SongNote songNote2 = new SongNote("D5", notation);
		SongNote songNote3 = new SongNote("G#5", notation);
		
		System.out.println(songNote.toJson());
		notation.setTuplet("3/2");
		songNote.setNotation(notation);
		songNote2.setNotation(notation);
		songNote3.setNotation(notation);
		System.out.println(songNote.toJson());
		System.out.println(songNote2.toJson());
		System.out.println(songNote3.toJson());

		melody.getSongNotes().add(songNote);
		melody.getSongNotes().add(songNote2);
		melody.getSongNotes().add(songNote3);
		String jsonString = melody.toJson();
		System.out.println(jsonString);
		
	}
}
