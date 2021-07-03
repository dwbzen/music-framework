package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * A Pitch has 3 attributes: <br>
 * 	Step (A, B, .... G)<br>
 * 	Alteration (0, 1, -1, 2, -2) corresponding to no alteration, sharp, flat, double sharp, double flat<br>
 * 	octave (0, 1, ... 9)</p>
 * Middle-C is C4, C0 is in the range of about 16 Hz.<br>
 * Note that this uses the Scientific octave naming system which starts on C0 and extends to C9.<br>
 * With this designation, middle-C is C4 and a piano range is A0 to C7 so C4 follows B3 in this notation.</p>
 * 
 * The range is C0 to C9 (although the full range chromatic Scale starts at A0).<br>
 * Starting at C instead of A is somewhat problematic for the boundary condition:<br>
 * Cb and B# The convention is that the letter name is first combined <br>
 * with the Arabic numeral to determine a specific pitch, which is then altered by applying accidentals.<br>
 * So Cb4 is one semitone down from C4 which is B3.
 * Similarly, B#4 is the same pitch as C5.</p>
 * 
 * Use copy constructors or clone() to create new pitches from existing.
 * 
 * <p>See <a href="https://en.wikipedia.org/wiki/Scientific_pitch_notation">Scientific Pitch Notation</a> on Wikipedia.</p>
 */
public final class Pitch  extends PitchElement implements Comparable<Pitch> {

	static final org.apache.log4j.Logger log = Logger.getLogger(Pitch.class);

	/**
	 * Note for all 12 Steps
	 */
	public static final String[] PITCH_NOTES = {"C", "D", "E", "F", "G", "A", "B", ""};
	public static final Step[] PITCH_STEPS = {Step.C, Step.D, Step.E, Step.F, Step.G, Step.A, Step.B, Step.C };
	public static final Step[] PITCH_STEPS_CHROMATIC_SHARP =
		{Step.C, Step.C, Step.D, Step.D, Step.E, Step.F, Step.F, Step.G, Step.G, Step.A, Step.A, Step.B, Step.C };
	public static final Step[] PITCH_STEPS_CHROMATIC_FLAT =
		{Step.C, Step.D, Step.D, Step.E, Step.E, Step.F, Step.G, Step.G, Step.A, Step.A, Step.B, Step.B, Step.C };
	public static final Step[] PITCH_STEPS_CHROMATIC_MIXED =
		{Step.C, Step.C, Step.D, Step.E, Step.E, Step.F, Step.F, Step.G, Step.G, Step.A, Step.B, Step.B, Step.C };

	
	public static final Alteration[] ALTERATION_CHROMATIC_SHARP =
		{Alteration.NONE, Alteration.UP_ONE, Alteration.NONE, Alteration.UP_ONE, Alteration.NONE, Alteration.NONE,
		 Alteration.UP_ONE, Alteration.NONE, Alteration.UP_ONE, Alteration.NONE, Alteration.UP_ONE, Alteration.NONE, Alteration.NONE };
	
	public static final Alteration[] ALTERATION_CHROMATIC_FLAT =
		{Alteration.NONE, Alteration.DOWN_ONE, Alteration.NONE, Alteration.DOWN_ONE, Alteration.NONE, Alteration.NONE,
		Alteration.DOWN_ONE, Alteration.NONE, Alteration.DOWN_ONE, Alteration.NONE, Alteration.DOWN_ONE, Alteration.NONE, Alteration.NONE};
	
	public static final Alteration[] ALTERATION_CHROMATIC_MIXED =
		{Alteration.NONE, Alteration.UP_ONE, Alteration.NONE, Alteration.DOWN_ONE, Alteration.NONE, Alteration.NONE,
		 Alteration.UP_ONE, Alteration.NONE, Alteration.UP_ONE, Alteration.NONE, Alteration.DOWN_ONE, Alteration.NONE, Alteration.NONE };

	public static final String[] SHARP = {"", "#", "##"};		// {"", "\u266F", "\u1D12A"};
	public static final String[] FLAT = {"", "b", "bb"};		// {"", "\u266D", "\u1D12B"};
	
	
	static List<String> notes = new ArrayList<String>();
	static {
		for(String n : PITCH_NOTES) {notes.add(n); }
	};
	static Map<Alteration, PitchSet> allPitchesMap = PitchSet.allPitches;

	/**
	 * Octave-neutral pitch definitions with rangeStep 
	 */
	public static final Pitch C = new Pitch(Step.C, Alteration.NONE);
	public static final Pitch D = new Pitch(Step.D, Alteration.NONE);
	public static final Pitch E = new Pitch(Step.E, Alteration.NONE);
	public static final Pitch F = new Pitch(Step.F, Alteration.NONE);
	public static final Pitch G = new Pitch(Step.G, Alteration.NONE);
	public static final Pitch A = new Pitch(Step.A, Alteration.NONE);
	public static final Pitch B = new Pitch(Step.B, Alteration.NONE);
	public static final Pitch BFlat = new Pitch(Step.B, Alteration.DOWN_ONE);
	public static final Pitch EFlat = new Pitch(Step.E, Alteration.DOWN_ONE);
	public static final Pitch AFlat = new Pitch(Step.A, Alteration.DOWN_ONE);
	public static final Pitch DFlat = new Pitch(Step.D, Alteration.DOWN_ONE);
	public static final Pitch GFlat = new Pitch(Step.G, Alteration.DOWN_ONE);
	public static final Pitch CFlat = new Pitch(Step.C, Alteration.DOWN_ONE);
	public static final Pitch FFlat = new Pitch(Step.F, Alteration.DOWN_ONE);
	public static final Pitch CSharp = new Pitch(Step.C, Alteration.UP_ONE);
	public static final Pitch DSharp = new Pitch(Step.D, Alteration.UP_ONE);
	public static final Pitch ESharp = new Pitch(Step.E, Alteration.UP_ONE);
	public static final Pitch FSharp = new Pitch(Step.F, Alteration.UP_ONE);
	public static final Pitch GSharp = new Pitch(Step.G, Alteration.UP_ONE);
	public static final Pitch ASharp = new Pitch(Step.A, Alteration.UP_ONE);
	public static final Pitch BSharp = new Pitch(Step.B, Alteration.UP_ONE);
	
	/**
	 * lowest possible pitch, range step = 0
	 */
	public static final Pitch C0 = new Pitch("C0");
	/**
	 * highest possible pitch, range step = 120
	 */
	public static final Pitch C9 = new Pitch("C9");
	
	public static final Pitch maxPitch = C9;
	public static final Pitch minPitch = C0;

	@JsonPropertyOrder({"step","octave","alteration","rangeStep"})
	@JsonProperty("step")			private Step step;
	/**
	 * octave number in standard scientific notation starting at 0
	 * if octave < 0, pitch is octave-neutral
	 */
	@JsonProperty("octave")			private int octave = 0;
	@JsonProperty("alteration") 	private int alteration = 0;
	
	/**
	 * Number of steps away from C0, a number >=0 and <pitchRange
	 */
	@JsonProperty("rangeStep")		private int rangeStep = 0;
	/**
	 * Need something to represent a "silent" Pitch, a.k.a. a rest
	 * This is set arbitrarily to octave 0, alteration 0, Step.SILENT
	 */
	public static Pitch SILENT = new Pitch(Step.SILENT, 0, Alteration.NONE);
	/**
	 * Need something to represent a NULL Pitch - for instances when a NULL can't be used as in a Map
	 * This is set arbitrarily to octave -1, alteration 0, Step.SILENT
	 */
	public static Pitch NULL_VALUE = new Pitch(Step.SILENT, -1, Alteration.NONE);
	
	public static int pitchRange = 120;	// C0 to C9
	
	public Pitch() {
		pitchElementType = PitchElementType.PITCH;
	}
	
	public Pitch(Step s, int oct, Alteration alt) {
		this();
		step = s;
		octave = oct;
		alteration = alt.value();
		if(octave >= 0) {		// if not octave-neutral set the range step, otherwise it's 0
			setRangeStep();
		}
	}
	
	public Pitch(Step s, int rangestep, int oct, int alt) {
		this();
		step = s;
		octave = oct;
		alteration = alt;
		rangeStep = rangestep;
	}
	
	public Pitch(Step s, int oct, int alt) {
		this();
		step = s;
		octave = oct;
		alteration = alt;
		if(octave >= 0) {		// if not octave-neutral set the range step, otherwise it's 0
			setRangeStep();
		}
	}

	public  Pitch(Step s, int oct) {
		this(s, oct, Alteration.NONE);
	}
	
	/**
	 * Construct an octave-neutral Pitch - a pitch without an octave designation
	 * Sort of an Abstract pitch as it were.
	 * 
	 * @param s Step
	 * @param alt Alteration
	 */
	public Pitch(Step s, Alteration alt) {
		this();
		step = s;
		octave = -1;
		alteration = alt.value();
		setRangeStep();
	}
	
	/**
	 * Copy constructor.
	 * @param other Pitch to copy (alteration, step, octave)
	 */
	public Pitch(Pitch other) {
		this();
		step =  other.getStep();
		octave = other.getOctave();
		alteration = other.getAlteration();
		rangeStep = other.getRangeStep();
	}
	
	/**
	 * Copy constructor.
	 * @param other Pitch to copy (alteration, step)
	 * @param octave octave for new Pitch
	 */
	public Pitch(Pitch other, int transposeSteps, Alteration alterationPreference) {
		this();
		step =  other.getStep();
		octave = other.getOctave();
		alteration = other.getAlteration();
		setRangeStep();
		adjustPitch(transposeSteps, alterationPreference);
	}
	
	/**
	 * Copy constructor.
	 * @param other Pitch to copy (alteration, step)
	 * @param octave octave for new Pitch
	 */
	public Pitch(Pitch other, int transposeSteps) {
		this(other, transposeSteps, Alteration.NONE);
	}

	/**
	 * Creates a Pitch from a standard string format as in C9, D#3, Eb2, Gb etc.
	 * or note + optional accidental + optional octave
	 * @param s note in standard scientific format
	 */
	public Pitch(String s) {
		this(Pitch.fromString(s));
		setRangeStep();
	}
	
	public void copy(Pitch other) {
		setAlteration(other.alteration);
		setOctave(other.octave);
		setRangeStep(other.rangeStep);
		setStep(other.step);
	}
	
	/**
	 * Makes a clone of this
	 */
	@Override
	public Pitch clone() {
		return new Pitch(this);
	}
	
	/**
	 * Computes the number of chromatic steps to get to other Pitch starting from this Pitch, essentially other-this.
	 * For example, B4.difference(E4) is 5<br>
	 * B4.difference(E3) is -7 (B4 down to E3)<br>
	 * E3.difference(B4) is 7 (E3 up to B4)<br>
	 * If == 0, the step is the same<br>
	 * If < 0, other is below this<br>
	 * else other is above this<br>
	 * If this and other have octave settings
	 * @param other
	 * @return int difference of rangeSteps
	 */
	public int difference(Pitch other) {
		return other.getRangeStep() - getRangeStep();
	}
	
	/**
	 * Computes Pitch difference relative to a given Key
	 * This method used for transposing instruments where Key is
	 * the instrument Key (Bb Clarinet for example).
	 * The result is the transposeSteps for the Key
	 * If Key is not an instrument Key, a warning is displayed and 0 transposeSteps added.
	 * 
	 * @param Pitch other
	 * @param Key - the instrument Key
	 */
	public int difference(Pitch other, Key key) {
		int transposeSteps = 0;
		if(!Key.transpositions.containsKey(key)) {
			log.warn("Unsupport instrument Key: " + key + " no transposition set");
		}
		else {
			transposeSteps = Key.transpositions.get(key).intValue();
		}
		return transposeSteps;
	}
	
	/**
	 * A class instance method for PitchRange.stepsBetween(this, Pitch other)
	 * @param other
	 * @return
	 */
	public int absoluteDifference(Pitch other) {
		return difference(other);
	}
	
	/**
	 * Starting with this pitch, this returns the number of steps it takes to reach other.<br>
	 * 
	 * @param Pitch other
	 * @return int step difference between this and other
	 */
	public int stepDifference(Pitch other) {
		int diff = other.rangeStep - rangeStep;
		return diff >= 0 ? diff : diff + 12;
	}

	/**
	 * Adds n steps to this. If this is octave neutral, the resulting Pitch will also have octave == -1.<br>
	 * Also adjusts the octave if needed and insures the alteration preference is honored.
	 * NOTE - this is not altered.
	 * @param n #steps to increment, must be >= 0
	 * @param altPref 0 = no preference, -1 = flats, 1 = sharps
	 * @return a new Pitch altered
	 */
	public Pitch increment(int n, int altPref) {
		Pitch p = clone();
		p.increment(n);
		if(altPref != p.alteration) {
			p.setEnharmonicEquivalent();
		}
		return p;
	}
	
	@Override
	/**
	 * Adds n steps to this. If this is octave neutral, the resulting Pitch will also have octave == -1.<br>
	 * Also adjusts the octave if needed.
	 * @param n number of steps to increment. If < 0, decrements by that amount.
	 */
	public void increment(int n)  {
		if (n != 0) {
			int rs = rangeStep + n;
			Pitch temp = null;
			if(rs >= 0 && rs <= 120) {		// otherwise out of bounds
				temp = allPitchesMap.get(alteration <= 0 ? Alteration.FLAT : Alteration.SHARP).getPitch(rs);
			}
			else {
				temp = (rs < 0) ? minPitch : maxPitch;
			}
			copy(temp);
		}
	}
	
	/**
	 * Adds n steps to this. If this is octave neutral, the resulting Pitch will also have octave == -1.<br>
	 * This retains the existing octave. For example  B1.incrementingPitchOnly(3,0) is D1 instead of D2.
	 * NOTE - this is not altered.
	 * @param n number of steps to increment. If < 0, decrements by that amount.
	 * @param altPref alteration preference: 0 = no preference, 1 = sharps, -1 = flats)
	 * @return a new Pitch altered
	 */
	public Pitch incrementPitchOnly(int n, int altPref)  {
		int myOctave = this.octave;
		Pitch p = increment(n, altPref);
		p.setOctave(myOctave);
		return p;
	}
	
	/**
	 * Lowers this by n steps,  adjusts the range step and sets the enharmonic equivalent of the adjusted pitch.<br>
	 * Also adjusts the octave if needed.
	 * @param n number of steps to decrement, must be >= 0
	 * @param altPref new alteration preference: 0 = no preference, 1 = sharps, -1 = flats)
	 * @return a new Pitch altered
	 */
	public Pitch decrement(int n, int altPref) {
		Pitch p = clone();
		p.decrement(n);
		if(altPref != p.alteration) {
			p.setEnharmonicEquivalent();
		}
		return p;
	}
	
	@Override
	/**
	 * Lowers this by n steps.
	 * Also adjusts the octave if needed.
	 * @param n  number of steps to decrement, if < 0 increments by that amount.
	 */
	public void decrement(int n) {
		if (n != 0) {
			int rs = this.rangeStep - n;
			Pitch temp = null;
			if(rs >= 0 && rs <= 120) {		// otherwise out of bounds
				temp = allPitchesMap.get(alteration <= 0 ? Alteration.FLAT : Alteration.SHARP).getPitch(rs);
			}
			else {
				temp = (rs < 0) ? minPitch : maxPitch;
			}
			copy(temp);
		}
	}
	
	/**
	 * Lowers this by n steps. If this is octave neutral, the resulting Pitch will also have octave == -1.<br>
	 * This retains the existing octave. For example  D2.decrementPitchOnly(3, -1) is Bb2 instead of Bb1.
	 * NOTE - this is not altered.
	 * @param n number of steps to increment. If < 0, decrements by that amount.
	 * @param altPref alteration preference: 0 = no preference, 1 = sharps, -1 = flats)
	 * @return a new Pitch altered
	 */
	public Pitch decrementPitchOnly(int n, int altPref)  {
		int myOctave = this.octave;
		Pitch p = decrement(n, altPref);
		p.setOctave(myOctave);
		return p;
	}

	/**
	 * Adjusts this Pitch by the number of steps indicated
	 * @param numberOfSteps
	 * @param alteration alteration preference - sharps (UP_ONE or SHARP) or flats (DOWN_ONE or FLAT) or NONE. If null, no preference indicated.
	 */
	public void adjustPitch(int numberOfSteps, Alteration alteration) {
		int altPreference = (alteration != null) ? alteration.value() : 0;
		adjustPitch(numberOfSteps);
		if(altPreference != alteration.value()) {
			setEnharmonicEquivalent();
		}
	}
	
	@Override
	/**
	 * Adjusts this Pitch by the number of steps indicated
	 * @param numberOfSteps
	 */
	public void adjustPitch(int numberOfSteps) {
		if(numberOfSteps >= 0 ) {
			increment(numberOfSteps);
		}
		else {
			decrement(-numberOfSteps);
		}
	}
	
	/**
	 * Implements Pitch + 1, adds 1 step and returns the new pitch
	 * @return new Pitch one step up from the original which is not changed.
	 */
	public Pitch upOneStep() {
		int oct = getOctave();
		Alteration alt = Alteration.NONE;
		Step s = null;
		int nstep = getStep().value() + getAlteration() + 1;
		if(nstep > 12) {
			nstep = 1;
			if(octave >= 0) {	// otherwise the pitch is octave-neutral
				oct+=1;
			}
		}
		s = PITCH_STEPS_CHROMATIC_SHARP[nstep-1];
		alt = ALTERATION_CHROMATIC_SHARP[nstep-1];
		return new Pitch(s, oct, alt);
	}
	
	public Step getStep() {
		return step;
	}
	
	public int getStepValue() {
		return getStep().getValue();
	}

	public int getOctave() {
		return octave;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	@Override
	public void setOctave(int octave) {
		this.octave = octave;
		setRangeStep();
	}

	public void setAlteration(int alteration) {
		this.alteration = alteration;
	}

	public void setRangeStep(int rangeStep) {
		this.rangeStep = rangeStep;
	}

	public int getAlteration() {
		return alteration;
	}
	
	public int getRangeStep() {
		return rangeStep;
	}
	
	private void setRangeStep() {
		if(!getStep().equals(Step.SILENT)) {
			int sind = getStep().getValue() + alteration;
			int ind = sind;
			if(sind <=0) {
				ind = 12 + sind;
			}
			else if(sind > 12) {
				ind = sind -12; 
			}
			// adjust octave used to calculate rangeStep for Cb and B#
			int oct = octave;
			if(step.equals(Step.C) && alteration < 0) {
				oct = octave-1;
			}
			else if(step.equals(Step.B) && alteration > 0) {
				oct = octave+1;
			}
			rangeStep = (ind - 1) + 12*(oct >=0 ? oct : 0);
		}
	}

	/**
	 * Gets the chromatic scale degree, a number from 1  to  12
	 * relative to key of C
	 * For Key of C: C = 1
	 * For example, Bb would be 11, B is 12, F# would be 7
	 * This converts Cb and B# appropriately:
	 * B# is 1 (really a C)
	 * Cb is 12 (really a B)
	 * @return scale degree (1 -12) from C
	 */
	@JsonIgnore public int getChromaticScaleDegree() {
		int d = step.value() + getAlteration();
		if(d < 0) { d=1-d;}
		else if(d>12 || d==0) { d=12-d;}
		
		return d;
	}
	
	/**
	 * Gets the chromatic scale degree (1 -12)
	 * relative to a given Key
	 * @param key
	 * @return scale degree (1 -12) from root of Key
	 */
	public int getChromaticScaleDegree(Key key) {
		int csd = getChromaticScaleDegree();
		int rsd = key.getRoot().getStep().value() + key.getRoot().getAlteration();
		int d = csd - rsd;
		if(d < 0) { 
			d=12 + d;
		}
		return d + 1;
	}
	/**
	 * Takes octave into account (assuming >= 0).
	 * C0 is the lowest note and has an absolute chromatic scale degree of 1
	 * B0 would be 12. C1 would be 13 etc.
	 */
	@JsonIgnore public int getAbsoluteChromaticScaleDegree() {
		int d = step.value() + getAlteration();
		int noctave = getOctave();
		if(d < 0) { d=1-d;}
		else if(d>12) { d=d-12; ++noctave; }
		else if(d==0) { d=12; --noctave; }
		
		if(noctave >=0) {
			d += 12*noctave;
		}
		return d;
	}
	
	@Override
	/**
	 * The ordering of Pitches is {C=C, D=D, E=E, F=F, G=G, A=A, B=B}
	 * Comparison basis is rangeStep which is the no. steps from C0.
	 * @return -1, 0 or 1 for this<other, this==other, this>other respectively.
	 */
	public int compareTo(Pitch other) {
		int sd = getRangeStep();
		int sdOther =  other.getRangeStep();
		int ret = (sd==sdOther) ? 0 : (sd<sdOther) ? -1 : 1;

		return ret;
	}
	
	/**
	 * Compares this Pitch with another. It must take into account enharmonic equivalence.
	 * Meaning Bb is the same pitch as A#
	 * @param other
	 * @return true if equal or enharmonically equivalent and in same octave
	 * TODO when testing pitches where one is octave-neutral and the other isn't
	 * TODO should test as if both were octave neutral
	 */
	public boolean equals(Pitch other) {
		int sd = getAbsoluteChromaticScaleDegree();
		int sdOther =  other.getAbsoluteChromaticScaleDegree();
		return (sd==sdOther);
	}

	/**
	 * Transpose this up n ocatves and return as a new Pitch
	 * @param n
	 * @return transposed Pitch
	 */
	public Pitch transposeUp(int n) {
		Pitch p = new Pitch(this);
		p.octave += n;
		return p;
	}
	/**
	 * Transpose this down n ocatves and return as a new Pitch
	 * @param n
	 * @return transposed Pitch
	 */
	public Pitch transposeDown(int n) {
		Pitch p = new Pitch(this);
		p.octave = (octave-n >= 0) ? octave-n : 0;
		return p;
	}
	
	public String toString() {
		return toString(false);
	}
	/**
	 * The Pitch in scientific format. 
	 * Uses #s  for accidental when alteration >= 1
	 * and flats (b) when alteration <= -1
	 * @return String representation of this Pitch
	 */
	public String toString(boolean quote) {
		int ordinal = step.getOrdinal();
		StringBuilder sb = (quote) ? new StringBuilder("\"" + PITCH_NOTES[ordinal]) : new StringBuilder( PITCH_NOTES[ordinal]);
		int alt = getAlteration();
		sb.append ((alteration == 0) ? "" : (alteration < 0) ? FLAT[-alt] : SHARP[alt]);
		if(octave >= 0) { sb.append(String.valueOf(octave)); }
		if(quote) { sb.append("\""); }
		return sb.toString();
	}
	
	/**
	 * The Pitch in scientific format with a given octave (could be <0 which omits the octave)
	 * Uses #s  for accidental when alteration >= 1
	 * and flats (b) when alteration <= -1
	 * @return String representation of this Pitch
	 */
	public String toString(int octAve) {
		StringBuffer sb = new StringBuffer( PITCH_NOTES[this.step.getOrdinal()]);
		int alt = getAlteration();
		sb.append ((alteration == 0) ? "" : (alteration < 0) ? FLAT[-alt] : SHARP[alt]);
		if(octAve >= 0) { sb.append(String.valueOf(octAve)); }
		return sb.toString();
	}
	
	@Override
	/**
	 * Sample output (Ab):<br>
	 * {"step":"A","octave":-1,"alteration":-1,"rangeStep":8,"stepValue":10,"octaveNeutral":true}
	 */
	 public String toJson() { 
		StringBuilder sb = new StringBuilder("{\"step\":"); 
		sb.append("\"" + toString() + "\",");
		sb.append("\"octave\":" + octave + ",");
		sb.append("\"alteration\":" + alteration + ",");
		sb.append("\"rangeStep\":" + rangeStep + ",");
		sb.append("\"stepValue\":" + step.value() + ",");
		sb.append("\"octaveNeutral\":" + isOctaveNeutral() + "}");
				
	 	return sb.toString();
	 }
	
	/**
	 * Creates a new Pitch that is the enharmonic equivalent of this
	 * @param alt if>0, make a sharp a flat or natural; if <0 make a flat a sharp
	 * @return new Pitch with alteration adjusted as needed
	 */
	public Pitch setEnharmonicEquivalent(int alt) {
		Pitch p = clone();
		p.setEnharmonicEquivalent();
		return p;
	}
	
	private void setEnharmonicEquivalent() {
		if(alteration > 0) {
			// make a sharp a flat (or natural)
			alteration *= -1;
			switch(step) {
			case A:	// A# -> Bb
			case ASHARP:
			case BFLAT:
			case CFLAT:
				step = Step.B;
				break;
			case B: // B# -> C
			case BSHARP:
				step = Step.C;
				if(octave > 0) { octave--;}
				alteration = 0;
				break;
			case C:  // C# -> Db
			case CSHARP:
			case DFLAT:
				step = Step.D;
				break;
			case D: // D# -> Eb
			case DSHARP:
			case EFLAT:
			case FFLAT:
				step = Step.E;
				break;
			case E: // E# -> F
			case ESHARP:
				step = Step.F;
				alteration = 0;
				break;
			case F: // F# -> Gb
			case FSHARP:
			case GFLAT:
				step = Step.G;
				break;
			case G: // G# -> Ab
			case GSHARP:
			case AFLAT:
				step = Step.A;
				break;
			case SILENT:
				step = Step.SILENT;
				break;
			}
		}
		else if(alteration < 0) {
			// make a flat a sharp
			alteration *= -1;
			switch(step) {
			case A:	// Ab -> G#
			case GSHARP:
			case AFLAT:
				step = Step.G;
				break;
			case B: // Bb -> A#
			case ASHARP:
			case BFLAT:
				step = Step.A;
				break;
			case C: // Cb -> B
			case CFLAT:
				step = Step.B;
				if(octave > 0) { octave++;}
				alteration = 0;
				break;
			case D: // Db -> C#
			case CSHARP:
			case BSHARP:
			case DFLAT:
				step = Step.C;
				break;
			case E:	// Eb -> D#
			case DSHARP:
			case EFLAT:
				step = Step.D;
				break;
			case F: // Fb -> E
			case FFLAT:
				step = Step.E;
				break;
			case G: // Gb -> F#
			case FSHARP:
			case ESHARP:
			case GFLAT:
				step = Step.F;
				break;
			case SILENT:
				step = Step.SILENT;
				break;
			}
		}
		else { // alteration == 0
			// F, C, G are sharps. B, E are flats
			// Gb -> F#, Db -> C#, Ab -> G#
			// A# -> Bb, D# -> Eb
		}
	}
	
	/**
	 * Parses pitch strings that may or may not include an octave or accidental.
	 * For example: Bb3, C4, F#, etc.
	 * Note that B# and Cb are treated as special cases being boundary pitches in Scientific pitch notation.
	 * @param s pitch String
	 * @return Pitch instance
	 */
	public static Pitch fromString(String s) {
		int ind = notes.indexOf(s.toUpperCase().substring(0, 1));
		Step step = PITCH_STEPS[ind];
		Alteration alt = Alteration.NONE;
		int slen = s.length();
		int oct = -1;
		char oc = s.charAt(slen-1);
		if(oc >= '0' && oc <= '9') {
			oct = Integer.parseInt(String.valueOf(oc));
		}
		if(slen >=3 ) {
			if(s.substring(1, 3).equals(SHARP[2])) {
				alt = Alteration.UP_TWO;
			}
			else if(s.substring(1, 3).equals(FLAT[2])) {
				alt = Alteration.DOWN_TWO;
			}
		}
		if(slen >=2 && alt == Alteration.NONE) {
			if(s.substring(1, 2).equals(SHARP[1])) {
				alt = Alteration.UP_ONE;
			}
			else if(s.substring(1, 2).equals(FLAT[1])) {
				alt = Alteration.DOWN_ONE;
			}
		}
		return new Pitch(step, oct, alt);
	}
	
	@Override
	protected void setPitchElementType() {
		//this.pitchElementType = PitchElementType.PITCH;		
	}

	@Override
	public boolean isOctaveNeutral() {
		return octave < 0;
	}

	@Override
	/**
	 * The retrograde of a single Pitch is just the Pitch
	 * return new Pitch
	 */
	public PitchElement getRetrograde() {
		return new Pitch(this);
	}

	@Override
	/**
	 * The inversion of a single Pitch is just the Pitch
	 * return new Pitch
	 */
	public PitchElement getInversion() {
		return new Pitch(this);
	}

	@Override
	public PitchElement getTransposition(int numberOfSteps) {
		return new Pitch(this, numberOfSteps);
	}

	
	@Override 
	public PitchElement getInversion(Pitch startingPitch) { 
		Pitch invertedPitch = new Pitch(startingPitch); 
		int difference = getRangeStep() - startingPitch.getRangeStep(); 
		invertedPitch.increment(-difference); 
		return  invertedPitch; 
	}

	@Override
	public int size() {
		return 1;
	}
	 

}
