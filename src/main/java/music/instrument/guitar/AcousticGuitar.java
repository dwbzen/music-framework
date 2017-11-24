package music.instrument.guitar;

import java.util.List;

import music.element.Cleff;
import music.element.Pitch;
import music.element.Step;
import music.instrument.Instrument;

/**
 * Guitar sounds 1 octave (8ve) lower than written.
 * Lower end of range determined by the tuning.
 * 
 * @author don_bacon
 *
 */
public class AcousticGuitar extends Instrument {

	private static final long serialVersionUID = 471314417767037863L;
	public final static Pitch LOW_RANGE = new Pitch(Step.E, 3);
	public final static Pitch HIGH_RANGE = new Pitch(Step.E, 6);
	public final static String NAME = "Guitar";
	
	private GuitarTuning guitarTuning = GuitarTuning.STANDARD;
	
	public AcousticGuitar() {
		this(LOW_RANGE, HIGH_RANGE);
	}
	
	public AcousticGuitar(Pitch low, Pitch high) {
		super(low, high);
		cleffs.add(Cleff.G);
		setName(NAME);
	}
	
	public List<Cleff> getCleffs() {
		return cleffs;
	}

	public GuitarTuning getGuitarTuning() {
		return guitarTuning;
	}

	public void setGuitarTuning(GuitarTuning guitarTuning) {
		this.guitarTuning = guitarTuning;
	}

}
