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
public class BassClarinet extends Instrument  {

	private static final long serialVersionUID = 3307270020064981278L;

	public final static String NAME = "Bass Clarinet";

	/**
	 * Range can be customized in config.properties
	 * For transposing instruments, Range is given as written pitch
	 */
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public BassClarinet() {
		this(LOW_RANGE, HIGH_RANGE);	// TODO: this causes 2 initializations, here and again in ProductionFlow.configure()
	}
	
	public BassClarinet(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Bb B. Cl.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(72);
		setPartName("Bb Bass Clarinet");
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
	 * Bb Bass Clarinet written an octave + major second above desired pitch.<br>
	 * So it has the same written range as a Bb Clarinet.
	 */
	public int getTranspositionSteps() {
		return getTransposeChromaticSteps();
	}

}
