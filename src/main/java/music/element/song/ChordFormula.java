package music.element.song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mathlib.util.IJson;
import music.element.IFormula;
import music.element.Key;
import music.element.Pitch;
import util.IMapped;
import util.music.MissingElementException;

/**
 * Encapsulates meta-information about a chord:
 * name - "Major", "Minor seventh" etc.
 * symbols - array of common symbols with the most common appearing first. "M", "M7#5", "9" etc.
 * groups - array of groups this chord belongs to. "triad", "seventh", "added" etc.
 * formula - integer array of #steps each note is from the previous.
 * 			 For example, formula for a major chord is [4,3] or, from the root, 4 steps to the next note
 * 			 then 3 steps.
 * intervals - the formula expressed as intervals using the standard notation.
 * 			   P - perfect (4th, 5th). d - diminished (4th, 5th),
 * 			   M - major (2nd, 3rd, 6th, 7th, 9th, 11th, 13th).  m - minor (2nd, 3rd, 6th, 7th, 9th, 11th, 13th)
 * 			  So a major chord has intervals [M3, m3]
 * size - #elements in the formula/interval array
 * chordSize - #notes in the chord
 * chordNumber - is a 3-byte binary (12 bits) where each bit corresponds to the scale degree-1 
 *		of the notes in the chord, with the root note at "C". This is the absolute chordNumber expressed as 3-byte Hex.
 *	note:		B	Bb	A	Ab	| G	 F#	 F	E |	Eb	D	Db	C
 *  degree-1:	11	10	9	8	| 7	 6	 5	4 |	3	2	1	0
 *              23  22  21  20  | 19 18  17 16| 15  14  13  12
 *                              | 31 30  29 28| 27  26  25  24
 * 	For example, "Dominant ninth" (formula "9") spelling is C,E,G,Bb,D 
 *	scale degree-1 == 10,7,4,2,0 as binary:  0100 1001 0101 or HEX(495)
 *	To find the relative chord number, rotate left by the degree-1 of the desired root.
 *  So E9 would be rotateLeft(0100 1001 0101 , 4) == 1001 0101 0100 or HEX(954)
 *  The chordNumber can be set directly or computed from the formula.
 *  The chordSpellingNumber is double word binary, 32 bits, accommodates 2 octave + 8 step span (a fifth)
 *  represents a realization of the chord, i.e. the actual notes.
 *  For example, a C7#9 could be played as C, E, Bb, C, G, C, D# (a span of 2 octaves + min 3rd)
 *  which would have a spelling number: 0x05081411
 * Chord formulas are persisted in the "chord_formula" MongoDB collection.
 * A Chord is a specific realization of a ChordFormula that has a root and other features.
 * @author don_bacon
 *
 */
public class ChordFormula implements IChordFormula, IJson, IMapped<String> {

	private static final long serialVersionUID = 3941757049122502147L;
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	private  ObjectMapper mapper = new ObjectMapper();

	/**
	 * Number of notes in the chord. Typically size + 1
	 */
	private int chordSize;
	/**
	 * chordSpellingNumber - double word binary, 32 bits, accommodates 2 octave + 8 step span
	 */
	private int spellingNumber = 0xFFFFFFFF;
	private String name;
	@JsonInclude(Include.NON_EMPTY)
	private List<String> alternateNames = new ArrayList<String>();
	private List<String> groups = new ArrayList<String>();
	private List<Integer> formula = new ArrayList<Integer>();
	private List<String> symbols = new ArrayList<String>();
	private String description = null;	// optional descriptive text
	private List<String> intervals = new ArrayList<String>();
	/**
	 * formulaNumber is a 3-byte binary (12 bits) where each bit corresponds to the scale degree-1
	 * Works for a scale or a chord.
	 */
	private int formulaNumber = 0;
	
	public static final ChordFormula SILENT = new ChordFormula();

	
	/**
	 * Create a null ChordFormula. Used for "silent" chords.
	 * A silent ChordFormula has a chordSize of 0.
	 */
	public ChordFormula() {
		this.chordSize = 0;
	}
	
	public ChordFormula(String name, String group, int[] frmla) {
		this.name = name;
		groups.add(group);
		setFormula(frmla);
	}
	
	/**
	 * Constructor used for static initialization of common chord formulas
	 * @param name
	 * @param altNames
	 * @param symbls
	 * @param group
	 * @param frmla
	 * @param intvls
	 */
	public ChordFormula(String name, String[] altNames, String[] symbls, String group, int[] frmla, String[] intvls) {
		this(name, group, frmla);
		if(intvls != null && intvls.length > 0) { intervals = Arrays.asList(intvls); }
		if(altNames != null && altNames.length > 0 ) { alternateNames = Arrays.asList(altNames); }
		symbols = Arrays.asList(symbls);
		chordSize = intvls.length;
	}
	
	/**
	 * Constructor used for static initialization of common chord formulas
	 * @param name
	 * @param symbls
	 * @param group
	 * @param frmla
	 * @param intvls
	 */
	public ChordFormula(String name, String symbls, String group, int[] frmla, String[] intvls) {
		this(name, group, frmla);
		symbols.add(symbls);
		chordSize = intvls.length;
		if(intvls != null && intvls.length > 0) { intervals = Arrays.asList(intvls); }
		setFormulaNumber(computeFormulaNumber(frmla));
	}
	
	public boolean isSilent() {
		return chordSize==0;
	}

	public int computeFormulaNumber() {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchSet(getFormula());
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			fnum += (1<<shiftamt);
		}
		return fnum;
	}
	
	public int computeSpellingNumber() {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchSet(getFormula());
		for(Integer i:ps) {
			fnum += (1<<i);
		}
		return fnum;
	}

	public static int computeFormulaNumber(int[] formula) {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchSet(formula);
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			fnum += (1<<shiftamt);
		}
		return fnum;
	}

	public  List<Pitch> createPitches(Pitch root) {
		return createPitches(root, Key.C_MAJOR);
	}
	
	public  List<Pitch> createPitches(Pitch root, Key key) {
		if(key == null) {
			throw new MissingElementException("Missing element: Key", new Throwable("IFormula.createPitches null key"));
		}

		List<Pitch> plist = new ArrayList<Pitch>();
		plist.add(root);
		Pitch current = root;
		Pitch next = null;
		int preference = (key.getSignature() != null && key.getSignature().length > 0) ? key.getSignature()[0].getAlteration() : 0;
		for(int i: formula) {
			next = new Pitch(current);
			if( i > 0) {
				next.increment(i);
				int alt = next.getAlteration();
				if(alt != 0 && alt != preference) {
					/*
					 * amounts to getting the enharmonic equivalent
					 * so D# same as Eb (preference -1)
					 * Db same as C# (preference 1)
					 */
					next.setEnharmonicEquivalent();
				}
			}
			plist.add(next);
			current = next;
		}
		return plist;
	}

	
	public String toJSON() {
		return toJson();
	}
	
	public String toJson() {
		String result = null;
		try {
			result = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			log.error(e.toString());
		}
		return result;
	}

	public int getChordSize() {
		return chordSize;
	}

	public void setChordSize(int chordSize) {
		this.chordSize = chordSize;
	}

	public int getSpellingNumber() {
		return spellingNumber;
	}

	public void setSpellingNumber(int chordSpellingNumber) {
		this.spellingNumber = chordSpellingNumber;
	}

	public List<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<String> symbols) {
		this.symbols = symbols;
	}

	/**
	 * Gets the first symbol only, which is the most common.
	 * @return
	 */
	public String getSymbol() {
		return (symbols.size()>0) ? symbols.get(0) : "";
	}
	
	public List<String> getIntervals() {
		return intervals;
	}

	public void setIntervals(List<String> intervals) {
		this.intervals = intervals;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getFormulaNumber() {
		return formulaNumber;
	}

	public void setFormulaNumber(int formulaNumber) {
		this.formulaNumber = formulaNumber;
	}
	
	public void setFormula(int[] frmla) {
		for(int i : frmla) {
			formula.add(i);
		}
		setFormulaNumber(computeFormulaNumber(frmla));
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

	public List<String> getGroups() {
		return groups;
	}

	public List<Integer> getFormula() {
		return formula;
	}

	public String toString() {
		return getName();
	}
	
	/**
	 * Symbols and Alternate names (if any) comprise the key set.
	 * In general, a ChordFormula doesn't have an alternate name as a ScaleFormula would.
	 */
	public Set<String> keySet() {
		Set<String> keyset = new HashSet<String>();
		if(alternateNames != null && alternateNames.size() > 0) {
			keyset.addAll(alternateNames);
		}
		keyset.addAll(symbols);
		return keyset;
	}
	
	public static void main(String...strings) {
		int[] cf = {4, 3, 3, 3};
		String[] intervals = {"M3", "m3", "m3", "M3"};
		ChordFormula f = new ChordFormula("Dominant ninth", "9", "extended", cf, intervals);
		System.out.println(f.toString());
		System.out.println(f.toJson());
	}
	
}
