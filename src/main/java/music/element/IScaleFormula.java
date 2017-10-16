package music.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import util.IMapped;
import util.music.MissingElementException;

/**
 * Predictably, a formula for creating a Scale Or a Chord
 * The formula is a List of intervals: 1(semitone), 2 (whole tone), 3 etc.
 * AND/OR traditional interval pattern consisting of letters:
 * T = whole tone (an interval spanning two semitones), 
 * S = semitone
 * A major scale formula in intervals is: 2 2 1 2 2 2 1
 * Same thing in interval pattern: T T S T T T S
 * Interval patterns are useful only for diatonic scales without tritones (minor third).
 * So a harmonic minor scale (2 1 2 2 1 3 1) could not be represented in this way.
 * A natural minor (melodic minor desending) is (2 1 2 2 1 2 2) or T S T T S T T
 * a melodic minor ascending is (2 1 2 2 2 2 1)
 * NOTE that the sum of intervals for scale formulas is 12
 * 
 * As a chord formula, the sum(intervals) does not have to == 12.
 * 
 * Formulas are specified as numeric intervals, and for convenience an interval pattern
 * 
 * IMPORTANT - by convention, all the scale formulas span an octave, so the last note
 * is the same Pitch as the first but an octave higher.
 * For example the C-major scale is C,D,E,F,G,A,B,C or 8 notes instead of 7.
 * Scale includes a truncate() method to remove that pesky last note if not needed.
 * 
 * @author DBacon
 *
 */
public interface IScaleFormula extends Serializable, IMapped<String> {
	
	public int[] getFormula();
	public String getName();
	public List<String> getGroups();
	public List<String> getAlternateNames();
	public String getDescription();
	
	public List<Pitch> createPitches(Pitch root);
	
	
	/**
	 * 
	 * @param formula the chord formula as int[]
	 * @param root the root Pitch
	 * @param key the assigned Key - cannot be null
	 * @return  List<Pitch>
	 * @throws util.music.MissingElementException
	 */
	public static List<Pitch> createPitches(int[] formula, Pitch root, Key key) {
		if(key == null) {
			// this can happen if the "key" is missing in the first measure
			// looking for something like "key" : { "name" : "Bb-Major" , "mode" : "MAJOR"} }
			// throw a RuntimeException
			throw new MissingElementException("Missing element: Key", new Throwable("IScaleFormula.createPitches null key"));
		}
		int ap = key.getAlterationPreference();
		Alteration preference = Alteration.NONE;
		if(ap < 0) {
			preference = Alteration.DOWN_ONE;
		}
		else if(ap > 0) {
			preference = Alteration.UP_ONE;
		}
		return createPitches(formula, root, key, preference);
	}
	
	/**
	 * Creates a List of Pitch for a Scale with a given formula and root
	 * A Scale may consist of a single note. In that case the formula is [0].
	 * 
	 * @param formula an int[] scale formula
	 * @param root root Pitch of the scale
	 * @param key optional associated Key. If != null, it determines accidental preference - # or b
	 * @param pref optional Alteration to use (overrides key setting)
	 * @return List<Pitch>
	 */
	public static List<Pitch> createPitches(int[] formula, Pitch root, Key key, Alteration pref) {
		List<Pitch> plist = new ArrayList<Pitch>();
		plist.add(root);
		Pitch current = root;
		Pitch next = null;
		int preference = (pref != null) ? pref.value() : 
			(key != null && key.getSignature() != null && key.getSignature().length > 0) ? key.getSignature()[0].getAlteration() : 0;
		for(int i: formula) {
			next = new Pitch(current);
			if( i > 0) {
				next.increment(i);
				int alt = next.getAlteration();
				if(alt != 0 && alt != preference) {
					/*
					 * amounts to getting the enharmonic equivalent
					 * so D# same as Eb (preference -1)
					 * Db same as C# (preference 1)
					 */
					next.setEnharmonicEquivalent();
				}
			}
			plist.add(next);
			current = next;
		}
		return plist;
	}
	
	/**
	 * Creates a List of Pitch for a Scale with a given formula and root
	 * @param formula a List<Integer> scale formula
	 * @param root root Pitch of the scale
	 * @param key optional associated Key. If != null, it determines accidental preference - # or b
	 * @return
	 */
	public static List<Pitch> createPitches(List<Integer> formula, Pitch root, Key key) {
		int[] array = new int[formula.size()];
		int index = 0;
		for(Integer i : formula) {
			array[index++] = i;
		}
		return createPitches(array, root, key);
	}
	
	/**
	 * Creates a List of Pitch for a Scale with a given formula, root, and key
	 * @param formula a ScaleFormula instance
	 * @param root root Pitch of the scale
	 * @param key optional associated Key. If != null, it determines accidental preference - # or b
	 * @return
	 */
	public static List<Pitch> createPitches(ScaleFormula formula, Pitch root, Key key) {
		return createPitches(formula.getFormula(), root, key);
	}
	/**
	 * Formula for creating a major scale. 
	 */
	public static final int[] MAJOR_STEPS = {2,2,1,2,2,2,1};
	public static final ScaleFormula MAJOR_SCALE_FORMULA = new ScaleFormula("Major", "major", MAJOR_STEPS);
	/**
	 * Formula for creating a natural minor a.k.a. melodic minor descending scale
	 */
	public static final int[] MINOR_STEPS = {2,1,2,2,1,2,2};
	public static final ScaleFormula MINOR_SCALE_FORMULA = new ScaleFormula("Minor", "minor", MINOR_STEPS);
	
	public static final int[] MELODIC_MINOR_ASCENDING_STEPS = {2,1,2,2,2,2,1};
	public static final ScaleFormula MELODIC_MINOR_ASCENDING_SCALE_FORMULA =  
			new ScaleFormula("Melodic minor", "minor", MELODIC_MINOR_ASCENDING_STEPS);
	
	public static final int[] HARMONIC_MINOR_STEPS = {2,1,2,2,1,3,1};
	public static final ScaleFormula HARMONIC_MINOR_SCALE_FORMULA =  
			new ScaleFormula("Harmonic minor", "minor", HARMONIC_MINOR_STEPS);
	
	public static final int[] CHROMATIC_STEPS = {1,1,1,1,1,1,1,1,1,1,1,1};
	public static final ScaleFormula CHROMATIC_SCALE_FORMULA =  
			new ScaleFormula("Chromatic", "chromatic", CHROMATIC_STEPS);
	
	public static final int[] WHOLE_TONE_STEPS = {2,2,2,2,2,2};
	public static final ScaleFormula  WHOLE_TONE_SCALE_FORMULA =  new ScaleFormula("Whole tone", "chromatic", WHOLE_TONE_STEPS);
	
	public static final int[] PENTATONIC_MAJOR_STEPS = {2, 2, 3, 2, 3};
	public static final ScaleFormula  PENTATONIC_MAJOR_SCALE_FORMULA =  
			new ScaleFormula("Petatonic major", "petatonic", PENTATONIC_MAJOR_STEPS);
	
	public static final int[] PENTATONIC_MINOR_STEPS = {3, 2, 2, 3, 2};
	public static final ScaleFormula PENTATONIC_MINOR_SCALE_FORMULA =  
			new ScaleFormula("Petatonic minor", "petatonic",PENTATONIC_MINOR_STEPS);
	
	public static final int[] BLUES_STEPS = {3, 2, 1, 1, 3, 2};	//1 b3 4 #4 5 b7
	public static final ScaleFormula BLUES_SCALE_FORMULA =
			new ScaleFormula("Blues", "blues", BLUES_STEPS);
	
	public static final int[] BLUES_DIMINISHED_STEPS = {1, 2, 1, 2, 1, 2, 1, 2};	// pitch set: 0,1,3,4,6,7,9,10 (,12)
	public static final ScaleFormula BLUES_DIMINISHED_SCALE_FORMULA =
			new ScaleFormula("Blues Diminished", "blues", BLUES_DIMINISHED_STEPS);
	
	public static final int[] UNPITCHED_5_STEPS = {3, 4, 3, 3};	// E, G, B, D, F for 5-line percussion
	public static final ScaleFormula UNPITCHED_5_SCALE_FORMULA = 
			new ScaleFormula("5-Line Unpitched Percussion", "unpitched", UNPITCHED_5_STEPS);

	public static final int[] UNPITCHED_4_STEPS = {3, 4, 3};	// E, G, B, D for 4-line percussion
	public static final ScaleFormula UNPITCHED_4_SCALE_FORMULA = 
			new ScaleFormula("4-Line Unpitched Percussion", "unpitched", UNPITCHED_4_STEPS);

	public static final int[] UNPITCHED_3_STEPS = {3, 4};	// E, G, B for 3-line percussion
	public static final ScaleFormula UNPITCHED_3_SCALE_FORMULA = 
			new ScaleFormula("3-Line Unpitched Percussion", "unpitched", UNPITCHED_3_STEPS);

	public static final int[] UNPITCHED_2_STEPS = {3 };	// E, G  for 2-line percussion
	public static final ScaleFormula UNPITCHED_2_SCALE_FORMULA = 
			new ScaleFormula("2-Line Unpitched Percussion", "unpitched", UNPITCHED_2_STEPS);
	
	public static final int[] UNPITCHED_1_STEPS = {0 };	// E4  for single-line percussion
	public static final ScaleFormula UNPITCHED_1_SCALE_FORMULA = 
			new ScaleFormula("1-Line Unpitched Percussion", "unpitched", UNPITCHED_1_STEPS);
	
	/**
	 * { "name" : "Hirajoshi Japan" , "groups" : [ "hirajoshi"] , "formula" : [ 2 , 1 , 4 , 1 , 4] , "size" : 5}
	 */
	public static final int[] HIRAJOSHI_STEPS = {2, 1, 4, 1, 4};	// root D: D, E, F, A, Bb, D
	public static final ScaleFormula HIRAJOSHI_SCALE_FORMULA =
			new ScaleFormula("Hirajoshi Japan", "hirajoshi", HIRAJOSHI_STEPS);
	
	/**
	 * { "name" : "Phrygian Dominant, Dorico Flamenco, Avaha Raba, Spanish Folk, Jewish Major" , "alternateNames" : [ "(India) Alhijaz (Arabia) Ahavh Rabbah (Israel) Hitzaz (Greece) Spanish Gypsy"] , "groups" : [ "phrygian"] , 
	 * 	 "formula" : [ 1 , 3 , 1 , 2 , 1 , 2 , 2] , "size" : 7}
	 */
	public static final int[] JEWISH_AHAVOH_RABBOH_STEPS = { 1 , 3 , 1 , 2 , 1 , 2 , 2 };	// root C: C, Db, E, F, G, Ab, Bb
	public static final ScaleFormula JEWISH_AHAVOH_RABBOH_SCALE_FORMULA =
			new ScaleFormula("Jewish Ahavoh-Rabboh", "phrygian", JEWISH_AHAVOH_RABBOH_STEPS);
	
}
