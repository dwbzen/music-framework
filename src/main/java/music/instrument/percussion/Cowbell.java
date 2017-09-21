package music.instrument.percussion;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.Step;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;

/**
 * Cowbells use a 2-line unpitched percussion clef with the lines corresponding
 * to 2 pitches: B3 and D4. Notation in MusicXML uses <unpitched> as in:
 *  <unpitched>
     <display-step>D</display-step>
     <display-octave>4</display-octave>
    </unpitched>
 * 
 * @author don_bacon
 *
 */
public class Cowbell  extends Instrument {

	private static final long serialVersionUID = -7747656027937632481L;
	public static final String NAME = "Cowbell";
	
	public final static Pitch LOW_RANGE = new Pitch(Step.B, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.D, 4);
	
	public Cowbell() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Cowbell(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(56);
		setMidiProgram(56);
		setPitchClass(PitchClass.DISCRETE_2LINE);
	}
	
	@Override
	public List<Cleff> getCleffs() {
		if(cleffs.size() == 0) {
			cleffs.add(Cleff.PERCUSSION_2LINE);
		}
		return cleffs;
	}

}
