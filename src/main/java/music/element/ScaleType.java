package music.element;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Scales may be described according to the intervals they contain: 
 * for example: diatonic (which implies hexatonic), chromatic, whole tone
 * and/or by the number of different pitch classes they contain:
 * Octatonic (8 notes per octave), 
 * Heptatonic (7 notes per octave) - the most common in Western music,
 * Hexatonic (6 notes per octave)
 * Pentatonic (5 notes per octave), Tetratonic (4 notes), tritonic (3 notes), and ditonic (2 notes)
 * Monotonic (1 note - boring)
 * 
 * Discrete (for unpitched percussion where Pitch is a line position and not a true Pitch)
 * There are 4 Discrete scale types corresponding to the #lines needed to score the instrument: 1, 2, 3, 4, or 5
 * 
 * ScaleType is immutable.
 * @author DBacon
 *
 */
public final class ScaleType {
	
	@JsonProperty("name")	private final String name;		// hexatonic, pentatonic, chromatic etc.
	@JsonProperty("length")	private final int length;		// #notes in the scale (normally 7)
	
	public ScaleType(String name) {
		this.name = name;
		length = scaleLengths.get(name).value;
	}
	
	/**
	 * Valid scale type names. Not case sensitive.
	 */
	public static String[] TYPE_NAMES = {
		"chromatic",
		"nonatonic",
		"whole tone",
		"octatonic",
		"heptatonic",
		"hexatonic",
		"pentatonic",
		"tetratonic",
		"tritonic",
		"ditonic",
		"monotonic",
		"diatonic",
		"discrete1",
		"discrete2",
		"discrete3",
		"discrete4",
		"discrete5"
	};
	
	public static enum ScaleTypes {
		CHROMATIC(12), NONATONIC(9), WHOLE_TONE(6), OCTATONIC(8), HEPTATONIC(7), HEXATONIC(6), PENTATONIC(5), 
		TETRATONIC(4), TRITONIC(3), DITONIC(2), MONOTONIC(1), DIATONIC(7), 
		DISCRETE_1LINE(1), DISCRETE_2LINE(2), DISCRETE_3LINE(3), DISCRETE_4LINE(4), DISCRETE_5LINE(5);
		ScaleTypes(int val) { this.value = val;}
		private final int value;
	    public int value() { return value; }
	}
	
	
	private static Map<String, ScaleTypes> scaleLengths = new HashMap<String, ScaleTypes>();
	static {
		scaleLengths.put(TYPE_NAMES[ScaleTypes.CHROMATIC.ordinal()], ScaleTypes.CHROMATIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.NONATONIC.ordinal()], ScaleTypes.NONATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.WHOLE_TONE.ordinal()], ScaleTypes.WHOLE_TONE);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.OCTATONIC.ordinal()], ScaleTypes.OCTATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.HEPTATONIC.ordinal()], ScaleTypes.HEPTATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.HEXATONIC.ordinal()], ScaleTypes.HEXATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DIATONIC.ordinal()], ScaleTypes.DIATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.PENTATONIC.ordinal()], ScaleTypes.PENTATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.TETRATONIC.ordinal()], ScaleTypes.TETRATONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.TRITONIC.ordinal()], ScaleTypes.TRITONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DITONIC.ordinal()], ScaleTypes.DITONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.MONOTONIC.ordinal()], ScaleTypes.MONOTONIC);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DISCRETE_1LINE.ordinal()], ScaleTypes.DISCRETE_1LINE);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DISCRETE_2LINE.ordinal()], ScaleTypes.DISCRETE_2LINE);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DISCRETE_3LINE.ordinal()], ScaleTypes.DISCRETE_3LINE);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DISCRETE_4LINE.ordinal()], ScaleTypes.DISCRETE_4LINE);
		scaleLengths.put(TYPE_NAMES[ScaleTypes.DISCRETE_5LINE.ordinal()], ScaleTypes.DISCRETE_5LINE);
	}
	
	public final static ScaleType DIATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.DIATONIC.ordinal()]);
	public final static ScaleType HEPTATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.HEPTATONIC.ordinal()]);
	public final static ScaleType CHROMATIC = new ScaleType(TYPE_NAMES[ScaleTypes.CHROMATIC.ordinal()]);
	public final static ScaleType PENTATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.PENTATONIC.ordinal()]);
	public final static ScaleType WHOLE_TONE = new ScaleType(TYPE_NAMES[ScaleTypes.WHOLE_TONE.ordinal()]);
	public final static ScaleType OCTATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.OCTATONIC.ordinal()]);
	public final static ScaleType HEXATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.HEXATONIC.ordinal()]);
	public final static ScaleType TETRATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.TETRATONIC.ordinal()]);
	public final static ScaleType TRITONIC = new ScaleType(TYPE_NAMES[ScaleTypes.TRITONIC.ordinal()]);
	public final static ScaleType DITONIC = new ScaleType(TYPE_NAMES[ScaleTypes.DITONIC.ordinal()]);
	public final static ScaleType MONOTONIC = new ScaleType(TYPE_NAMES[ScaleTypes.MONOTONIC.ordinal()]);
	public final static ScaleType NONATONIC = new ScaleType(TYPE_NAMES[ScaleTypes.NONATONIC.ordinal()]);
	public final static ScaleType DISCRETE_1LINE = new ScaleType(TYPE_NAMES[ScaleTypes.DISCRETE_1LINE.ordinal()]);
	public final static ScaleType DISCRETE_2LINE = new ScaleType(TYPE_NAMES[ScaleTypes.DISCRETE_2LINE.ordinal()]);
	public final static ScaleType DISCRETE_3LINE = new ScaleType(TYPE_NAMES[ScaleTypes.DISCRETE_3LINE.ordinal()]);
	public final static ScaleType DISCRETE_4LINE = new ScaleType(TYPE_NAMES[ScaleTypes.DISCRETE_4LINE.ordinal()]);
	public final static ScaleType DISCRETE_5LINE = new ScaleType(TYPE_NAMES[ScaleTypes.DISCRETE_5LINE.ordinal()]);

	
	public int getLength() {
		return length;
	}
	public String getName() {
		return name;
	}

}
