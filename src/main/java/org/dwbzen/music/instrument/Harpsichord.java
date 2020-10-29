package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

/**
 * Range is A0 to C8 notated on a Grand Staff.
 * 
 * @author don_bacon
 *
 */
public class Harpsichord extends Instrument {

	private static final long serialVersionUID = 1073358282424682321L;
	public final static String NAME = "Harpsichord";
	public final static Pitch LOW_RANGE = new Pitch(Step.A, 0);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 8);
	
	public Harpsichord() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Harpsichord(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		setName(NAME);
		abreviation = "Hpsd.";
		midiInstrument = new MidiInstrument("", 7, NAME);
		midiInstrument.setMidiProgram(7);
		setMidiProgram(1);
		setInstrumentName(NAME);
		setInstrumentSound("keyboard.harpsichord");
		numberOfStaves = 2;
		cleffs.add(Cleff.G);		// staff 1
		cleffs.add(Cleff.F);		// staff 2
		setKey(Key.C_MAJOR);
	}

}

