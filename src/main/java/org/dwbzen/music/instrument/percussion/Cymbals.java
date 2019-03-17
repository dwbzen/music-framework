package org.dwbzen.music.instrument.percussion;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchClass;
import org.dwbzen.music.element.Step;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.instrument.MidiInstrument;

public class Cymbals extends Instrument {

	private static final long serialVersionUID = 2239324105509402552L;
	public static final String NAME = "Cymbals";
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 2);
	public final static Pitch HIGH_RANGE = new Pitch(Step.E, 4);

	public Cymbals() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Cymbals(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		setName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(49);	// Crash Cymbals 1
		cleffs.add(Cleff.PERCUSSION);
		setMidiProgram(49);
		setPitchClass(PitchClass.DISCRETE_1LINE);
	}

}
