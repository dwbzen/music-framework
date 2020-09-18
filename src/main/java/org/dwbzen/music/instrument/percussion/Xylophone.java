package org.dwbzen.music.instrument.percussion;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.instrument.MidiInstrument;

public class Xylophone extends Instrument {

	private static final long serialVersionUID = 5110848489031495457L;
	public static final String NAME = "Xylophone";
	public final static Pitch LOW_RANGE = new Pitch(Step.A, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.G, 6);
	
	public Xylophone() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	/**
	 * Xylophone is a transposing instrument, sounding one octave higher than written.
	 * In musicXML:
	 	<transpose>
     		<diatonic>0</diatonic>
     		<chromatic>0</chromatic>
     		<octave-change>1</octave-change>
    	</transpose>
	 * @param low
	 * @param high
	 */
	public Xylophone(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		setName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(14);
		cleffs.add(Cleff.G);
		setMidiProgram(14);
	}
	
	@Override
	public String getName() {
		if(name == null) {
			name = NAME;
		}
		return name;
	}

}
