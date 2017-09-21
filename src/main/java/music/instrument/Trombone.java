package music.instrument;
import java.util.List;

import music.element.Alteration;
import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Trombone extends Instrument  {

	private static final long serialVersionUID = -3912912625745751978L;
	public static final String NAME = "Tenor Trombone";
	public final static Pitch LOW_RANGE = new Pitch(Step.F, 2, Alteration.NONE);
	public final static Pitch HIGH_RANGE = new Pitch(Step.B, 4, Alteration.DOWN_ONE);

	public Trombone() {
		this(LOW_RANGE, HIGH_RANGE);
	}

	public Trombone(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Trb.";
		setPartName("Trombone");
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(58);
		setMidiProgram(58);
		setInstrumentName(NAME);			// default value, configurable
		setInstrumentSound("brass.trombone.tenor");	// default value, configurable
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
