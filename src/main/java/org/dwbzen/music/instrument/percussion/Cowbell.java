package org.dwbzen.music.instrument.percussion;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchClass;
import org.dwbzen.music.element.Step;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.instrument.MidiInstrument;

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
	
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 4);
	public final static Pitch HIGH_RANGE = new Pitch(Step.G, 4);
	
	public Cowbell() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public Cowbell(Pitch low, Pitch high) {
		super(low, high);
		setPartName(NAME);
		setName(NAME);
		midiInstrument = new MidiInstrument("", 1, NAME);
		midiInstrument.setMidiProgram(56);
		cleffs.add(Cleff.PERCUSSION_2LINE);
		setMidiProgram(56);
		setPitchClass(PitchClass.DISCRETE_2LINE);
	}
	
}
