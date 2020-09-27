package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

public class PianoRH extends Instrument {
	private static final long serialVersionUID = 8520579724073011712L;
	public final static String NAME = "Piano";
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 4);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);

	public PianoRH() {
		this(LOW_RANGE, HIGH_RANGE);
	}

	public PianoRH(Pitch lowRange, Pitch highRange) {
		super(lowRange, highRange);
		this.abreviation = "Pno.";
		setPartName(NAME);
		setName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(1);
		setMidiProgram(1);
		setInstrumentName(NAME);
		setInstrumentSound("keyboard.piano.grand");
		cleffs.add(Cleff.G);
	}
}
