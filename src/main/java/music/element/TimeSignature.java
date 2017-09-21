package music.element;

import java.io.Serializable;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import util.IJson;

@Embedded
public class TimeSignature implements Serializable, IJson {

	private static final long serialVersionUID = 3422423947214684471L;
	public static final int DEFAULT_DIVISIONS_PER_MEASURE = 16;	// 4 divisions per beat (quarter note) in 4/4 time

	/**
	 * Time signature beats per measure
	 */
	@Property("beats")		private int beats = 4;	// beats per measure. divisions per beat = divisions/beats, 24/3 = 8 for example
	/**
	 * Time signature beat note (1=whole, 2=half, 4 = quaver, 8 = semiquaver etc.)
	 * SO time signature is beats/beatNote: 3/4, 6/8, whatever
	 */
	@Property("beatNote")	private int beatNote = 4;		// defaults to quarterNote
	/**
	 * Number of basic units in the measure. Must be >0
	 */
	@Property("divisions")	private int divisions = DEFAULT_DIVISIONS_PER_MEASURE;

	/**
	 * Defaults to 4/4 time
	 */
	public TimeSignature() {
	}
	
	public TimeSignature(int beatsPerMeasure, int notethatHasOneBeat, int numOfDivisions) {
		this.beats = beatsPerMeasure;
		this.beatNote = notethatHasOneBeat;
		this.divisions = numOfDivisions;
	}
	
	public int getDivisions() {
		return divisions;
	}

	public void setDivisions(int divisions) {
		this.divisions = divisions;
	}
	
	public int getDivisionPerBeat() {
		return  divisions/beats;
	}
	public int getBeats() {
		return beats;
	}

	public void setBeats(int beats) {
		this.beats = beats;
	}

	public int getBeatNote() {
		return beatNote;
	}

	public void setBeatNote(int beatNote) {
		this.beatNote = beatNote;
	}
	
	public void setTimeSignature(int[] ts) {
		beats = ts[0];
		beatNote = ts[1];
	}
	
	public int[] getTimeSignature() {
		int[] ts = new int[2];
		ts[0] = beats;
		ts[1] = beatNote;
		return ts;
	}

}
