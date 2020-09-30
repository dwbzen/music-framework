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
 * A written C will sound a Bb, to sound a C need to score a D.<br>
 * To do this the Clarinet is configured as:<br>
 * music.instrument.Clarinet.transpose.diatonic=1<br>
 * music.instrument.Clarinet.transpose.chromatic=2</p>
 * 
 * The transpose key (when creating the musicXML) however works the opposite with regards to<br>
 * the steps sign (positive or negative). This is handled by MusicXMLHelper and for a Clarinet<br>
 * the transpose section looks like this:<br>
 * <pre>{@code
 <transpose>
     <diatonic>-1</diatonic>
     <chromatic>-2</chromatic>
 </transpose>
 * }</pre>
 * 
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
	 * Bb Clarinet written a major second (2 chromatic steps) above desired pitch
	 */
	public int getTranspositionSteps() {
		return getTransposeChromaticSteps();
	}

}
