package org.dwbzen.music.element;

import java.io.Serializable;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Immutable class encapsulates time signature as in 4/4 or 3/8.
 * @author don_bacon
 *
 */
public final class TimeSignature implements Serializable, IJson {

	static final long serialVersionUID = 3422423947214684471L;
	public static final int DEFAULT_DIVISIONS_PER_MEASURE = RhythmScale.defaultUnitsPerMeasure;	// 480 units = 120 divisions per beat (quarter note) in 4/4 time
	public static final TimeSignature FourFourTimeSignature = new TimeSignature();

	/**
	 * Time signature beats per measure, typically 4
	 */
	@JsonProperty("beatsPerMeasure")	private final int beats;	// beats per measure. divisions per beat = divisions/beats, 24/3 = 8 for example
	/**
	 * Time signature is beats/beatNote. Beat note (1=whole, 2=half, 4 = quaver, 8 = semiquaver etc.)
	 */
	@JsonProperty("beatType")	private final int beatNote;
	/**
	 * Number of basic units in the measure. Must be >0
	 */
	@JsonProperty("divisions")	private final int divisions;

	/**
	 * Creates a 4/4 TimeSignature with default number of divisions/measure.
	 */
	public TimeSignature() {
		beats = 4;
		beatNote = 4;
		divisions = DEFAULT_DIVISIONS_PER_MEASURE;
	}
	public TimeSignature(int beatsPerMeasure, int notethatHasOneBeat, int numOfDivisions) {
		beats = beatsPerMeasure;
		beatNote = notethatHasOneBeat;
		divisions = numOfDivisions;
	}
	
	public int getDivisions() {
		return divisions;
	}
	
	public int getDivisionPerBeat() {
		return  divisions/beats;
	}
	public int getBeats() {
		return beats;
	}

	public int getBeatNote() {
		return beatNote;
	}

	public int[] getTimeSignature() {
		int[] ts = new int[2];
		ts[0] = beats;
		ts[1] = beatNote;
		return ts;
	}

}
