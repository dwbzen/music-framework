package music.element.song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import music.element.ScaleFormula;

import org.apache.log4j.Logger;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

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
@Entity(value="ChordFormula", noClassnameStored=true)
public class ChordFormula extends ScaleFormula implements IChordFormula {

	private static final long serialVersionUID = 3941757049122502147L;
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	private static Morphia morphia = new Morphia();

	/**
	 * Number of notes in the chord. Typically size + 1
	 */
	@Property	private int chordSize;
	/**
	 * chordSpellingNumber - double word binary, 32 bits, accommodates 2 octave + 8 step span
	 */
	@Property	private int spellingNumber = 0xFFFFFFFF;
	@Property	private List<String> symbols = new ArrayList<String>();
	
	public static final ChordFormula SILENT = new ChordFormula();

	
	/**
	 * Create a null ChordFormula. Used for "silent" chords.
	 * A silent ChordFormula has a chordSize of 0.
	 */
	public ChordFormula() {
		super();
		this.chordSize = 0;
	}
	
	public ChordFormula(String name, String group, int[] frmla) {
		super(name, group, frmla);
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
		super(name, group, frmla, altNames, intvls);
		symbols = Arrays.asList(symbls);
		chordSize = intvls.length;
		setFormulaNumber(computeFormulaNumber(frmla));	
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
	public ChordFormula(String name, String[] altNames, String[] symbls, String[] group, int[] frmla, String[] intvls) {
		super(name, group, frmla, altNames, intvls);
		symbols = Arrays.asList(symbls);
		chordSize = intvls.length;
		setFormulaNumber(computeFormulaNumber(frmla));
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
		super(name, group, frmla, null, intvls);
		symbols.add(symbls);
		chordSize = intvls.length;
		setFormulaNumber(computeFormulaNumber(frmla));
	}
	
	public boolean isSilent() {
		return chordSize==0;
	}

	public int computeFormulaNumber() {
		int fnum = 0;
		List<Integer> ps = formulaToPitchSet(getFormula());
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			fnum += (1<<shiftamt);
		}
		return fnum;
	}
	
	public int computeSpellingNumber() {
		int fnum = 0;
		List<Integer> ps = formulaToPitchSet(getFormula());
		for(Integer i:ps) {
			fnum += (1<<i);
		}
		return fnum;
	}

	public static int computeFormulaNumber(int[] formula) {
		int fnum = 0;
		List<Integer> ps = formulaToPitchSet(formula);
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			fnum += (1<<shiftamt);
		}
		return fnum;
	}
	
	public String toJSON() {
		return morphia.toDBObject(this).toString();
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
	
	public String toString() {
		return getName();
	}
	
	/**
	 * Symbols and Alternate names (if any) comprise the key set.
	 * In general, a ChordFormula doesn't have an alternate name as a ScaleFormula would.
	 */
	@Override
	public Set<String> keySet() {
		Set<String> keyset = super.keySet();	// alternateNames
		keyset.addAll(symbols);
		return keyset;
	}
	
}
