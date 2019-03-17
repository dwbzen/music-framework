package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

public class Shamisen  extends Instrument  {

	private static final long serialVersionUID = -4993921394556102078L;
	public static final String NAME = "Shamisen";
	public final static Pitch LOW_RANGE = new Pitch(Step.G, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public Shamisen() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Shamisen(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Shamisen";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(107);
		cleffs.add(Cleff.G);
		setName(NAME);
		setPartName(NAME);
		setMidiProgram(107);
		setInstrumentName(NAME);			// default value, configurable
		setInstrumentSound("pluck.shamisen");	// default value, configurable
	}
	

}

