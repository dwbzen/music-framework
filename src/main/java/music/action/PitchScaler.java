package music.action;

import org.apache.log4j.Logger;

import mathlib.Point2D;
import music.element.Key;
import music.element.Pitch;
import music.element.PitchRange;
import music.instrument.Instrument;

/**
 * Scales a Point to a pitch for a particular instrument or range
 * 
 * @author don_bacon
 * 
 * <p>For background information see 
 * <a href="https://en.wikipedia.org/wiki/Transposing_instrument">Transposing Instrument</a> and
 * <a href="https://en.wikipedia.org/wiki/List_of_musical_instruments_by_transposition">Musical Instruments by Transposition</a> on Wikipedia
 *
 */
public class PitchScaler extends Scaler {
	
	static final org.apache.log4j.Logger log = Logger.getLogger(PitchScaler.class);
	
	private Key key = Key.C_MAJOR;
	private Pitch root = Pitch.C;
	private Rounder rounder = Rounder.ROUND;
	private int transposeSteps = 0;
	
	public PitchScaler(Instrument inst, Number maxX, Number minX) {
		super(inst, maxX, minX);
		setKey(instrument.getKey());
	}
	
	public PitchScaler(Instrument inst) {
		super(inst);
		setKey(instrument.getKey());
	}
	
	public Pitch scale(Point2D<Double> dval) {
		return scale(dval.getX());
	}
	
	public Pitch scale(double dval) {
		return scale(Double.valueOf(dval));
	}
	
	/**
	 * This takes a raw Number in a given range, scales and rounds
	 * according to the associated Instrument
	 * Scale formula: y = m * (x - minVal)
	 * where m = (stepRange - 1 ) / maxVal
	 * and x is the number to scale (which must satisfy minval <= x <= maxVal)
	 * 
	 * An instrument with a single-note range, such as Cymbals or Snare Drum,
	 * is special cased since the step range is 0.
	 * 
	 * @param num a Number in the range [maxVal, minVal]
	 * @return a Pitch in the PitchRange of the associated Instrument suitably rounded
	 */
	public Pitch scale(Number num) {
		Pitch p = null;
		if(drange == null) {
			drange = maxVal  - minVal ;
		}
		PitchRange pr = instrument.getPitchRange();
		int sr = pr.getStepRange();
		int stepRange =sr == 0 ? 0 : sr - 1;
		double m = stepRange/drange.doubleValue();
		double d = m * ( num.doubleValue() - minVal.doubleValue());
		p = round(d);	// rounds and transposes if needed for transposing instrument
		log.debug(" num: " + num + " d: " + d + "  pitch: " + p);
		if(p.compareTo(pr.getLow()) < 0) {
			log.warn("PitchScaler for " + instrument.getName() + " out of bounds " + num);
		}
		return p;
	}
	
	public Pitch round(double d) {
		int rangeNote = 0;
		switch(rounder) {
		case CEILING:
			rangeNote = (int) Math.ceil(d);
			break;
		case FLOOR:
			rangeNote = (int)Math.floor(d);
			break;
		case ROUND:
		default:
			rangeNote = (int)Math.round(d);
			break;
		};
		if(rangeNote == 5) {
			log.debug("stop");
		}
		return new Pitch(getInstrument().getNotes().get(rangeNote), transposeSteps);
	}
	

	public Key getKey() {
		return key;
	}
	
	/**
	 * The instrument's key tells which pitch will sound when the player plays a note written as C.
	 * For example, when a Bb clarinet musician plays the written note C4, the sound that is
	 * actually produced is Bb3. In order to produce a C, the musician actually plays a D.
	 * 
	 * For transposing instrument transposeSteps is the #steps below Pitch.C (so negative)
	 * For example, Bb root would return difference -2
	 * Then to transpose a note, we decrement by that amount which amounts to incrementing.
	 * A list of common transpositions:
	 * C  sounds as written
	 * D  sounds a major 2nd higher than written (+2)
	 * Eb sounds a minor 3rd higher than written (+3)
	 * F  sounds a perfect 5th lower than written (+5)
	 * G  sounds a perfect 4th lower than written (-5)
	 * A  sounds a minor 3rd lower than written (-3)
	 * Bb sounds a major 2nd lower than written (-2)
	 * Setting the Key also determines and sets transposeSteps.
	 * 
	 * @param key
	 */
	public void setKey(Key key) {
		this.key = key;
		root = key.getRoot();
		transposeSteps = 0;
		if(!Key.transpositions.containsKey(key)) {
			log.warn("Unsupport instrument Key: " + key + " no transposition set");
		}
		else {
			transposeSteps = Key.transpositions.get(key).intValue();
		}
	}
	
	public Rounder getRounder() {
		return rounder;
	}
	public void setRounder(Rounder rounder) {
		this.rounder = rounder;
	}

	public Pitch getRoot() {
		return root;
	}

	public int getTransposeSteps() {
		return transposeSteps;
	}

}
