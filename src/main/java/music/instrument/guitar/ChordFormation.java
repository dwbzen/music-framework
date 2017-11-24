package music.instrument.guitar;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates a single guitar chord formation - fret positions and optional fingering
 * A private Map<Integer, Integer> holds a single chord formation, keyed by string number
 * where 1 = the top string and 6 = the bottom string. So in the above example,
 * the Map contains: { {1,3}, {2,3}, {3,4},  {4,3}, {5,4}, {6 ,3} }
 * omitted strings are not included as a key.
 * @author don_bacon
 *
 */
public class ChordFormation {
	@JsonProperty("positions")	private Map<Integer, Integer> fretPositions = new TreeMap<Integer, Integer>();

	public ChordFormation() {
	}
	
	public Map<Integer, Integer> getFretPositions() {
		return fretPositions;
	}

}
