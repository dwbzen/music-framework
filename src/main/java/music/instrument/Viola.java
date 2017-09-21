package music.instrument;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Viola  extends Instrument {

	private static final long serialVersionUID = -733219100147855962L;
	public static final String NAME = "Viola";
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public Viola() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Viola(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Vla.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(42);
		setPartName(NAME);
		setMidiProgram(42);	// Cello=43, Bass=44
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
		if(cleffs.size() == 0) {
			cleffs.add(Cleff.C);
		}
		return cleffs;
	}

}
