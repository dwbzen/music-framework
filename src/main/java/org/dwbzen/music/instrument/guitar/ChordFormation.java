package org.dwbzen.music.instrument.guitar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates a single guitar chord formation - fret positions and optional fingering
 * A private Map<Integer, Integer> holds a single chord formation, keyed by string number
 * where 1 = the top string and 6 = the bottom string. 
 * For example, {"p" : "3,5,3,4,3,3", "f" : "131211"}
 * the Map contains: { {1,3}, {2,3}, {3,4}, {4,3}, {5,5}, {6,3} }
 * omitted strings are not included as a key. The "p" positions list read left to right as bottom string to top string.
 * The fingering has the same structure the key being the string number, the value is the finger.
 * 
 * This G7 chord formation: {"p":"x,10,12,10,12,10","f":"12131;13141"} has omitted string and multiple fingerings.
 * The fretPosition map is { {1,10}, {2,12}, {3,10}, {4,12}, {5,10} }.
 * 
 * @author don_bacon
 *
 */
public class ChordFormation {
	@JsonProperty("positions")	private Map<Integer, Integer> fretPositions = new TreeMap<Integer, Integer>();
	@JsonProperty("fingerings")	private List<Map<Integer, Integer>> fingerings = new ArrayList<Map<Integer, Integer>>();

	public ChordFormation() {
	}
	
	public Map<Integer, Integer> getFretPositions() {
		return fretPositions;
	}

	public List<Map<Integer, Integer>> getFingerings() {
		return fingerings;
	}

	public void addFingering(Map<Integer, Integer> fingering) {
		fingerings.add(fingering);
	}
}
