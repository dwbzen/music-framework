package music.instrument;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Shakuhachi  extends Instrument  {

	private static final long serialVersionUID = 6292988077300679100L;
	public static final String NAME = "Shakuhachi";
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 4);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 6);
	
	public Shakuhachi() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Shakuhachi(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Shakuhachi";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(76);
		setPartName(NAME);
		setMidiProgram(76);
		setInstrumentName(NAME);					// default value, configurable
		setInstrumentSound("wind.flutes.shakuhachi");	// default value, configurable
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
