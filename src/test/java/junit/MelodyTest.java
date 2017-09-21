package music.junit;

import org.junit.Test;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import music.element.song.Melody;
import music.element.song.Notation;
import music.element.song.SongNote;

public class MelodyTest {

	@Test
	public void testMelody() {
		Morphia morphia = new Morphia();
		morphia.mapPackage("music.element");
		morphia.mapPackage("music.element.song");
		
		Melody melody = new Melody();
		Notation notation = new Notation("eighth");
		SongNote songNote = new SongNote("Eb5", notation);
		SongNote songNote2 = new SongNote("D5", notation);
		SongNote songNote3 = new SongNote("G#5", notation);
		
		System.out.println(songNote.toJSON());
		notation.setTuplet("3/2");
		songNote.setNotation(notation);
		songNote2.setNotation(notation);
		songNote3.setNotation(notation);
		System.out.println(songNote.toJSON());
		System.out.println(songNote2.toJSON());
		System.out.println(songNote3.toJSON());

		melody.getSongNotes().add(songNote);
		melody.getSongNotes().add(songNote2);
		melody.getSongNotes().add(songNote3);
		String jsonString = melody.toJSON();
		System.out.println(jsonString);
		
		BasicDBObject obj = (BasicDBObject)JSON.parse(jsonString);
		Melody m = morphia.fromDBObject(null, Melody.class, obj);
		System.out.println("from DB object: " + m.toJSON());

	}
}
