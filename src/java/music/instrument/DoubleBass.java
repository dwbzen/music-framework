package music.instrument;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class DoubleBass  extends Instrument {
	
	private static final long serialVersionUID = 5725894689788992537L;
	public static final String NAME = "Double Bass";
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 1);
	public final static Pitch HIGH_RANGE = new Pitch(Step.G, 3);

	public DoubleBass() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public DoubleBass(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "D.B.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(44);
		setPartName(NAME);
		setMidiProgram(44);
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
			cleffs.add(Cleff.F);
		}
		return cleffs;
	}
}
