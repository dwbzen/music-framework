package music.instrument.percussion;

import music.element.Cleff;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

public class WoodBlocks extends Instrument {

	private static final long serialVersionUID = 7119446863181988432L;
	public static final String NAME = "WoodBlocks";
	/**
	 * DISCRETE_5LINE constrained to 5 pitches corresponding to
	 * Percussion clef lines. From top to bottom: F5, D5, B4, G4, E4
	 * Basically a special scale independent of PITCHed scales.
	 */
	public final static Pitch LOW_RANGE = new Pitch(Step.F, 5);
	public final static Pitch HIGH_RANGE = new Pitch(Step.E, 4);
	
	public WoodBlocks() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public WoodBlocks(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(116);	// Woodblock
		cleffs.add(Cleff.PERCUSSION);
		setName(NAME);
		setMidiProgram(116);
		setPitchClass(PitchClass.DISCRETE_5LINE);
	}
	
}
