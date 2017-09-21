package music.instrument.vocal;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

/**
 * Abstract Voice class. Subclasses determine range:
 * SopranoVoice, AltoVoice, TenorVoice, BassVoice
 * for SATB choir. The range given here is the complete
 * range of all voices. Individual ranges set by class
 * in configuration file. Ditto on the Cleff.
 * 
 * @author don_bacon
 *
 */
public abstract class Voice extends Instrument {
	
	private static final long serialVersionUID = 7514088913858185193L;
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public Voice() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Voice(Pitch low, Pitch high) {
		super(low, high);
		setPartName(getName());
		midiInstrument = new MidiInstrument("", 1,  getName());
		midiInstrument.setMidiProgram(53);
		setMidiProgram(53);
	}

	@Override
	public List<Cleff> getCleffs() {
		return cleffs;
	}
}
