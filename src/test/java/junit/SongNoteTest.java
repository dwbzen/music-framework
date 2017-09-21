package junit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import music.element.song.Notation;
import music.element.song.SongNote;

public class SongNoteTest {
	
	@Test
	public void testSongNote() {
		Morphia morphia = new Morphia();
		morphia.mapPackage("music.element");
		morphia.mapPackage("music.element.song");
		
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
		String jsonString = songNote3.toJSON();
		System.out.println(jsonString);
		
		BasicDBObject obj = (BasicDBObject)JSON.parse(jsonString);
		SongNote sn = morphia.fromDBObject(null, SongNote.class, obj);
		String snJSON = sn.toJSON();
		System.out.println("from DB object: " + snJSON);
		assertTrue(jsonString.equals(snJSON));
		
	}

}
