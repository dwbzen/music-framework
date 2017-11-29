package music.instrument;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Cello  extends Instrument {
	
	private static final long serialVersionUID = -1974532251844054125L;
	public static final String NAME = "Cello";
	public final static Pitch LOW_RANGE = new Pitch(Step.D, 2);
	public final static Pitch HIGH_RANGE = new Pitch(Step.C, 5);

	public Cello() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Cello(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Vc.";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(43);
		cleffs.add(Cleff.F);
		setPartName(NAME);
		setName(NAME);
		setMidiProgram(43);
	}

}
