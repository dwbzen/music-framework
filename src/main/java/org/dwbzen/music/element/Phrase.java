package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.Measurable.TieType;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.music.PitchCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Phrase is a list of measures for a particular instrument treated as a unit.<br>
 * The Instrument determines the RhythmScale and number of staves.<br>
 * Phrase includes operations such as creating a retrograde, transposition, adjusting durations, creating tone rows.</p>
 * Certain instruments require a Grand Staff (2 staves). By default, operations apply to all staves,<br>
 * or optionally constrained to a specific staff.</p>
 * Note that Measure encapsulates the staves required for the Instrument.
 * @author don_bacon
 *
 */
public class Phrase implements IJson, Cloneable {

	@JsonProperty("staves")		private int numberOfStaves = 1;		// determined by the instrument.
	@JsonProperty	private List<Measure> measures = new ArrayList<>();
	@JsonProperty	private Instrument instrument;
	@JsonIgnore		private IRhythmScale rhythmScale;
	@JsonIgnore		private	Map<Integer, PitchCollection> staffPitches = null;		// keyed by staff number, created dynamically
	@JsonIgnore		private Map<Integer, List<Duration>> durationsMap = null;		// Duration by staff number, created dynamically
	
	/**
	 * Create a Phrase with defaults
	 */
	public Phrase() {
	}
	
	public Phrase(Instrument instrument) {
		assert(instrument != null);
		setInstrument(instrument);
	}
	
	public Phrase(Instrument instrument, List<Measure> listOfMeasure) {
		this(instrument);
		measures.addAll(listOfMeasure);
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
	 * Notes retain their original duration. But ties are adjusted accordingly. <br>
	 * The notes and measures of this are unchanged.
	 * @return Phrase
	 */
	public Phrase getRetrograde() {
		return getRetrograde(false);
	}
	
	/**
	 * 
	 * @param staffNumber - the staff number to create the retrograde from. If <= 0, forms the retrograde on all staves.
	 * @param notesOnly - if true, only the pitches are reversed, the durations, ties remain the same.
	 * @return  Phrase with the designated staff/staves in retrograde.
	 */
	public Phrase getRetrograde(boolean notesOnly) {
		Phrase phrase = null;
		List<Measure> retroMeasures = new ArrayList<>();
		if(notesOnly) {
			/*
			 * keep the same note durations/types; update pitches to retrograde
			 * TODO
			 */
			phrase = clone();
		}
		else {
			phrase = new Phrase(this.instrument);
			
			int len = measures.size();
			for(int i=0; i<len; i++) {
				Measure originalMeasure = measures.get(len-i-1);
				/*
				 * copy the Measure attributes, but not the notes
				 */
				Measure retroMeasure = Measure.copy(originalMeasure);
				/*
				 * copy the Notes/Chords in reverse order
				 */
				for(int staffNumber=1; staffNumber<=numberOfStaves; staffNumber++) {
					int measureLen = originalMeasure.size();
					List<Measurable> originalMeasurables = originalMeasure.getMeasureables(staffNumber);
					for(int j=0; j<measureLen; j++) {
						Measurable orig = originalMeasurables.get(measureLen-j-1);
						Measurable mnew = orig.clone();
						retroMeasure.addMeasureable(staffNumber, mnew);
					}
				}
				retroMeasures.add(retroMeasure);
			}
		}

		phrase.measures.addAll(retroMeasures);
		return phrase;
	}
	
	public List<Duration> getDurations(int staffNumber) {
		if(durationsMap == null) {
			durationsMap = new HashMap<>();
		}
		List<Duration> durations = null;
		if(!durationsMap.containsKey(staffNumber)) {
			durations = new ArrayList<>();
			for(Measure measure: measures) {
				for(Measurable m: measure.getMeasureables(staffNumber)) {
					Duration d = m.getDuration();
					d.setNoteType(m.getNoteType());
					durations.add(d);
				}
			}
			durationsMap.put(staffNumber, durations);
		}
		return durationsMap.get(staffNumber);
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
		if(staffPitches == null) {
			createPitchCollections();
		}
		return staffPitches.get(staffNumber);
	}
	
	private void createPitchCollections() {
		staffPitches = new HashMap<>();
		for(int snum = 1; snum <= numberOfStaves; snum++) {
			for(int staffNumber = 1; staffNumber <= numberOfStaves; staffNumber++) {
				createPitchCollection(staffNumber);
			}
		}
	}
	
	private void createPitchCollection(int staffNumber) {
		PitchCollection pc = new PitchCollection();
		for(Measure measure: measures) {
			for(Measurable meas : measure.getMeasureables(staffNumber)) {
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
		staffPitches.put(staffNumber, pc);		
	}

	public Map<Integer, PitchCollection> getPitches() {
		if(staffPitches == null) {
			createPitchCollections();
		}
		return staffPitches;
	}
	
	/**
	 * Add a Measure to the Phrase
	 * @param measure - Measure to add. Must have the correct numberOfStaves for this Phrase.
	 */
	public void addMeasure(Measure measure) {
		measures.add(measure);
	}

	public List<Measure> getMeasures() {
		return measures;
	}

	public Map<Integer, PitchCollection> getStaffPitches() {
		if(staffPitches == null) {
			createPitchCollections();
		}
		return staffPitches;
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
	
}
