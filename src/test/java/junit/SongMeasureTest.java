package junit;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import music.element.Key;
import music.element.TimeSignature;
import music.element.song.KeyLite;
import music.element.song.Melody;
import music.element.song.Notation;
import music.element.song.SongMeasure;
import music.element.song.SongNote;

public class SongMeasureTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(SongMeasureTest.class);
	@Test
	public void testSongMeasure() {

		SongMeasure measure = createTestSongMeasure();
		String jsonString = measure.toJson();
		log.info("measure: " + jsonString);
		assertEquals(measure.getMelody().getSongNotes().size(), 3);
	}
	
	private static SongMeasure createTestSongMeasure() {
		Melody melody = new Melody();
		Notation notation = new Notation("eighth");
		SongNote songNote = new SongNote("Eb5", notation);
		SongNote songNote2 = new SongNote("D5", notation);
		SongNote songNote3 = new SongNote("G#5", notation);
		
		notation.setTuplet("3/2");
		songNote.setNotation(notation);
		songNote2.setNotation(notation);
		songNote3.setNotation(notation);

		melody.getSongNotes().add(songNote);
		melody.getSongNotes().add(songNote2);
		melody.getSongNotes().add(songNote3);
		
		SongMeasure measure = new SongMeasure();
		KeyLite key = new KeyLite(Key.A_MAJOR);
		measure.setKey(key);
		measure.setNumber(1);
		measure.setMelody(melody);
		measure.setTimeSignature(new TimeSignature(4, 4, 192));
		return measure;
	}
}
