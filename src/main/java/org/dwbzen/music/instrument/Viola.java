package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

public class Viola  extends Instrument {

	private static final long serialVersionUID = -733219100147855962L;
	public static final String NAME = "Viola";
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public Viola() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Viola(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Vla.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(42);
		cleffs.add(Cleff.C);
		setName(NAME);
		setPartName(NAME);
		setMidiProgram(42);	// Cello=43, Bass=44
		setInstrumentName(NAME);
		setInstrumentSound("strings.viola");
	}

}
