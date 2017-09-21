package music.element.song;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

import music.element.Duration;
import music.element.Key;
import music.element.Pitch;
import music.element.TimeSignature;
import util.IJson;

/**
 * A pitch + Duration. Can also be a rest (no pitch).
 * A lite version of music.element.Note
 * Some examples, 
 * 	{ "pitch" : "C5",  "notations" : { "type" : "eighth" }
 *  { "pitch" : "0",   "notations" : { "type" : "eighth", "tuplet" : "3/2" }
 *  { "pitch" : "Eb4", "notations" : { "type" : "eighth", "dots" : 1, "tie" : "start" } }
 *  { "pitch" : "Eb4", "notations" : { "type" : "16th", "tie" : "stop" } }
 * 
 * A "0" Pitch indicates a rest.
 * 
 * @author don_bacon
 *
 */
@Embedded
@Entity(value="Note", noClassnameStored=true)
public class SongNote implements IJson, Comparable<SongNote> {

	private static final long serialVersionUID = 1282905330533960496L;
	private static Morphia morphia = new Morphia();

	@Property("pitch")		private String pitch = null;
	@Embedded("notations")	private Notation notation;
	@Transient				private boolean rest = true;	// set automatically
	@Transient				protected Duration duration;	// calculated from Notation + TimeSignature
	@Transient				private Pitch notePitch = null;		// will be null for a rest
	@Transient				private Key	originalKey = null;				// optional - will be non-null if transposedKey is set
	@Transient				private Key transposedKey = null;			// optional - used by SongAnalyzer
	@Transient				private TimeSignature timeSignature = null;
	
	public SongNote() {
	}
	
	public SongNote(String pitch, Notation notation) {
		this.pitch = pitch;
		this.notation = notation;
	}

	@Override
	public int compareTo(SongNote arg0) {
		// TODO Auto-generated method stub
		return 0;
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
	 * Sets Duration from TimeSignature + Notation
	 * if both are non null
	 */
	public void setDuration() {
		
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

	@Override
	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}

}
