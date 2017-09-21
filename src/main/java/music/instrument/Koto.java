package music.instrument;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Koto  extends Instrument {
	private static final long serialVersionUID = -3851614751866335492L;
	public static final String NAME = "Koto";
	public final static Pitch LOW_RANGE = new Pitch(Step.G, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.A, 5);
	
	public Koto() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Koto(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Koto";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(108);
		setPartName(NAME);
		setMidiProgram(108);
		setInstrumentName(NAME);			// default value, configurable
		setInstrumentSound("pluck.koto");	// default value, configurable
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
			cleffs.add(Cleff.G);
		}
		return cleffs;
	}

}
