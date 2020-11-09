package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.music.PitchCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Phrase is a list of measures treated as a unit.<br>
 * It is associated with a specific Instrument which also determines the RhythmScale and number of staves.<br>
 * Phrase includes operations such as creating a retrograde, adjusting durations, and transposition.</p>
 * Certain instruments require a Grand Staff (2 staves). By default any operations apply to all staves,<br>
 * or optionally constrained to a specific staff.</p>
 * 
 * @author don_bacon
 *
 */
public class Phrase implements IJson, Cloneable {

	@JsonProperty("staves")		private int numberOfStaves = 1;		// determined by the instrument.
	@JsonProperty	private List<Measure> measures = new ArrayList<Measure>();
	@JsonProperty	private Instrument instrument;
	@JsonIgnore		private IRhythmScale rhythmScale;
	@JsonIgnore		private	Map<Integer, PitchCollection> pitchCollections = null;		// keyed by staff number
	
	/**
	 * Create a Phrase with defaults
	 */
	public Phrase() {
	}
	
	public Phrase(Instrument instrument) {
		assert(instrument != null);
		setInstrument(instrument);
	}

	@Override
	public Phrase clone() {
		Phrase phrase = new Phrase(instrument);
		for(Measure measure : measures) {
			phrase.addMeasure(Measure.copy(measure, true));
		}
		return phrase;
	}
	
	public Phrase getInversion() {
		Phrase phrase = new Phrase(instrument);
		// TODO
		return phrase;
	}
	
	public Phrase getRetrograde() {
		Phrase phrase = new Phrase(instrument);
		// TODO
		return phrase;
	}
	
	public Phrase getRetrogradeInversion() {
		return getInversion().getRetrograde();
	}
	
	/**
	 * Get the Pitches for the Notes in each Measure for a particular staff
	 * @return PitchCollection
	 */
	public PitchCollection getPitches(int staffNumber) {
		if(pitchCollections == null) {
			pitchCollections = new HashMap<>();
			for(int snum = 1; snum <= numberOfStaves; snum++) {
				PitchCollection pc = new PitchCollection();
				pc.setOctaveNeutral(false);
				for(Measure measure : measures) {
					for(Measurable meas : measure.getMeasureables(snum)) {
						if(meas.getType().equals(Measurable.NOTE)) {
							Note note = (Note)meas;
							pc.addPitch(note.getPitch());
						}
						else if(meas.getType().equals(Measurable.CHORD)) {
							Chord chord = (Chord)meas;
							// TODO
						}
						else {
							// TODO
							RhythmElement re = (RhythmElement)meas;
						}
					}
				}
				pitchCollections.put(snum, pc);
			}
		}
		return pitchCollections.get(staffNumber);
	}
	
	public void addMeasure(Measure measure) {
		measures.add(measure);
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
		numberOfStaves = instrument.getCleffs().size();
		rhythmScale = instrument.getRhythmScale();
	}

	public IRhythmScale getRhythmScale() {
		return rhythmScale;
	}

	public void setRhythmScale(IRhythmScale rhythmScale) {
		this.rhythmScale = rhythmScale;
	}

	public int getNumberOfStaves() {
		return numberOfStaves;
	}

	public List<Measure> getMeasures() {
		return measures;
	}
	
}
