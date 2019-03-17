package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.math.MathUtil;
import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.music.transform.ITransformer.Preference;


/**
 * A RhythmScale is a list of time units, or simply units
 * A unit represents a time interval (duration) that is a division of
 * a whole note. This is independent of how long a unit actually lasts
 * (in seconds) since that depends on
 * the tempo marking and the root, to be time-independent.
 * 
 * A "root rhythm" is the number of units in a Whole Note.
 * For example, consider a root duration of 16 (logically a whole note)
 * with units {1, 2, 3, 4, 6, 8, 10, 12, 14, 16}, where 1 unit represents in this case a 16th note.
 * 
 * Broadly speaking, there are 2 categories of rhythmic Expression:
 *  	texture type - MONOPHONIC or CHORDAL
 * 		rhythmic unit type - METRIC (1 note per unit) EXTRAMETRIC (tuplets)
 * 
 * Texture is instrument-dependent; rhythmic unit is independent of orchestration.
 * What they have in common is how data points are consumed. CHORDAL texture expression consumes
 * multiple notes and arranges them vertically,  METRIC/EXTRAMETRIC consumes 1..n notes and arranges them horizontally.
 * 
 * EXTRAMETRIC Expression associates each unit with an
 * allowable tuplet representation of that duration, encapsulated in the util.Ratio class.
 * 16:  "units" : 8 , "ratio" : { "beats" : 3 , "timeOf" : 2}  says that 16 units can be represented as a triplet of 8-unit notes.
 *
 * Given a root of 16 for example, 4 units can be represented as a single quarter note,
 * a 3:2 eighth note tuplet, or a 5:4 sixteenth note tuplet.
 * 
 * CHORDAL texture Expression associates each allowable unit with expression(s) giving the number of notes
 * permitted in the chord. For example. {4, {2, 3, 4, 5} }
 * says that a chord consisting of 2 to 5 notes are valid for 4 units of duration.
 * CHORDAL works with tuplets as well. For example, 
 * {4,  {{2, [3,2]}, {2, 3, 4, 5}}, ... } says that 4 units can be represented
 * as a 3:2 triplet having 2,3,4, or 5 notes.
 * 
 * chordal property defaults to false but can be overridden for individual
 * instruments as configured (chordalProbability > 0)
 * 
 * Principle use of Rhythm scales is converting raw durations values in a data set
 * to a discrete value during music generation. A similar concept
 * is used when converting raw Pitch to a particular Scale pitch.
 * Selection rules are not encoded in the RhythmScale itself, but as a Selector class.
 * 
 * @author bacond6
 * @see music.element.TextureType
 * @see music.element.RhythmicUnitType
 * 
 */
public class RhythmScale  implements IRhythmScale {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(RhythmScale.class);
	private static final long serialVersionUID = -1208212903638963243L;
	
	/**
	 * Need NoteType in order to create music XML
	 * In ascending duration order
	 */
	public static String[] NoteTypes = {
			"64th", "32nd", "16th", "eighth", "quarter", "half", "whole"
		};
	
	@JsonProperty("name")	private String name = null;
	/**
	 *  #divisions in a whole note, must be a power of 2
	 *  TODO - refactor to support compound rhyhthms like 12/8 or 6/4.
	 */
	@JsonProperty("root")			protected int root;
	@JsonProperty("units")			private SortedSet<Integer> baseUnits = new TreeSet<Integer>();	// permitted units for expression
	@JsonProperty("expressions")	private Map<Integer, IRhythmTextureMap> expressions = new TreeMap<Integer, IRhythmTextureMap>();
	@JsonProperty("chordal")		private boolean chordal = false;
	@JsonIgnore	private Map<Integer, List<RhythmExpression>> expression = new TreeMap<Integer, List<RhythmExpression>>();
	@JsonIgnore	private int range;
	@JsonIgnore protected ExpressionSelector expressionSelector = null;

	
	/**
	 * Maps how the units are realized as a List<Duration> for example,
	 * 13 units (assuming a root of 16) can be realized as 2 tied durations of 12 + 1 units.
	 * 15 units can be realized as 14 + 1 (double-dotted half + 16th) or 12 + 3 (dotted half + dotted eighth)
	 * 
	 */
	@JsonIgnore protected Map<Integer, List<Duration>> factorMap = new HashMap<Integer, List<Duration>>();

	public RhythmScale() {
	}
	
	public RhythmScale(int divisionsInaWholeNote) {
		root = divisionsInaWholeNote;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getRoot() {
		return root;
	}

	@Override
	public void setRoot(int root) {
		this.root = root;
	}

	@Override
	public SortedSet<Integer> getBaseUnits() {
		return baseUnits;
	}

	@Override
	public Map<Integer, IRhythmTextureMap> getExpressions() {
		return expressions;
	}
	
	@Override
	public IRhythmTextureMap getRhythmTextureMap(Integer units) {
		return expressions.get(units);
	}
	
	@Override
	public int getRange() {
		return range;
	}

	@Override
	public void setRange(int range) {
		this.range = range;
	}

	@Override
	public ExpressionSelector getExpressionSelector() {
		return expressionSelector;
	}

	@Override
	public void setExpressionSelector(ExpressionSelector expressionSelector) {
		this.expressionSelector = expressionSelector;
	}
	
	/**
	 * Round raw units to this RhythmScale. Use Preference to break ties.
	 * @param double rawUnits
	 * @param Preference pref
	 * @return int units - closest unit value
	 */
	@Override
	public int findClosestUnits(double rawUnits, Preference pref) {
		int nunits = -1;
		double diffLow = -999;
		double diffHigh = 999;
		double diff = 0;
		int low = 0;
		int high = 0;
		for(Integer bunits : baseUnits) {	// for example {1, 2, 3, 4, 6, 8, 10, 12, 14, 16}
			diff = bunits.doubleValue() - rawUnits;
			if(diff == 0) {
				nunits = bunits;
				break;
			}
			else if(diff > 0) {
				if(diff < diffHigh) {
					diffHigh = diff;
					high = bunits;
				}
			}
			else if(diff < 0) {
				if(diff > diffLow) {
					diffLow = diff;
					low = bunits;
				}
			}
		}
		if(diffLow + diffHigh == 0) {	// equidistant
			nunits = (pref == Preference.Down) ? low : high;
		}
		else {
			nunits = (low < high && low > 0) ? low : high;
		}
		log.trace("closest units to " + rawUnits + " = " + nunits);
		return nunits;
	}

	public Map<Integer, List<Duration>> getFactorMap() {
		return factorMap;
	}
	
	public List<Duration> addFactor(int units, Duration duration) {
		List<Duration> dlist = null;
		if(factorMap.containsKey(units)) {
			dlist = factorMap.get(units);
			dlist.add(duration);
		}
		else {
			dlist = new ArrayList<Duration>();
			dlist.add(duration);
			factorMap.put(units, dlist);
		}
		return dlist;
	}

	@Override
	public List<Duration> getFactors(Integer units) {
		return factorMap.get(units);
	}
	
	@Override
	public String getNoteType(Note note) {
		return determineNoteType(note, root);
	}
	
	/**
	 * Derives the note type ("half", "quarter" etc.) from the root
	 * for the Note provided
	 * Example, root = 16, units = 6 -> "quarter" (4 + 1 dot). log2 = 4 + 2 = 6
	 *          root = 16, units = 1 -> "16th". log2 = 4 + 0 = 4
	 * 		    root = 32, units = 6 -> "eighth" (4 + 1 dot).  log2 = 5 + 2 = 7
	 * ASSUMES that 2 <= root <= 64 and the Note's baseUnits <= root
	 * @param Note
	 * @param rootUnits
	 * @return String note type
	 */
	public static String determineNoteType(Note note, int rootUnits) {
		int baseUnits = note.getDuration().getBaseUnits();
		int log2root = MathUtil.log2(rootUnits);
		int log2Units = MathUtil.log2(baseUnits);	// 1 -> 0, 2 -> 1, 4 -> 2, 8 -> 3, 16 -> 4, 32 -> 5, 64 -> 6
		int ind = 6 - log2root + log2Units;
		
		// 2: 5,6		 	 4: 4,5,6			 8: 3,4,5,6 
		// 16: 2,3,4,5,6	32: 1,2,3,4,5,6		64: 0,1,2,3,4,5,6
		
		return NoteTypes[ind];
	}

	@Override
	public boolean isChordal() {
		return chordal;
	}

	@Override
	public void setChordal(boolean chordal) {
		this.chordal = chordal;
	}
	
}
