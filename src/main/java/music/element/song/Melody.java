package music.element.song;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import music.element.Key;
import mathlib.util.IJson;

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
public class Melody implements IJson {

	private static final long serialVersionUID = 8282039435484302599L;
	public static String NO_MELODY_KEY = "NONE";	// Pitch is "0"

	@JsonProperty("notes")  private List<SongNote> songNotes = new ArrayList<SongNote>();
	@JsonIgnore			 	private Key	originalKey = null;			// optional - will be non-null if transposedKey is set
	@JsonIgnore			 	private Key transposedKey = null;			// optional - used by SongAnalyzer
	@JsonIgnore			 	private SongMeasure songMeasure = null;	// parent SongMeasure

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
		return  toJson();
	}

	public static void main(String... args) {
		
	}
}
