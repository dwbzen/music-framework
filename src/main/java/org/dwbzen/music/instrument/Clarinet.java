package org.dwbzen.music.instrument;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Step;

/**
 * Range can be customized in config.properties.
 * For transposing instruments, Range is given as written pitch.<br>
 * Parts for Clarinet are written a major 2nd (2 steps) higher than it sounds.<br>
 * Parts for BassClarinet have the same written range but sound an octave + major 2nd lower than written.<br>
 * So a written C will sound a Bb.
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
		this.abreviation = "Bb Cl.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(72);
		setPartName("Bb " + NAME);
		setMidiProgram(72);
		setInstrumentName(NAME);			// default value, configurable
		setName(NAME);
		cleffs.add(Cleff.G);
		setInstrumentSound("wind.reed.clarinet");	// default value, configurable
		setTransposes(true);
	}

	@Override
	public void establishKey() {
		this.key = Key.D_MAJOR;
	}

	@Override
	/**
	 * Bb Clarinet written a major second above desired pitch
	 */
	public int getTranspositionSteps() {
		return getTransposeChromaticSteps();
	}

}
