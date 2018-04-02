package junit;

import org.junit.Test;

import junit.framework.TestCase;
import music.element.song.Notation;
import music.element.song.SongNote;

public class SongNoteTest extends TestCase {
	
	@Test
	public void testSongNote() {
		
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
		String jsonString = songNote3.toJson();
		System.out.println(jsonString);
		
	}

}
