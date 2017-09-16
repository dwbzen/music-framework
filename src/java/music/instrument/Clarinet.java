package music.instrument;

import java.util.List;

import music.element.Cleff;
import music.element.Key;
import music.element.Pitch;
import music.element.Step;

/**
 * A B-flat clarinet sounds a whole tone lower than written.
 * 
 */
public class Clarinet extends Instrument {

	public final static String NAME = "Clarinet";
	private static final long serialVersionUID = 47389667570552052L;

	/**
	 * Range can be customized in config.properties
	 * For transposing instruments, Range is given as actual pitch (not written pitch)
	 */
	public final static Pitch LOW_RANGE = new Pitch(Step.F, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.D, 6);
	
	public Clarinet() {
		this(LOW_RANGE, HIGH_RANGE);	// TODO: this causes 2 initializations, here and again in ProductionFlow.configure()
	}
	
	public Clarinet(Pitch low, Pitch high) {
		super(low, high);
		this.abreviation = "ClB";
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(72);
		setPartName("Bb " + NAME);
		setMidiProgram(72);
		setInstrumentName(NAME);			// default value, configurable
		setInstrumentSound("wind.reed.clarinet");	// default value, configurable
	}

	@Override
	public void establishKey() {
		this.key = Key.BFlat_MAJOR;
		/*
		 * Note - some music notation software does this for you
		 */
		setTransposes(false);
	}

	@Override
	/**
	 * Bb Clarinet written a major second above desired pitch
	 */
	public int getTranspositionSteps() {
		// written a whole step above desired pitch
		return 2;
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
