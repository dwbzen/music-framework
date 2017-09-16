package music.instrument.percussion;

import java.util.List;

import music.element.Cleff;
import music.element.Interval;
import music.element.Key;
import music.element.Pitch;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

public class Xylophone extends Instrument {

	private static final long serialVersionUID = 5110848489031495457L;
	public static final String NAME = "Xylophone";
	public final static Pitch LOW_RANGE = new Pitch(Step.A, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.G, 6);
	
	public Xylophone() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	/**
	 * Xylophone is a transposing instrument, sounding one octave higher than written.
	 * In musicXML:
	 	<transpose>
     		<diatonic>0</diatonic>
     		<chromatic>0</chromatic>
     		<octave-change>1</octave-change>
    	</transpose>
	 * @param low
	 * @param high
	 */
	public Xylophone(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(14);
		setMidiProgram(14);
		transposes=true;
		setTransposeInterval(new Interval(12));
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
	
	@Override
	/**
	 * Transpose interval is an octave, so there is no change in Key
	 */
	public Key getKey(Key scoreKey) {
		return scoreKey;
	}

}
