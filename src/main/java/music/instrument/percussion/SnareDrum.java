package music.instrument.percussion;

import music.element.Cleff;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

public class SnareDrum extends Instrument {

	private static final long serialVersionUID = -4172265466963842466L;
	public static final String NAME = "Snare Drum (2)";
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 2);
	public final static Pitch HIGH_RANGE = new Pitch(Step.E, 4);
	
	public SnareDrum() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public SnareDrum(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(40);
		cleffs.add(Cleff.PERCUSSION);
		setName(NAME);
		setMidiProgram(40);
		setPitchClass(PitchClass.DISCRETE_1LINE);
	}

}
