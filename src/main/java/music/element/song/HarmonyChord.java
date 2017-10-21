package music.element.song;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import music.element.Key;
import music.element.Pitch;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

import util.IJson;
import util.INameable;
import mathlib.cp.ICollectable;

/**
 * A HarmonyChord is a chord that has an associated ChordFormula rather than
 * an arbitrary array of notes.
 * It is a realization of a particular ChordFormula with a root Pitch.
 * Because components are Pitch there is no implied duration and
 * instances can't be used in a Measure or Score (it's not a Measurable).
 * The name is set to the root + symbol (the first one) without the octave#
 * if there is one. For example, "C9+11". Alternate names are created automatically
 * from other symbols if there is more than one.
 * A "silent chord" is used when no chord is sounded.
 * In JSON: "harmony" : [ { "chord" : "0", "beat" : 1 } ]
 * 
 * @author don_bacon
 *
 */
@Entity(value="HarmonyChord", noClassnameStored=true)
public class HarmonyChord implements IJson, INameable, Comparable<HarmonyChord>, ICollectable<Pitch> {

	private static final long serialVersionUID = -5601371412350435601L;
	private static Morphia morphia = new Morphia();
	private static boolean includeSpellingInToString = true;
	static final org.apache.log4j.Logger log = Logger.getLogger(HarmonyChord.class);
	public static final HarmonyChord SILENT = new HarmonyChord();
	
	/** Used to represent a Terminal state in a Markov Chain */
	public static final Pitch TERMINAL = Pitch.SILENT;
	public static final HarmonyChord TERMINAL_HARMONY_CHORD = new HarmonyChord("¶");
	
	/** Used to represent a NULL key in a Map - since it can't really be a null */
	public static final Pitch NULL_VALUE = Pitch.NULL_VALUE;
	public static final HarmonyChord NULL_VALUE_HARMONY_CHORD = new HarmonyChord("§");
	
	@Id ObjectId id;
	@Reference	private ChordFormula chordFormula = null;	// info about the chord - cannot be null
	@Embedded	private Pitch root = null;
	@Transient  private Pitch bassNote = null;		// a slash chord has something other than the root in the bass
	@Embedded	private List<Pitch> chordPitches = new ArrayList<Pitch>();
	@Property	private String name = null;				// root + symbol, "C9+11" for example
	@Transient	private List<String> alternateNames = new ArrayList<String>();
	@Property	private List<String> spelling = null;	// for readability, just the pitches as in C Eb G Db
	
	/**
	 * Create a silent HarmonyChord (no chord is sounded).
	 */
	public HarmonyChord() {
		this("0");
	}
	
	public HarmonyChord(String name) {
		this.name = name;
		setChordFormula(new ChordFormula());	// a silent chord
		spelling = new ArrayList<String>();		// no pitches
		root = Pitch.SILENT;
	}
	
	/**
	 * 
	 * @param chordFormula
	 * @param rootPitch
	 * @param key
	 */
	public HarmonyChord(ChordFormula chordFormula, Pitch rootPitch, Key key) {
		this.root = rootPitch;
		this.bassNote = rootPitch;
		chordPitches.addAll(chordFormula.createPitches(root, key));
		int fnum = ChordManager.computeFormulaNumber(chordFormula);
		chordFormula.setFormulaNumber(fnum);
		chordFormula.setSpellingNumber(ChordManager.computeSpellingNumber(chordFormula));
		this.chordFormula =chordFormula;
		setNames();		// set the name and alternateNames if there are any
		setChordSpelling();
	}
	
	/**
	 * 
	 * @param chordFormula
	 * @param rootPitch
	 */
	public HarmonyChord(ChordFormula chordFormula, Pitch rootPitch) {
		this(chordFormula, rootPitch, Key.C_MAJOR);
	}
	
	/**
	 * creates a HarmonyChord from a string representation, such as "Bb9", "D7" or "C#m7-5"
	 * Also support slash chords such as "C9/D" for example.
	 * 
	 * @param chord String representation of a chord
	 * @param chordFormulas a  Map<String, ChordFormula> of chord formulas
	 * @throws IllegalArgumentException if chord name is not valid
	 */
	public HarmonyChord(String chord, Map<String, ChordFormula> chordFormulas) throws IllegalArgumentException {
		ChordInfo chordInfo = ChordManager.parseChordName(chord);
		String rootNote = chordInfo.getRootNote();
		String bass = chordInfo.getBassNote();
		String symbol = chordInfo.getChordSymbol();
		this.chordFormula = chordFormulas.get(symbol);
		this.root = new Pitch(rootNote);
		this.bassNote = new Pitch(bass);
		if(chordFormula != null) {
			chordPitches.addAll(chordFormula.createPitches(root, Key.C_MAJOR));
			setNames();		// set the name and alternateNames if there are any
			setChordSpelling();
		}
		else {
			// not a valid chord name
			throw new IllegalArgumentException(symbol + " is not a valid chord symbol");
		}
	}
	
	/**
	 * Deep copy constructor. Doesn't deep copy the ChordFormula but it does
	 * create new root Pitch and chordPitches. A new Id is also created for it.
	 * If other is a TERMINAL, SILENT or NULL_VALUE HarmonyChord,
	 * other will also be.
	 * @param other HarmonyChord to copy
	 */
	public HarmonyChord(HarmonyChord other) {
		this.root = new Pitch(other.getRoot());
		this.bassNote = this.root;
		setChordFormula(other.getChordFormula());
		int fnum = 0;
		int spellingNumber = 0;
		if(chordFormula.getSize() > 0) {
			chordPitches.addAll(chordFormula.createPitches(root));
			setChordSpelling();
			fnum = ChordManager.computeFormulaNumber(chordFormula);
			spellingNumber = ChordManager.computeSpellingNumber(chordFormula);
		}
		chordFormula.setFormulaNumber(fnum);
		chordFormula.setSpellingNumber(spellingNumber);
		setNames();
		setId(new ObjectId(new Date(), 1));
	}
	
	/**
	 * Deep copy constructor that transposes to a new Key.
	 * So for example, a C chord in the key of E-Major, would
	 * transpose to an Ab in the key of C-Major, a B7 would be G7 and so forth.
	 * @param other HarmonyChord to copy
	 */
	public  HarmonyChord(HarmonyChord other, Key key, Key newKey) {
		Pitch otherRoot = other.getRoot();
		int n = otherRoot.getChromaticScaleDegree(key);
		log.debug(otherRoot.toString() + ": n = " + n);
		this.root = new Pitch(newKey.getDesignation());
		this.bassNote = this.root;
		root.increment(n-1, newKey.getAlterationPreference(-1));	// we prefer flats to sharps
		log.debug("other root: " + otherRoot.toString() + " key: " + key.toString() +
				" newKey: " + newKey + " newRoot: " + root.toString());
		chordFormula = other.getChordFormula();
		chordPitches.addAll(chordFormula.createPitches(root, newKey));
		setNames();
		alternateNames.addAll(other.getAlternateNames());
		setChordSpelling();
	}
	
	/**
	 * A HarmonyChord is silent if the underlying ChordFormula isSilent or the name is "0"
	 * A silent HarmonyChord serves as an end delimiter in a ChordProgression.
	 * @return true if silent
	 */
	public boolean isSilent() {
		return this.getChordFormula().isSilent() || name.equals("0");
	}
	
	private void setChordSpelling() {
		spelling = new ArrayList<String>();
		for(Pitch pitch: chordPitches) {
			spelling.add(pitch.toString());
		}
	}
	
	@Override
	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}
	
	/**
	 * The name plus the spelling delimited by a space.
	 * For example, "B9 [B, D#, F#, A, C#]"
	 * A spelling of a silent HarmonyChord is an empty list: []
	 */
	public String toString() {
		return toString(isIncludeSpellingInToString());
	}

	public String toString(boolean includeSpelling) {
		return includeSpelling ? name + " " + spelling : name;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public List<String> getSpelling() {
		return spelling;
	}

	public void setRoot(Pitch root) {
		this.root = root;
	}

	public ChordFormula getChordFormula() {
		return chordFormula;
	}

	protected void setChordFormula(ChordFormula chordFormula) {
		this.chordFormula = chordFormula;
	}

	public Pitch getRoot() {
		return root;
	}

	public List<Pitch> getChordPitches() {
		return chordPitches;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getAlternateNames() {
		return alternateNames;
	}
	
	protected void setNames() {
		for(int i=0; i<chordFormula.getSymbols().size(); i++) {
			if(i==0) {
				String symbol = chordFormula.getSymbols().get(i);
				name = root.toString(-1) + (symbol.equals("M") ? "" : symbol);
			}
			else {
				alternateNames.add(chordFormula.getSymbols().get(i));
			}
		}
	}

	public int getFormulaNumber() {
		return getChordFormula().getFormulaNumber();
	}
	public int getSpellingNumber() {
		return getChordFormula().getSpellingNumber();
	}
	
	public static boolean isIncludeSpellingInToString() {
		return includeSpellingInToString;
	}

	public static void setIncludeSpellingInToString(boolean inc) {
		includeSpellingInToString = inc;
	}

	/**
	 * How different are the formula numbers?
	 * @param other
	 * @return the number of notes different in the formula number of this and another HarmonyChord
	 * NOTE - each difference may be counted twice
	 * For example - C E G and C Eb G have 2 notes different - E and Eb, although it's only 1
	 * note in each spelling. Hope that's not too confusing. A note that appears in one chard
	 * but not another would also have a difference of 1. The way to distinguish
	 * is by the number of notes in each chord.
	 */
	public int notesDifferent(HarmonyChord other) {
		int diff = getFormulaNumber() ^ other.getFormulaNumber();
		int sum = 0;
		for(int i=0; i<12; i++) {
			sum += (diff >> i) & 1;
		}
		return sum;
	}
	
	/**
	 * How similar are the formula numbers?
	 * @param other
	 * @return the number of notes that are the same in the formula number of this and another HarmonyChord
	 */
	public int notesSame(HarmonyChord other) {
		int diff = getFormulaNumber() & other.getFormulaNumber();
		int sum = 0;
		for(int i=0; i<12; i++) {
			sum += (diff >> i) & 1;
		}
		return sum;
	}
	
	/**
	 * Compares this HarmonyChord to another by comparing respective formula numbers.
	 * The formula number of a "silent" HarmonyChord is 0.
	 * @param HarmonyChord other
	 * @return formula number of this - formula number of other
	 */
	@Override
	public int compareTo(HarmonyChord other) {
		return getChordFormula().getFormulaNumber() - other.getChordFormula().getFormulaNumber();
	}
	
	/**
	 * HarmonyChords are equal if they have the same formula number and name
	 * @param other HarmonyChord to test for equality
	 * @return true if equal
	 */
	public boolean equals(HarmonyChord other) {
		return compareTo(other) == 0 && other.getName().equals(getName());
	}
	
	/**
	 * HarmonyChords are identical if they are equal AND they have the same spelling number
	 * @param other  HarmonyChord to test
	 * @return true if identical
	 */
	public boolean identical(HarmonyChord other) {
		return compareTo(other) == 0 && getChordFormula().getSpellingNumber() == other.getChordFormula().getSpellingNumber();
	}

	@Override
	public Pitch getTerminal() {
		return TERMINAL;
	}

	@Override
	public Pitch getNullValue() {
		return NULL_VALUE;
	}

}
