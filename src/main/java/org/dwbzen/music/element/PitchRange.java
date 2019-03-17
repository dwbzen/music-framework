package org.dwbzen.music.element;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a range of Pitches from low to high.
 * 
 * @author don_bacon
 *
 */
public class PitchRange {

	@JsonProperty	private Pitch low;
	@JsonProperty	private Pitch high;
	@JsonProperty	private int stepRange = 0;	// #chromatic steps in the range (for a piano it's 88)
	
	public PitchRange(Pitch from, Pitch to) {
		this.low = from;
		this.high = to;
		this.stepRange = stepsBetween(low, high);
	}
	
	
	/**
	 * Calculates the number of steps (semitones) between Pitch p1 and another Pitch p2
	 * in that direction (so could be negative if this < p)
	 * Example: Bb - F# (same octave) = 10 - 2 = 8
	 */
	public static int stepsBetween(Pitch p1, Pitch p2) {
		int steps = 0;
		if(p1.compareTo(p2) != 0) {
			int octdiff = p2.getOctave() - p1.getOctave();
			int stepdiff = p2.getChromaticScaleDegree() - p1.getChromaticScaleDegree();
			steps = (12 * octdiff) + stepdiff + 1;
		}
		return steps;
	}

	public Pitch getLow() {
		return low;
	}

	public void setLow(Pitch low) {
		this.low = low;
	}

	public Pitch getHigh() {
		return high;
	}

	public void setHigh(Pitch high) {
		this.high = high;
	}

	public int getStepRange() {
		return stepRange;
	}

	public static void main(String[] args) {
		Pitch p1 = new Pitch("D4");
		Pitch p2 = new Pitch("D6");
		PitchRange pr = new PitchRange(p1, p2);
		System.out.println("step range: " + pr.getStepRange());
		
	}
}
