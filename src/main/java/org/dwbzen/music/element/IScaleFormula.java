package org.dwbzen.music.element;

import java.util.List;

import org.dwbzen.util.IMapped;

/**
 * Predictably, a formula for creating a Scale Or a Chord
 * The formula is a List of intervals: 1(semitone), 2 (whole tone), 3 etc.
 * AND/OR traditional interval pattern consisting of letters:<br>
 * T = whole tone (an interval spanning two semitones), 
 * S = semitone</p>
 * A major scale formula in intervals is: [ 2, 2, 1, 2, 2, 2, 1 ],"size":7<br>
 * As an interval pattern: T T S T T T S</p>
 * 
 * Interval patterns are useful only for diatonic scales without tritones (minor third).
 * So a harmonic minor scale (2 1 2 2 1 3 1) could not be represented in this way.<br>
 * A natural minor (melodic minor desending) is (2 1 2 2 1 2 2) or T S T T S T T<br>
 * a melodic minor ascending is (2 1 2 2 2 2 1)<br>
 * NOTE that the sum of intervals for scale formulas is 12
 * <br>
 * 
 * Formulas are specified as numeric intervals, and for convenience an interval pattern
 * </p>
 * IMPORTANT - by convention, all the scale formulas span an octave, so the last note
 * is the same Pitch as the first but an octave higher.<br>
 * The scale size is both the length of the steps array and the number of unique notes, viz. exclusive of the final note.<br>
 * For example the C-major scale (2, 2, 1, 2, 2, 2, 1) is<br>
 * C (2 steps) D (2 steps) E (1 step) F (2 steps) G (2 steps) A (2 steps) B (1 step) C
 * Scale includes a truncate() method to remove that last note if not needed.
 * 
 * @author DBacon
 *
 */
public interface IScaleFormula extends IFormula, IMapped<String>, Comparable<ScaleFormula> {
	
	public List<String> getGroups();
	public List<Pitch> createPitches(Pitch root);
		
	
	/**
	 * Creates a List of Pitch for a Scale with a given formula and root
	 * @param formula a List<Integer> scale formula
	 * @param root root Pitch of the scale
	 * @param key optional associated Key. If != null, it determines accidental preference - # or b
	 * @return
	 */
	public static List<Pitch> createPitches(List<Integer> formula, Pitch root, Key key) {
		return IFormula.createPitches(formula, root, key);
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
	 * Here are some common scale formulas taken from "commonScaleFormulas.json"
	 *  
	 */
	public static final int[] MAJOR_STEPS = {2,2,1,2,2,2,1};
	static String[] majorGroups = {"major", "diatonic"};
	public static final ScaleFormula MAJOR_SCALE_FORMULA = new ScaleFormula("Major", majorGroups, MAJOR_STEPS);
	/**
	 * Formula for creating a natural minor a.k.a. melodic minor descending scale
	 */
	public static final int[] MINOR_STEPS = {2,1,2,2,1,2,2};	// Melodic minor descending == Natural minor == Aeolian mode
	public static final ScaleFormula MINOR_SCALE_FORMULA = new ScaleFormula("Minor", "minor", MINOR_STEPS);
	
	public static final int[] MELODIC_MINOR_ASCENDING_STEPS = {2,1,2,2,2,2,1};		// Natural minor with #6 and #7
	public static final ScaleFormula MELODIC_MINOR_ASCENDING_SCALE_FORMULA =  
			new ScaleFormula("Melodic minor", "minor", MELODIC_MINOR_ASCENDING_STEPS);
	
	public static final int[] HARMONIC_MINOR_STEPS = {2,1,2,2,1,3,1};	// Natural minor with #7
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
	
	public static final int[] BLUES_STEPS = {3, 2, 1, 1, 3, 2};	// 1 b3 4 #4 5 b7
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
	
	/**
	 * {"Hungarian Gypsy":{"alternateNames":[ "Mela Sanmukhapriya (India)" ],"groups":[ "hungarian" ],"formula":[ 2, 1, 3, 1, 1, 2, 2 ],"size":7} },
	 */
	public static final int[] HUNGARIAN_GYPSY_STEPS = {2, 1, 3, 1, 1, 2, 2};		// "C", "D", "Eb", "Gb", "G", "Ab", "Bb", "C" 
	public static final ScaleFormula HUNGARIAN_GYPSY_SCALE_FORMULA =
			new ScaleFormula("Hungarian Gypsy", "hungarian", HUNGARIAN_GYPSY_STEPS);
	
}
