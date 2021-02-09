package org.dwbzen.music.element.song;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.TimeSignature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A pitch + Duration. Can also be a rest (no pitch).  A lite version of music.element.Note<br>
 * Some examples,<br>
 * 	{ "pitch" : "C5",  "notations" : { "type" : "eighth" }<br>
 *  { "pitch" : "0",   "notations" : { "type" : "eighth", "tuplet" : "3/2" }<br>
 *  { "pitch" : "Eb4", "notations" : { "type" : "eighth", "dots" : 1, "tie" : "start" } }<br>
 *  { "pitch" : "Eb4", "notations" : { "type" : "16th", "tie" : "stop" } }</p>
 * 
 * A "0" Pitch indicates a rest. Duration is not set directly, but calculated from the type of note<br>
 * given by notations.type, the TimeSignature, and the number of units per measure (default is 480).<br>
 * 
 * 
 * @author don_bacon
 *
 */
public class SongNote implements IJson, Comparable<SongNote> {
	
	/** Used to represent a Terminal state in a Markov Chain */
	public final static SongNote TERMINAL_SONG_NOTE = new SongNote("¶", new Notation());
	
	/** Used to represent a NULL key in a Map - since it can't really be a null */
	public final static SongNote NULL_VALUE_SONG_NOTE = new SongNote("§", new Notation());

	@JsonProperty("pitch")				private String pitch = null;
	@JsonProperty("notations")			private Notation notation;
	
	@JsonIgnore	private int unitsPerMeasure = RhythmScale.defaultUnitsPerMeasure;	// typically 480
	@JsonIgnore	private boolean rest = false;		// set automatically
	@JsonIgnore	protected Duration duration;		// calculated from Notation, TimeSignature and units/measure
	@JsonIgnore	private Pitch notePitch = null;		// will be null for a rest
	@JsonIgnore	private TimeSignature timeSignature = TimeSignature.FourFourTimeSignature;		// 4/4 with default units/measure
	
	@JsonIgnore	private Key	originalKey = null;				// optional - will be non-null if transposedKey is set
	@JsonIgnore	private Key transposedKey = null;			// optional - used by SongAnalyzer
	
	public SongNote() {
		this("0", new Notation());
	}
	
	public SongNote(String pitch, Notation notation) {
		this.pitch = pitch;
		setNotation(notation);
	}

	@Override
	/**
	 * SongNotes are equal if they have the same note type, pitch, and Duration.
	 * Comparison is first by note type, then pitch then by duration.
	 */
	public int compareTo(SongNote otherSongNote) {
		int result = 1;
		if(otherSongNote != null) {
			
		}
		return result;
	}

	public String getPitch() {
		return pitch;
	}

	public void setPitch(String pitch) {
		this.pitch = pitch;
	}

	public Notation getNotation() {
		return notation;
	}

	public void setNotation(Notation notation) {
		this.notation = notation;
	}

	public boolean isRest() {
		return rest;
	}

	public void setRest(boolean rest) {
		this.rest = rest;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}
	
	/**
	 * Sets Duration from TimeSignature, Notation, and units per measure
	 * @return number of units
	 */
	public int setDuration() {
		// TODO
		return 0;
	}

	public Pitch getNotePitch() {
		return notePitch;
	}

	public void setNotePitch(Pitch notePitch) {
		this.notePitch = notePitch;
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

	public TimeSignature getTimeSignature() {
		return timeSignature;
	}

	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
	}

	public int getUnitsPerMeasure() {
		return unitsPerMeasure;
	}

	public void setUnitsPerMeasure(int unitsPerMeasure) {
		this.unitsPerMeasure = unitsPerMeasure;
	}

	public String toString() {
		return toJson();
	}
}
