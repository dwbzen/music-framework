package junit;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.music.element.song.Notation;
import org.dwbzen.music.element.song.SongNote;

public class SongNoteTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(SongNoteTest.class);
	@Test
	public void testSongNote() {
		
		Notation notation = new Notation("eighth");
		SongNote songNote = new SongNote("Eb5", notation);
		SongNote songNote2 = new SongNote("D5", notation);
		SongNote songNote3 = new SongNote("G#5", notation);
		
		log.info(songNote.toJson());
		notation.setTuplet("3/2");
		songNote.setNotation(notation);
		songNote2.setNotation(notation);
		songNote3.setNotation(notation);
		log.info(songNote.toJson());
		log.info(songNote2.toJson());
		String jsonString = songNote3.toJson();
		log.info(jsonString);
		
	}

}
