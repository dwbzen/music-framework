package org.dwbzen.music.element;

public interface IAdjustable {
	
	/**
	 * Adjusts this Pitch or all the Pitches in the PitchSet by the number of steps indicated
	 * @param numberOfSteps
	 */
	public void adjustPitch(int numberOfSteps);
	
	/**
	 * Lowers this by n steps. This applies to all the Pitches in a PitchSet
	 * 
	 * @param n  number of steps to decrement, if < 0 increments by that amount.
	 */
	public void decrement(int n);
	
	/**
	 * Adds n steps to this. If this is octave neutral, the resulting Pitch will also have octave == -1.
	 * @param n number of steps to increment. If < 0, decrements by that amount.
	 */
	public void increment(int n) ;
}
