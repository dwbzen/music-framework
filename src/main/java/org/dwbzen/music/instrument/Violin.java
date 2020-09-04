package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

public class Violin  extends Instrument {
	
	private static final long serialVersionUID = 4213736476200509616L;
	public static final String NAME = "Violin";
	public final static Pitch LOW_RANGE = new Pitch(Step.G, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.G, 6);
	
	public Violin() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Violin(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Vl";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(41);
		cleffs.add(Cleff.G);
		setName(NAME);
		setPartName(NAME);
		setMidiProgram(41);
		setInstrumentName(NAME);
		setInstrumentSound("strings.violin");
	}

}
