package org.dwbzen.music.element;
import org.dwbzen.music.element.Pitch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates intervals and interval notation used in chord formation
 * starting with root (R) through #13th.
 * A ChordInterval is created by specifying the number of half-steps (chromatic step)
 * and a root Pitch. The Pitch can include an octave or be -1 for octave-neutral.
 * steps can be 0 through 22 (which is a #13th) a 2-octave span. 
 * Anything greater is normalized down to nearest octave. 
 * For example 23 steps becomes 11 (i.e. 23 mod 12 or 23%12).
 * Specifying a step value < 0 will cause an IllegalArgumentException.
 * 
 * A ChordInterval has a String notation and text description.
 * The notation is how the interval is commonly specified in chords,
 * for example "5" for perfect 5th, "m3" for minor third (3 steps), "M3" for major third (4 steps).
 * Some intervals have more than one name.  Secondary name(s) may be used when specifying
 * an interval, but the primary name is always used in toString() and toJSON();
 * 
 * Text description has a similar pattern: a primary description ("7th" for example)
 * and 0:n secondary descriptions ("dominant 7th", "minor 7th")
 * 
 * @author don_bacon
 *
 */
public class ChordInterval  implements Serializable, Comparable<ChordInterval> {

	private static final long serialVersionUID = 493622549097169989L;
	/**
	 * Maps the notation name to #steps in the interval
	 */
	public static final Map<String, Integer> notation = new HashMap<String, Integer>();
	
	/**
	 * Maps the interval #steps to the most common notation name. Reverse of the above.
	 */
	public static final Map<Integer, String> reverseNotation = new HashMap<Integer, String>();
	
	/**
	 * Descriptive text
	 */
	public static final Map<String, String> text = new HashMap<String, String>();
	
	static {
		notation.put("R", 0);		reverseNotation.put(0, "R");	text.put("R", "root");
		notation.put("m2", 1);		reverseNotation.put(1, "m2");	text.put("m2", "minor 2nd");
		notation.put("2", 2);		reverseNotation.put(2, "2");	text.put("2", "major 2nd");
		notation.put("M2", 2);										text.put("M2", "major 2nd");
		notation.put("m3", 3);		reverseNotation.put(3, "m3");	text.put("m3", "minor 3rd");
		notation.put("3", 4);										text.put("3", "major 3rd");
		notation.put("M3", 4);		reverseNotation.put(4, "M3");	text.put("M3", "major 3rd");
		notation.put("4", 5);		reverseNotation.put(5,"4");		text.put("4", "4th");
		notation.put("M4", 5);										text.put("M4", "4th");
		notation.put("b5", 6);		reverseNotation.put(6, "b5");	text.put("5b", "flat 5th");
		notation.put("5", 7);		reverseNotation.put(7, "5");	text.put("5", "5th");
		notation.put("P5", 7);										text.put("5", "perfect 5th");
		notation.put("M5", 7);										text.put("5", "major 5th");
		notation.put("#5", 8);		reverseNotation.put(8, "#5");	text.put("#5", "sharp 5th");
		notation.put("6", 9);		reverseNotation.put(9, "6");	text.put("6", "6th");
		notation.put("M6", 9);										text.put("M6", "major 6th");
		notation.put("dim7", 9);									text.put("dim7", "diminished 7th");
		notation.put("7", 10);		reverseNotation.put(10, "7");	text.put("7", "7th");
		notation.put("dom7", 10);									text.put("dom7", "dominant 7th");
		notation.put("m7",10);										text.put("m7", "minor 7th");
		notation.put("M7", 11);		reverseNotation.put(11, "M7");	text.put("M7", "major 7th");
		notation.put("O", 12);		reverseNotation.put(12, "O");	text.put("O", "octave");
		notation.put("8", 12);										text.put("8", "octave");
		notation.put("b9", 13);		reverseNotation.put(13, "b9");	text.put("b9", "flat 9th");
		notation.put("9", 14);		reverseNotation.put(14, "9");	text.put("9", "9th");
		notation.put("#9", 15);		reverseNotation.put(15, "#9");	text.put("#9", "sharp 9th");
		notation.put("O+3", 16);	reverseNotation.put(16, "O+3");	text.put("O+3", "octave+3rd");
		notation.put("11", 17);		reverseNotation.put(17, "11");	text.put("11", "11th");
		notation.put("#11", 18);	reverseNotation.put(18, "#11");	text.put("#11", "sharp 11th");
		notation.put("O+5", 19);	reverseNotation.put(19, "O+5");	text.put("O+3", "octave+5th");
		notation.put("b13", 20);	reverseNotation.put(20, "b13");	text.put("b13", "flat 13th");
		notation.put("13", 21);		reverseNotation.put(21, "13");	text.put("13", "13th");
		notation.put("#13", 22);	reverseNotation.put(22, "#13");	text.put("#13", "sharp 13th");
	}
	
	@JsonProperty("root")			private Pitch root = null;
	@JsonProperty("steps")			private int steps = 0;		// 0 through 22
	@JsonProperty("abbreviation")	private String abbreviation = null;	// notation
	@JsonProperty("description")	private String description = null;	// text
	
	/**
	 * Construct a new ChordInterval.
	 * For safety, the rootPitch is copy-constructed.
	 * 
	 * @param rootPitch the root Pitch
	 * @param steps number of chromatic steps in the interval (0-22)
	 */
	public ChordInterval(Pitch rootPitch, int steps) {
		if(steps < 0) {
			throw new IllegalArgumentException("Steps cannot be negative");
		}
		this.steps = (steps > 22) ? steps % 12 : steps;
		this.root = new Pitch(rootPitch);
		this.abbreviation = reverseNotation.get(steps);
		this.description = text.get(abbreviation);
	}
	
	@Override
	public int compareTo(ChordInterval other) {
		int result;
		if(steps == other.steps) {
			result = root.compareTo(other.getRoot());
		}
		else {
			result = (steps < other.steps) ? -1 : 1; 
		}
		return result;
	}

	public Pitch getRoot() {
		return root;
	}

	public void setRoot(Pitch root) {
		this.root = root;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
