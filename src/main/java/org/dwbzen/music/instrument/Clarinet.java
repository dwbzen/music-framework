package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

/**
 * A B-flat clarinet sounds a whole tone lower than written.
 * 
 */
public class Clarinet extends Instrument {

	public final static String NAME = "Clarinet";
	private static final long serialVersionUID = 47389667570552052L;

	/**
	 * Range can be customized in config.properties
	 * For transposing instruments, Range is given as written pitch
	 */
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public Clarinet() {
		this(LOW_RANGE, HIGH_RANGE);	// TODO: this causes 2 initializations, here and again in ProductionFlow.configure()
	}
	
	public Clarinet(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "ClB";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(72);
		setPartName("Bb " + NAME);
		setMidiProgram(72);
		setInstrumentName(NAME);			// default value, configurable
		setName(NAME);
		cleffs.add(Cleff.G);
		setInstrumentSound("wind.reed.clarinet");	// default value, configurable
	}

	@Override
	public void establishKey() {
		this.key = Key.D_MAJOR;
		/*
		 * Note - some music notation software does this for you
		 */
		setTransposes(false);
	}

	@Override
	/**
	 * Bb Clarinet written a major second above desired pitch
	 */
	public int getTranspositionSteps() {
		// written a whole step above desired pitch
		return 2;
	}

}
