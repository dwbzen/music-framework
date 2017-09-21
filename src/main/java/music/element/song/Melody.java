package music.element.song;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import music.element.Key;
import util.IJson;

/**
 * The melody part of a SongMeasure. For example, given 192 divisions in 4/4 time:
 * "melody" : [  
 * 		{ "pitch" : "D4", "duration" : 32, "notations" : { "type" : "quarter", "tuplet" : "3/2" } },
 * 		{ "pitch" : "Bb4", "duration" : 32, "notations" : { "type" : "quarter", "tuplet" : "3/2" } },
	    { "pitch" : "C5", "duration" : 32, "notations" : { "type" : "quarter", "tuplet" : "3/2"} },
 * 	    { "pitch" : "E5",  "notations" : { "type" : "half" } } ]
 * @author don_bacon
 *
 */
@Embedded
public class Melody implements IJson {

	private static final long serialVersionUID = 8282039435484302599L;
	private static Morphia morphia = new Morphia();
	public static String NO_MELODY_KEY = "NONE";	// Pitch is "0"

	@Embedded("notes")  private List<SongNote> songNotes = new ArrayList<SongNote>();
	@Transient			 private Key	originalKey = null;			// optional - will be non-null if transposedKey is set
	@Transient			 private Key transposedKey = null;			// optional - used by SongAnalyzer
	@Transient			 private SongMeasure songMeasure = null;	// parent SongMeasure

	public Melody() {
	}
	
	public Key getOriginalKey() {
		return originalKey;
	}

	public void setOriginalKey(Key originalKey) {
		this.originalKey = originalKey;
	}

	public Key getTransposedKey() {
		return transposedKey;
	}

	public void setTransposedKey(Key transposedKey) {
		this.transposedKey = transposedKey;
	}

	public SongMeasure getSongMeasure() {
		return songMeasure;
	}

	public void setSongMeasure(SongMeasure songMeasure) {
		this.songMeasure = songMeasure;
	}

	public List<SongNote> getSongNotes() {
		return songNotes;
	}

	@Override
	public String toJSON() {
		return  morphia.toDBObject(this).toString();
	}

	public static void main(String... args) {
		
	}
}
