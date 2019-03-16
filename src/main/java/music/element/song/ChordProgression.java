package music.element.song;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dwbzen.common.cp.ICollectable;

/**
 * A ChordProgression is just a List of HarmonyChord.
 * 
 * @author Don_Bacon
 *
 */
public class ChordProgression extends ArrayList<HarmonyChord> implements Comparable<ChordProgression>, List<HarmonyChord>, ICollectable<HarmonyChord> {

	private static final long serialVersionUID = -2572748011314576371L;
	private static String DELIM = " ";
	
	/** Used to represent a Terminal state in a Markov Chain */
	public static final HarmonyChord TERMINAL = HarmonyChord.TERMINAL_HARMONY_CHORD;
	/** Used to represent a NULL key in a Map - since it can't really be a null */
	public static final HarmonyChord NULL_VALUE = HarmonyChord.NULL_VALUE_HARMONY_CHORD;

	public ChordProgression() {
		super();
	}
	public ChordProgression(List<SongMeasure> measures) {
		super();
		addAll(measures);
	}
	public ChordProgression(List<SongMeasure> measures, boolean transposed) {
		super();
		addAll(measures, transposed);
	}
	
	/**
	 * Copy constructor. This does a deep copy of the member HarmonyChords.
	 * @param ChordProgression chordProgressionToAdd
	 */
	public ChordProgression(ChordProgression chordProgressionToAdd) {
		super();
		for(HarmonyChord hc : chordProgressionToAdd) {
			add(new HarmonyChord(hc));
		}
	}
	
	/**
	 * Creates a new ChordProgression from an existing one and adding a HarmonyChord on the end
	 * So the result will be 1 longer than the original.
	 * The chordProgressionToAdd is deep copied as is the HarmonyChord.
	 * @param ChordProgression chordProgressionToAdd
	 * @param HarmonyChord other
	 */
	public ChordProgression(ChordProgression chordProgressionToAdd, HarmonyChord other) {
		super();
		for(HarmonyChord hc : chordProgressionToAdd) {
			add(new HarmonyChord(hc));
		}
		if(other != null) {
			add(other);
		}
	}
	
	public int length() {
		return this.size();
	}
	
	/**
	 * Adds the entire List<Harmony> from the List<SongMeasure> provided
	 * @param measures List<SongMeasure>
	 * @return true if added successfully
	 * @see java.util.ArrayList.addAll(Collection<? extends E> c)
	 */
	public boolean addAll(List<SongMeasure> measures) {
		boolean ok = true;
		for(SongMeasure measure : measures) {
			List<Harmony> hl = measure.getHarmony();
			for(Harmony harmony : hl) {
				if(harmony.getHarmonyChord() == null) {		// if no chord is sounded this will be null
					continue;
				}
				ok &= this.add(harmony.getHarmonyChord());
			}
		}
		return ok;
	}

	/**
	 * Adds the entire List<Harmony> from the List<SongMeasure> provided
	 * @param measures List<SongMeasure>
	 * @param original if true return harmony chords in original key, otherwise in transposed key
	 * @return true if added successfully
	 * @see java.util.ArrayList.addAll(Collection<? extends E> c)
	 */
	public boolean addAll(List<SongMeasure> measures, boolean original) {
		boolean ok = true;
		if(original) {
			ok &= addAll(measures);
		}
		else {
			for(SongMeasure measure : measures) {
				List<Harmony> hl = measure.getHarmony();
				for(Harmony harmony : hl) {
					if(harmony.getHarmonyChord() == null) {		// if no chord is sounded this will be null
						continue;
					}
					ok &= this.add(harmony.getTransposedHarmonyChord());
				}
			}
		}
		return ok;
	}
	
	/**
	 * Adds another ChordProgression to this one.
	 * @param cp ChordProgression to add
	 * @return true if added
	 */
	public boolean add(ChordProgression cp) {
		return this.addAll(cp);
	}
	
	/**
	 * Returns a new ChordProgression of the portion of this ChordProgression
	 * between the specified fromIndex, inclusive, and toIndex, exclusive.
	 * @param startIndex beginning index to get, inclusive
	 * @param endIndex  end index to get, exclusive
	 * @return ChordProgression
	 */
	public ChordProgression subset(int fromIndex, int toIndex) {
		ChordProgression hclist = new ChordProgression();
		for(int i=fromIndex; i<toIndex; i++) {
			hclist.add(  get(i) );
		}
		return hclist;
	}
	
	/**
	 * Returns a new Sentence of the portion of this Sentence
	 * from the specified fromIndex, inclusive to the end of the String
	 * @param startIndex beginning index to get, inclusive
	 * @return ChordProgression
	 */
	public ChordProgression subset(int startIndex) {
		return subset(startIndex, this.size());
	}

	/**
	 * The key is the names of the chords concatenated with a configured delimiter, typically a space.
	 * @return String key for this ChordProgression
	 */
	public String getKey() {
		return toString();
	}
	
	/**
	 * Chord names concatenated with a default delimiter of space.
	 */
	public String toString() {
		return toString(DELIM);
	}
	
	public String toString(String delim) {
		StringBuffer sb = new StringBuffer();
		int len = this.size() - 1;	// number of delimiters needed
		int i=0;
		Iterator<HarmonyChord> hcit = iterator();
		while(hcit.hasNext()) {
			HarmonyChord hc = hcit.next();
			sb.append(hc.getName());
			if((++i) <= len) {
				sb.append(delim);
			}
		}
		return sb.toString();
	}
	@Override
	public int compareTo(ChordProgression cp) {
		return toString().compareTo(cp.toString());
	}
	
	public static String getDelimiter() {
		return DELIM;
	}
	
	public static void setDelimiter(String delim) {
		DELIM = delim;
	}
	@Override
	public HarmonyChord getTerminal() {
		return TERMINAL;
	}
	@Override
	public HarmonyChord getNullValue() {
		return NULL_VALUE;
	}

}

