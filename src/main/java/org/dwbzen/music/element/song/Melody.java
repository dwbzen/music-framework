package org.dwbzen.music.element.song;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.cp.ICollectable;
import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.Key;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The melody part of a SongMeasure. For example, given 192 divisions in 4/4 time:
 * "melody" : [  <br>
 * 		{ "pitch" : "0", "notations" :   { "type" : "quarter", "tuplet" : "3/2" } },<br>
 * 		{ "pitch" : "Bb4", "notations" : { "type" : "quarter", "tuplet" : "3/2" } },<br>
	    { "pitch" : "C5",  "notations" : { "type" : "quarter", "tuplet" : "3/2"} },<br>
 * 	    { "pitch" : "E5",  "notations" : { "type" : "half" } } ]
 * 
 * @author don_bacon
 *
 */
public class Melody implements IJson, ICollectable<SongNote> {

	public static String NO_MELODY_KEY = "NONE";	// Pitch is "0"
	public static final SongNote TERMINAL = SongNote.TERMINAL_SONG_NOTE;
	public static final SongNote NULL_VALUE = SongNote.NULL_VALUE_SONG_NOTE;
	
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

	public static void main(String... args) {
		
	}

	@Override
	public SongNote getTerminal() {
		return TERMINAL;
	}

	@Override
	public SongNote getNullValue() {
		return NULL_VALUE;
	}
}
