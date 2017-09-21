package music.instrument;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

/**
 * Range is A0 to C8
 * @author dbacon
 *
 */
public abstract class Piano extends Instrument {

	private static final long serialVersionUID = -5031830056618215582L;
	public final static String NAME = "Piano";
	public final static Pitch LOW_RANGE = new Pitch(Step.A, 0);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 8);
	
	public Piano() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Piano(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(1);
		setMidiProgram(1);
	}
	
	@Override
	public String getName() {
		if(name == null) {
			name = NAME;
		}
		return name;
	}

	@Override
	public List<Cleff> getCleffs() {
		return cleffs;
	}
}
