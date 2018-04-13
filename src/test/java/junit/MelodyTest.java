package junit;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import music.element.song.Melody;
import music.element.song.Notation;
import music.element.song.SongNote;

public class MelodyTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(MelodyTest.class);

	@Test
	public void testMelody() {
		
		Melody melody = new Melody();
		Notation notation = new Notation("eighth");
		SongNote songNote = new SongNote("Eb5", notation);
		SongNote songNote2 = new SongNote("D5", notation);
		SongNote songNote3 = new SongNote("G#5", notation);
		assertTrue(songNote.getPitch().equals("Eb5"));
		
		log.info(songNote.toJson());
		notation.setTuplet("3/2");
		songNote.setNotation(notation);
		songNote2.setNotation(notation);
		songNote3.setNotation(notation);
		log.debug(songNote.toJson());
		log.debug(songNote2.toJson());
		log.debug(songNote3.toJson());

		melody.getSongNotes().add(songNote);
		melody.getSongNotes().add(songNote2);
		melody.getSongNotes().add(songNote3);
		String jsonString = melody.toJson();
		log.debug(jsonString);
		
	}
}
