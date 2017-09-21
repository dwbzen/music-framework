package junit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import music.element.Key;
import music.element.TimeSignature;
import music.element.song.KeyLite;
import music.element.song.Melody;
import music.element.song.Notation;
import music.element.song.SongMeasure;
import music.element.song.SongNote;

public class SongMeasureTest {

	@Test
	public void testSongMeasure() {
		Morphia morphia = new Morphia();
		morphia.mapPackage("music.element");
		morphia.mapPackage("music.element.song");

		SongMeasure measure = createTestSongMeasure();
		String jsonString = measure.toJSON();
		System.out.println("measure: " + jsonString);
		
		BasicDBObject obj = (BasicDBObject)JSON.parse(jsonString);
		SongMeasure m = morphia.fromDBObject(null, SongMeasure.class, obj);
		String fromDBObject = m.toJSON();
		System.out.println("from DB object: " + fromDBObject);
		assertTrue(fromDBObject.equals(jsonString));
		
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
