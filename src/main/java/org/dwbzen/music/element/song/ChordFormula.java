package org.dwbzen.music.element.song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.Alteration;
import org.dwbzen.music.element.IFormula;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.util.IMapped;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulates meta-information about a chord:
 * <dl>
 * <dt>name</dt>  <dd>The root plus the type or how the chord is commonly known. For example, "CMajor", "BbMinor seventh" etc.</dd>
 * <dt>symbols</dt>  <dd>a List<String> of common symbols with the most common appearing first. "M", "M7#5", "9" etc.</dd>
 * <dt>groups</dt>  <dd>a List<String> of groups this chord belongs to, for example "triad", "seventh", "added tone" etc.</dd>
 * <dt>formula</dt>  <dd>a List<Integer> of #steps each note is from the previous.<br>
 * 			 For example, formula for a major chord is [4,3] or, from the root, 4 steps to the next note then 3 steps.<br>
 * 			 Note that the formula does not include a final interval to double the root.</dd>
 * 
 * <dt>intervals</dt>  <dd>the formula expressed as intervals using the standard notation.<br>
 * 	<ul>
 * 	<li>P - perfect (4th, 5th)</li>
 *  <li>d - diminished (4th, 5th)</li>
 * 	<li>M - major (2nd, 3rd, 6th, 7th, 9th, 11th, 13th)</li>
 * 	<li>m - minor (2nd, 3rd, 6th, 7th, 9th, 11th, 13th)</li>
 *  </ul>
 * 	So a major chord for example has intervals ["M3", "m3"]
 *  </dd>
 * 
 * <dt>size</dt>  <dd> the length of the formula and interval lists</dd>
 * <dt>chordSize</dt>  <dd> the length (number of notes) of the chord></dd>
 * <dt>formulaNumber</dt>  <dd>is a 3-byte binary (12 bits) where each bit corresponds to the scale degree-1
 *		of the notes in the chord, with the root note at "C".<br>
 *     This is the absolute formulaNumber expressed as 3-byte Hex.</dd>
 * </dl>
 *  <h3>Formula Number</h3>
 *  The formula number is a unique codified form of the chord formula.<br>
 * 	For example a "Dominant thirteenth" with root C (formula [4,3,3,4,7]) has the notes: C, E, G, Bb, D, F, A<br>
 *	scale degree-1 of the notes without regard to order (C,D,E,F,G,A,Bb)<br>
 *  in reverse order is 10,9,7,4,2,0 as binary  0110 1001 0101,  0x695 or 1685 <br>
 *
 *	To find the relative formula number, rotate left by the degree-1 of the desired root.<br>
 *  So E9 would be rotateLeft(0100 1001 0101 , 4) == 1001 0101 0100 or HEX(954)<br>
 *  The formulaNumber can be set directly or computed from the formula.
 *  
 *  <h3>Spelling Number</h3>
 *  The chord spelling number is also unique for a given spelling (the notes in the order played).<br>
 *  The chord spellingNumber is double word binary, 32 bits, accommodates 2 octave + 8 step span (a fifth)<br>
 *  represents a realization of the chord, i.e. the actual notes.<br>
 *  For example, a C7#9 played as C4, E4, G4, Bb4, D#5 <br>
 *  which would have a spelling number: 0x05081411</p>
 *  
 *  <h3>Inversions</h3>
 *  A chord having a note other than the root in the bass is an inversion of that chord.<br>
 *  In chord tablature these are notated as "slash" chords. For example, a C7 in root position is C,E,G,Bb<br>
 *  C7/E is the first inversion, C7/G is the second and so on.<br>
 *  Expressing these as a formula requires doubling the root. The inversions are then simply left rotations of that formula.<br>
 *  For example doubling the root of C7 gives the formula [4,3,3,2] (or M3,m3,m3,M2), C7/E = [3,3,2,4] and so on<br>
 *  This is important when trying to determine the formula for an arbitrary Chord which may or may not have an associated formula.<br>
 *  This idea is captured in the inversions property which is a Map<Integer, List<Integer>>.<br>
 *  The key is index of the pitch in root position that's in the bass (the lowest pitch).<br>
 *  The List<Integer> is the inversion omitting the last number. <br>
 *  For example, inversions for a 7th chord are {0, [4,3,3]}, {1, [3,3,2]}, {2, [3,2,4]}, {3, [2,4,3]}<br>
 *  The inversions for the chord formula is created when the formula is loaded</p>
 *  
 * Chord formulas are persisted in the "chord_formula" MongoDB collection<br>
 * and loaded from the resource file, "allChordFormulas.json"</p>
 * Wikipedia References:
 * <ul>
 * <li><a href="https://en.wikipedia.org/wiki/Chord_(music)">Chord (Music)</a></li>
 * <li><a href="https://en.wikipedia.org/wiki/Interval_(music)">Interval (Music)</a></li>
 * </ul>
 *  
 * 
 * @author don_bacon
 *
 */
public class ChordFormula implements IChordFormula, IJson, Cloneable, IMapped<String> {

	private static final long serialVersionUID = 3941757049122502147L;
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	
	private  ObjectMapper mapper = new ObjectMapper();
	@JsonPropertyOrder({"name"})

	@JsonProperty("name")			private String name;
	@JsonProperty("symbols")		private List<String> symbols = new ArrayList<String>();
	@JsonProperty("groups")			private List<String> groups = new ArrayList<String>();
	@JsonProperty("formula")		private List<Integer> formula = new ArrayList<Integer>();
	@JsonIgnore						private List<Integer> doubleRootFormula = null;
	@JsonProperty("inversions")		private Map<Integer, List<Integer>> inversions = null;
	@JsonProperty					private List<Integer> inversionFormulaNumbers = null;
	@JsonIgnore						private int slash = -1;	// if >=0, the index of the bass note

	/**
	 * Number of notes in the chord.
	 */
	@JsonProperty("chordSize")	private int chordSize;
	/**
	 * size (#elements) of formula
	 */
	@JsonIgnore		private int size;
	
	/**
	 * chordSpellingNumber - double word binary, 32 bits, accommodates 2 octave + 8 step span
	 */
	@JsonProperty("spellingNumber")			private int spellingNumber = 0xFFFFFFFF;
	@JsonInclude(Include.NON_EMPTY)			private List<String> alternateNames = new ArrayList<String>();
	@JsonInclude(Include.NON_EMPTY)			private String description = null;	// optional descriptive text
	
	@JsonProperty("intervals")				private List<String> intervals = new ArrayList<String>();
	/**
	 * The pitches, octave neutral root of "C", derived from the formula
	 */
	@JsonProperty("spelling")				private String spelling = null;
	
	@JsonIgnore	private List<String> spellingNotes = new ArrayList<String>();	
	@JsonIgnore	private List<Pitch> template = new ArrayList<Pitch>();
	/**
	 * formulaNumber is a 3-byte binary (12 bits) where each bit corresponds to the scale degree-1
	 * Works for a scale or a chord.
	 */
	@JsonProperty("formulaNumber")			private int formulaNumber = 0;
	
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
		template.addAll(createPitches(Pitch.C));
		addSpelling();
		getInversions();
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
		symbols = Arrays.asList(symbls);
		chordSize = intvls.length;
		if(intvls != null && intvls.length > 0) { intervals = Arrays.asList(intvls); }
		if(altNames != null && altNames.length > 0 ) { alternateNames = Arrays.asList(altNames); }
		setFormulaNumber(computeFormulaNumber(frmla));
		spellingNumber = computeSpellingNumber();
		getInversions();
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
		spellingNumber = computeSpellingNumber();
		getInversions();
	}
	
	public Object clone() {
		ChordFormula cf = null;
		try {
			cf = (ChordFormula)super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Clone not supported (ChordFormula)");
		}
		return cf;
	}
	
	@JsonIgnore	
	public boolean isSilent() {
		return chordSize==0;
	}

	public int computeFormulaNumber() {
		return computeFormulaNumber(getFormula());
	}
	
	public int computeFormulaNumber(List<Integer> aformula) {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchIndexes(aformula);
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			fnum += (1<<shiftamt);
		}
		return fnum;
	}
	
	public int computeSpellingNumber() {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchIndexes(getFormula());
		for(Integer i:ps) {
			fnum += (1<<i);
		}
		return fnum;
	}

	public static int computeFormulaNumber(int[] formula) {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchIndexes(formula);
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			int amt = (1<<shiftamt);
			fnum += amt;
		}
		return fnum;
	}

	public  List<Pitch> createPitches(Pitch root) {
		return createPitches(root, Key.C_MAJOR);
	}
	
	public  List<Pitch> createPitches(Pitch root, Key akey) {
		Alteration altpref = determinAlterationPreference(akey);
		List<Pitch> pitches = IFormula.createPitches(formula, root, akey, altpref);
		pitches.forEach(p -> spellingNotes.add(p.toString()));
		return pitches;
	}
	
	public Alteration determinAlterationPreference(Key akey) {
		Alteration altpref = Alteration.FLAT;	// the default
		Pitch p = akey.getDesignation();
		String keystep = p.getStep().name().toUpperCase();
		if(p.getAlteration() < 0) {
			altpref = Alteration.FLAT;
		}
		else if(p.getAlteration() > 0) {
			altpref = Alteration.FLAT;
		}
		else {
			if("ABDEG".contains(keystep)) {		// the sharp keys that don't include an alteration
				altpref = Alteration.SHARP;
			}
		}
		return altpref;
	}
	
	public String toJSON() {
		return toJson();
	}

	public int getChordSize() {
		return chordSize;
	}

	public void setChordSize(int chordSize) {
		this.chordSize = chordSize;
	}
	
	public int getSize() {
		return formula.size();
	}

	public int getSpellingNumber() {
		return spellingNumber;
	}

	public void setSpellingNumber(int chordSpellingNumber) {
		this.spellingNumber = chordSpellingNumber;
	}

	public List<String> getSymbols() {
		return this.symbols;
	}
	
	public void setSymbols(List<String> syms) {
		this.symbols = syms;
	}
	
	public void addSymbol(String asymbol) {
		this.symbols.add(asymbol);
	}
	
	/**
	 * Gets the first symbol only, which is the most common.
	 * @return
	 */
	@JsonIgnore
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
		size = frmla.length;
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

	public List<Pitch> getTemplate() {
		return template;
	}
	
	public String getSpelling() {
		return spelling;
	}
	
	/**
	 * Sets spelling and spellingNotes
	 * @param sp String, for example "C Eb G Bb"
	 */
	public void setSpelling(String sp) {
		spelling = sp;
	}
	
	public List<String> getSpellingNotes() {
		return spellingNotes;
	}
	
	public void setSpellingNotes(List<String> notes) {
		spellingNotes.addAll(notes);
	}
	
	public void setSpellingNotes(String notes) {
		for(String note : notes.split(" ")) {
			spellingNotes.add(note);
		}
	}
	
	public void addSpelling() {
		StringBuffer sb = new StringBuffer();
		for(Pitch p : createPitches(Pitch.C, Key.C_MAJOR)) {
			sb.append(p.toString(-1));
			sb.append(" ");
		}
		spelling = sb.substring(0, sb.length()-1);
		setSpellingNotes(spelling);
	}

	public Map<Integer, List<Integer>> getInversions() {
		if(inversions == null) {
			createInversions();
		}
		return inversions;
	}

	public void addInversion(int key, List<Integer> value) {
		getInversions().put(key, value);
	}
	
	/**
	 * Creates an inversions Map for this ChordFormula.<br>
	 * Inversions all have the same formulaNumber as the chord in root position.<br>
	 * The difference is the root note which is expressed as a slash chord.<br>
	 * For example, the 1st inversion of C7 (E G Bb C) is C7/E.
	 * 
	 */
	public void createInversions() {
		if(inversions == null) {
			inversions = new TreeMap<>();
			inversionFormulaNumbers = new ArrayList<>();
		}
		if(doubleRootFormula == null) {
			doubleRootFormula = new ArrayList<>();
			int sum = 0;
			for(int i=0; i<formula.size(); i++) {
				doubleRootFormula.add(formula.get(i));
				sum += formula.get(i);
			}
			if(sum <=12) {
				doubleRootFormula.add(12-sum);
			}
			else {
				doubleRootFormula.add(24-sum);
			}
		}
		int nInversions = doubleRootFormula.size();
		List<Integer> inversion = null;
		/*
		 * rotate the doubleRootFormula to the left for each inversion and calculate its formula number for easy lookup
		 */
		for(int index = 0; index<nInversions; index++) {
			if(index == 0) {
				inversions.put(index, doubleRootFormula);
				inversionFormulaNumbers.add(computeFormulaNumber(doubleRootFormula.subList(0, nInversions-1)));
			}
			else {
				inversion = new ArrayList<>(doubleRootFormula);
				Collections.rotate(inversion, -index);
				inversions.put(index, inversion);
				inversionFormulaNumbers.add(computeFormulaNumber(inversion.subList(0, nInversions-1)));
			}
		}
	}
	
	public List<Integer> getInversionFormulaNumbers() {
		if(inversionFormulaNumbers == null) {
			createInversions();
		}
		return inversionFormulaNumbers;
	}

	public int getSlash() {
		return slash;
	}

	public void setSlash(int slash) {
		this.slash = slash;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(getName());
		sb.append(", symbol: " + getSymbol());
		try {
			sb.append(", formula: " + mapper.writeValueAsString(getFormula()));
		} catch (JsonProcessingException e) {
			System.err.println("Cannot serialize because " + e.toString());
			e.printStackTrace();
		}
		sb.append(", formula#: " + formulaNumber);
		sb.append(", spelling: " + spelling);
		return sb.toString();
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
		ChordFormula f =  ChordLibrary.DOMINANT_NINTH;
		//System.out.println(f.toString());
		String jstr = f.toJson(true);
		System.out.println(jstr);
		
		ObjectMapper mapper = new ObjectMapper();
		ChordFormula chordFormula = null;
		try {
			chordFormula = mapper.readValue(jstr, ChordFormula.class);
		} catch (IOException e) {
			log.error("Cannot deserialize " + jstr + "\nbecause " + e.toString());
		}
		if(chordFormula != null) {
			System.out.println(chordFormula.toJson());
		}
	}
	
}
