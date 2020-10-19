package org.dwbzen.music.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.instrument.MidiInstrument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The music data portion of a ScorePart
 * @author don_bacon
 *
 */
public class ScorePartEntity implements Serializable {

	private static final long serialVersionUID = -8305108753987313360L;
	@JsonProperty("partName")	private String partName;
	@JsonProperty("partNumber")	private int partNumber;
	@JsonProperty("partId")		private String partId;
	@JsonProperty("scoreKey")   private Key scoreKey = null;		// set from configuration by ScorePart
	@JsonProperty("staves")		private int numberOfStaves = 1;		// set to 2 for Grand Staff or as required by the instrument.

	
	/**
	 * A linked List of Measures
	 */
	@JsonProperty	private List<Measure> measures = new ArrayList<Measure>();
	@JsonIgnore		private Instrument instrument;

	/**
	 * Corresponding midi instrument for this Part - could be null
	 */
	@JsonIgnore		private MidiInstrument midiInstrument;
	@JsonIgnore		private Score score;	// parent Score

	public ScorePartEntity(Score score, String partName, Instrument instrument) {
		this.partName = partName;
		this.instrument = instrument;
		this.score = score;
		numberOfStaves = instrument.getNumberOfStaves();
	}
	
    public String toString() {
    	StringBuffer sb = new StringBuffer(partName);
    	for(Measure measure: getMeasures()) {
    		sb.append(measure.toString() + "\n");
    	}
    	return sb.toString();
    }


	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public List<Measure> getMeasures() {
		return measures;
	}

	public void setMeasures(List<Measure> measures) {
		this.measures = measures;
	}

	public MidiInstrument getMidiInstrument() {
		return midiInstrument;
	}

	public void setMidiInstrument(MidiInstrument midiInstrument) {
		this.midiInstrument = midiInstrument;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public Score getScore() {
		return score;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public String getPartId() {
		return partId;
	}

	public void setPartId(String partId) {
		this.partId = partId;
	}

	public Key getScoreKey() {
		return scoreKey;
	}

	public void setScoreKey(Key scoreKey) {
		this.scoreKey = scoreKey;
	}

	public int getNumberOfStaves() {
		return numberOfStaves;
	}

	public void setNumberOfStaves(int numberOfStaves) {
		this.numberOfStaves = numberOfStaves;
	}
	
}
