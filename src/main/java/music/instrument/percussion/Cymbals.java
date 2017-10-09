package music.instrument.percussion;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

public class Cymbals extends Instrument {

	private static final long serialVersionUID = 2239324105509402552L;
	public static final String NAME = "Cymbals";
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 2);
	public final static Pitch HIGH_RANGE = new Pitch(Step.E, 4);

	public Cymbals() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Cymbals(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(49);	// Crash Cymbals 1
		setMidiProgram(49);
		setPitchClass(PitchClass.DISCRETE_1LINE);
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
			cleffs.add(Cleff.PERCUSSION);
		}
		return cleffs;
	}

}
