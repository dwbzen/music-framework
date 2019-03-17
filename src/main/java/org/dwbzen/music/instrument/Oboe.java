package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;

public class Oboe  extends Instrument {
	
	public static final String NAME = "Oboe";
	private static final long serialVersionUID = 7017425870770834795L;
	/**
	 * Range can be customized in config.properties
	 */
	public final static Pitch LOW_RANGE = new Pitch("Bb3");
	public final static Pitch HIGH_RANGE = new Pitch("Eb6");

	public Oboe() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Oboe(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Ob.";
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(69);
		cleffs.add(Cleff.G);
		setName(NAME);
		setMidiProgram(69);
		transposes = false;
	}

}
