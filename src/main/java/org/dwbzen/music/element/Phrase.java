package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.music.instrument.Instrument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Phrase is a list of measures treated as a unit.<br>
 * It can optionally be associated with a specific Instrument and/or RhythmScale.<br>
 * Phrase includes operations such as creating a retrograde, adjusting durations, and transposition.</p>
 * Certain instruments require a Grand Staff (2 staves). By default any operations apply to all staves,<br>
 * or optionally constrained to a specific staff.</p>
 * 
 * @author don_bacon
 *
 */
public class Phrase {

	@JsonProperty("staves")		private int numberOfStaves = 1;		// set to 2 for Grand Staff or as required by the instrument.
	@JsonProperty	private List<Measure> measures = new ArrayList<Measure>();
	@JsonProperty	private Instrument instrument;
	@JsonIgnore		private IRhythmScale rhythmScale;
	
	/**
	 * Create a Phrase with defaults
	 */
	public Phrase() {
	}
	
	public Phrase(Instrument instrument) {
		numberOfStaves = instrument.getCleffs().size();
		this.instrument = instrument;
		rhythmScale = instrument.getRhythmScale();
	}
	
	public void addMeasure(Measure measure) {
		measures.add(measure);
	}
}
