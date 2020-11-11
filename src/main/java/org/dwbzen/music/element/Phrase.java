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
 * Phrase includes operations such as creating a retrograde, transposition, adjusting durations, creating tone rows.</p>
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
	
	/**
	 * The retrograde of a Phrase is all the notes/rests on the measure(s) on each staff in reverse order.<br>
	 * Notes retain their original duration.
	 * @return Phrase
	 */
	public Phrase getRetrograde() {
		Phrase phrase = new Phrase(instrument);
		// TODO
		return phrase;
	}
	
	/**
	 * The retrograde of a Phrase is all the notes/rests on the measure(s) on the designated staff in reverse order.<br>
	 * Notes retain their original duration.
	 * 
	 * @param staffNumber - the staff number to create the retrograde from. If <= 0, forms the retrograde on all staves.
	 * @return Phrase with the designated staff/staves in retrograde. Other staves are left unchanged.
	 */
	public Phrase getRetrograde(int staffNumber) {
		Phrase phrase = null;
		if(staffNumber <= 0) {
			phrase = getRetrograde();
		}
		else {
			phrase = new Phrase(instrument);
			// TODO
		}
		return phrase;
	}
	
	/**
	 * 
	 * @param staffNumber - the staff number to create the retrograde from. If <= 0, forms the retrograde on all staves.
	 * @param notesOnly - if true, only the pitches are reversed, the durations remain the same.
	 * @return  Phrase with the designated staff/staves in retrograde.
	 */
	public Phrase getRetrograde(int staffNumber, boolean notesOnly) {
		Phrase phrase = new Phrase(instrument);
		// TODO
		return phrase;
	}
	
	public Phrase getRetrogradeInversion() {
		return getInversion().getRetrograde();
	}
	
	/**
	 * Get the Pitches for the Notes in each Measure for a particular staff.<br>
	 * This actually creates and saves the PitchCollection for all staves so subsequent calls will be quicker.
	 * 
	 * @param staffNumber
	 * @return PitchCollection of the PitchElements in the designated staff.
	 */
	public PitchCollection getPitches(int staffNumber) {
		if(pitchCollections == null) {
			createPitchCollections();
		}
		return pitchCollections.get(staffNumber);
	}
	
	private void createPitchCollections() {
		pitchCollections = new HashMap<>();
		for(int snum = 1; snum <= numberOfStaves; snum++) {
			PitchCollection pc = new PitchCollection();

			for(Measure measure : measures) {
				for(Measurable meas : measure.getMeasureables(snum)) {
					if(meas.getType().equals(Measurable.NOTE)) {
						Note note = (Note)meas;
						pc.addPitchElement(note.getPitch());
					}
					else if(meas.getType().equals(Measurable.CHORD)) {
						Chord chord = (Chord)meas;
						pc.addPitchElement(chord.getPitchSet());
					}
					else {
						// there are no pitches in a RhythmElement
					}
				}
			}
			pitchCollections.put(snum, pc);
		}
	}
	
	public Map<Integer, PitchCollection> getPitches() {
		if(pitchCollections == null) {
			createPitchCollections();
		}
		return pitchCollections;
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
