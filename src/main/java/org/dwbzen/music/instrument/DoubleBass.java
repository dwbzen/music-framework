package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

public class DoubleBass  extends Instrument {
	
	private static final long serialVersionUID = 5725894689788992537L;
	public static final String NAME = "Double Bass";
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 1);
	public final static Pitch HIGH_RANGE = new Pitch(Step.G, 3);

	public DoubleBass() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public DoubleBass(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "D.B.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(44);
		cleffs.add(Cleff.F);
		setName(NAME);
		setPartName(NAME);
		setMidiProgram(44);
	}

}
