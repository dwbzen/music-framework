package music.instrument.percussion;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

public class Timpani extends Instrument {

	private static final long serialVersionUID = -1893145019330011321L;
	public static final String NAME = "Timpani";
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 2);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 4);
	
	public Timpani() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Timpani(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(48);
		setMidiProgram(48);
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
