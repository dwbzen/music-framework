package music.instrument;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Flute  extends Instrument {

	public static final String NAME = "Flute";
	private static final long serialVersionUID = 1320783372935423746L;
	public final static Pitch LOW_RANGE = new Pitch(Step.C, 4);
	public final static Pitch HIGH_RANGE = new Pitch(Step.A, 6);	// can actually go to A7
	
	public Flute() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Flute(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Fl";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(74);
		cleffs.add(Cleff.G);
		setName(NAME);
		setPartName(NAME);
		setMidiProgram(74);
	}

}
