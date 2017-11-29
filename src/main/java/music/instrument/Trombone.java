package music.instrument;
import music.element.Cleff;
import music.element.Pitch;

public class Trombone extends Instrument  {

	private static final long serialVersionUID = -3912912625745751978L;
	public static final String NAME = "Tenor Trombone";
	public final static Pitch LOW_RANGE = new Pitch("F2");
	public final static Pitch HIGH_RANGE = new Pitch("Bb4");

	public Trombone() {
		this(LOW_RANGE, HIGH_RANGE);
	}

	public Trombone(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "Trb.";
		setPartName("Trombone");
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(58);
		cleffs.add(Cleff.F);
		setName(NAME);
		setMidiProgram(58);
		setInstrumentName(NAME);			// default value, configurable
		setInstrumentSound("brass.trombone.tenor");	// default value, configurable
	}

}
