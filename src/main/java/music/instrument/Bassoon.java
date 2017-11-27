package music.instrument;

import music.element.Alteration;
import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;

public class Bassoon extends Instrument {

	public static final String NAME = "Bassoon";
	private static final long serialVersionUID = 1320783372935423746L;
	public final static Pitch LOW_RANGE = new Pitch(Step.B, 1, Alteration.DOWN_ONE);
	public final static Pitch HIGH_RANGE = new Pitch(Step.B, 4, Alteration.DOWN_ONE);

	public Bassoon() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Bassoon(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Bsn";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(71);
		cleffs.add(Cleff.F);
		setPartName(NAME);
		setName(NAME);
		setMidiProgram(71);
	}

}
