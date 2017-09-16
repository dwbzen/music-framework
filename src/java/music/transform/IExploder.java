package music.transform;

import java.util.Arrays;
import java.util.List;

import math.IntegerPair;
import music.element.Measurable;
import music.element.Measure;

public interface IExploder {
	public static String[] EXPLODER_TYPES = {"ARPEGIO", "CHORD"};
	public static enum ExploderType {
		ARPEGIO(0), CHORD(1);
		ExploderType(int val) { this.value = val;}
		private final int value;
	    public int value() { return value; }
	    public String toString() { return EXPLODER_TYPES[value]; }
	}
	
	/**
	 * Some common tuplet Ratios
	 */
	public static final IntegerPair ONE_TO_ONE = 	IntegerPair.pair(1);		// default ratio 1:1
	public static final IntegerPair THREE_TWO =		IntegerPair.pair(3, 2);		// triplet 3:2
	public static final IntegerPair FIVE_FOUR = 	IntegerPair.pair(5, 4);		// five in the time of 4
	public static final IntegerPair SIX_FOUR = 		IntegerPair.pair(6, 4);		// six in the time of 4
	public static final IntegerPair SEVEN_FOUR = 	IntegerPair.pair(7, 4);		// seven in the time of 4
	public static final IntegerPair SEVEN_EIGHT = 	IntegerPair.pair(7, 8);		// seven in the time of 8
	public static final IntegerPair ELEVEN_EIGHT = 	IntegerPair.pair(11, 8);	// eleven in the time of 8
	
	/**
	 * Some common formulae each IntegerPair is the range of intervals - inclusive at both ends
	 * Note that despite the naming, any given formula could be applied as
	 * ARPEGIO (horizontally) or CHORD (vertically).
	 */
	public static final List<IntegerPair> EIGHT = Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(2), IntegerPair.pair(4), IntegerPair.pair(6),
			IntegerPair.pair(4), IntegerPair.pair(-2), IntegerPair.pair(-4), IntegerPair.pair(0) );
	public static final List<IntegerPair> EIGHT_RANDOM =  Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(1, 4), IntegerPair.pair(3, 6), IntegerPair.pair(1, 4),
			IntegerPair.pair(-3, 4), IntegerPair.pair(-4, 0), IntegerPair.pair(-5, 0), IntegerPair.pair(0) );
	public static final List<IntegerPair> EIGHT_RANDOM2 =  Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(2, 6), IntegerPair.pair(-2, 6), IntegerPair.pair(0),
			IntegerPair.pair(-2, 6), IntegerPair.pair(-6, 0), IntegerPair.pair(-6, 2), IntegerPair.pair(0) );

	public static final List<IntegerPair> TRIPLET_RANDOM = Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(2, 6), IntegerPair.pair(-6, 0) );
	
	public static final List<IntegerPair> TRIPLE_RANDOM_CHORD = Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(2, 8), IntegerPair.pair(-8, -2) );
	
	public static final List<IntegerPair> QUAD_RANDOM_CHORD = Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(2, 6), IntegerPair.pair(-6, -2), IntegerPair.pair(-12,-6) );
	
	public static final List<IntegerPair> OCTAVE_DOUBLE_CHORD = Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(-12) );

	public static final List<IntegerPair> QUINTUPLET_RANDOM =  Arrays.asList(
			IntegerPair.pair(0), IntegerPair.pair(2, 6), IntegerPair.pair(-2, 6),  IntegerPair.pair(-6, 0), IntegerPair.pair(-6, 2));
	
	List<? super Measurable> explode(Measurable m, Measure measure);
	
	int size();
}
