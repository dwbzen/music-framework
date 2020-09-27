package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

public class PianoLH extends Instrument {
	private static final long serialVersionUID = 4662584960835172115L;
	public final static String NAME = "Piano";
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 2);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 4);

	public PianoLH() {
		this(LOW_RANGE, HIGH_RANGE);
	}

	public PianoLH(Pitch lowRange, Pitch highRange) {
		super(lowRange, highRange);
		this.abreviation = "Pno.";
		setPartName(NAME);
		setName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(1);
		setMidiProgram(1);
		setInstrumentName(NAME);
		setInstrumentSound("keyboard.piano.grand");
		cleffs.add(Cleff.F);
	}
}
