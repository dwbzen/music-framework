package org.dwbzen.music.element.song;

import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads all the chord formulas into memory.
 * All the chords in chords.json (resources/data/music) are available in a map
 * indexed by name, symbol(s) and alternate names.
 * 
 * @author don_bacon
 * @see music.element.song.ChordManager loadChordFormulas
 * 
 */
public class ChordLibrary {
	/**
	 * Map keys are the name ("Major", "Diminished" etc)
	 * the symbols "+(M7)", "M7+5", "M7#5" etc.
	 * and any alternate names.
	 */
	static TreeMap<String,ChordFormula> chordFormulas = new TreeMap<String, ChordFormula>();
	
	//
	// Define common ChordFormulas here.
	//
	// Triad group
	// { "name" : "Major" , "symbols" : ["M"], "groups" : [ "triad"] , "formula" : [ 4 , 3 ] , "intervals": ["M3", "m3"], "size" : 2, "chordSize" : 3, "formulaNumber" : "145" }
	// { "name" : "Minor" , "symbols" : ["m"], "groups" : [ "triad"] , "formula" : [ 3 , 4 ] , "intervals": ["m3", "M3"], "size" : 2, "chordSize" : 3, "formulaNumber" : "137" }
	// { "name" : "Diminished" , "symbols" : ["dim"], "groups" : [ "triad"] , "formula" : [ 3 , 3 ] , "intervals": ["m3", "m3"], "size" : 2, "chordSize" : 3, "formulaNumber" : "73" }
	// { "name" : "Augmented" , "symbols" : ["aug", "+"], "groups" : [ "triad"] , "formula" : [ 4 , 4 ] , "intervals": ["M3", "M3"], "size" : 2, "chordSize" : 3, "formulaNumber" : "273" }
	//
	public static final int[] MAJOR_FORMULA = {4,3};
	public static final String[] MAJOR_INTERVALS = {"M3", "m3"};
	public static final ChordFormula MAJOR = new ChordFormula("Major", "M", "triad", MAJOR_FORMULA, MAJOR_INTERVALS);
	
	public static final int[] MINOR_FORMULA = {3,4};
	public static final String[] MINOR_INTERVALS = {"m3", "M3"};
	public static final ChordFormula MINOR = new ChordFormula("Minor", "m", "triad", MINOR_FORMULA, MINOR_INTERVALS);
	
	public static final int[] DIMINISHED_FORMULA = {3,3};
	public static final String[] DIMINISHED_INTERVALS = {"m3", "m3"};
	public static final ChordFormula DIMINISHED = new ChordFormula("Diminished", "dim", "triad", DIMINISHED_FORMULA, DIMINISHED_INTERVALS);

	public static final int[] AUGMENTED_FORMULA = {4,4};
	public static final String[] AUGMENTED_INTERVALS = {"M3", "M3"};
	public static final String[] AUGMENTED_SYMBOLS = {"aug", "+"};
	public static final ChordFormula AUGMENTED = new ChordFormula("Minor", null, AUGMENTED_SYMBOLS, "triad", AUGMENTED_FORMULA, AUGMENTED_INTERVALS);

	// seventh group
	// { "name" : "Diminished seventh" , "symbols" : ["dim7"], "groups" : [ "seventh"] , "formula" : [ 3, 3, 3 ] , "intervals": ["m3", "m3", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "585" }
	// { "name" : "Minor seventh" , "symbols" : ["m7", "min7", "-7"], "groups" : [ "seventh"] , "formula" : [ 3, 4, 3 ] , "intervals": ["m3", "M3", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1161" }
	// { "name" : "Half-diminished seventh" , "alternateNames" : ["minor seventh flat fifth", "minor sixth"], "symbols" : ["m7-5", "m7b5", "-7b5"], "groups" : [ "seventh"] , "formula" : [ 3, 3, 4 ] , "intervals": ["m3", "m3", "M3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1097" }
	// { "name" : "Minor seventh flat fifth" , "symbols" : ["m7-5", "m7b5", "-7b5"], "groups" : [ "seventh"] , "formula" : [ 3, 3, 4 ] , "intervals": ["m3", "m3", "M3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1097" }
	// { "name" : "Minor major seventh" , "symbols" : ["m(M7)", "m maj7", "-M7"], "groups" : [ "seventh"] , "formula" : [ 3, 4, 4 ] , "intervals": ["m3", "M3", "M3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "2185" }
	// { "name" : "Dominant seventh" , "symbols" : ["7"], "groups" : [ "seventh"] , "formula" : [ 4, 3, 3 ] , "intervals": ["M3", "m3", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1169" }
	// { "name" : "Major seventh" , "symbols" : ["M7", "maj7"], "groups" : [ "seventh"] , "formula" : [ 4, 3, 4 ] , "intervals": ["M3", "m3", "M3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "2193" }
	// { "name" : "Augmented seventh" , "symbols" : ["+7", "aug7", "7+", "C7+5", "C7#5"], "groups" : [ "seventh"] , "formula" : [ 4, 4, 2 ] , "intervals": ["M3", "m3", "M2"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1297" }
	// { "name" : "Augmented major seventh" , "symbols" : ["+(M7)", "M7+5", "M7#5"], "groups" : [ "seventh"] , "formula" : [ 4, 4, 3 ] , "intervals": ["M3", "m3", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "2321" }
	public static final int[] DIMINISHED_SEVENTH_FORMULA = {3,3,3};
	public static final String[] DIMINISHED_SEVENTH_INTERVALS = {"m3", "m3", "m3"};
	public static final String[] DIMINISHED_SEVENTH_SYMBOLS = {"dim7"};
	public static final ChordFormula DIMINISHED_SEVENTH = new ChordFormula("Diminshed seventh", null, DIMINISHED_SEVENTH_SYMBOLS, "seventh", DIMINISHED_SEVENTH_FORMULA, DIMINISHED_SEVENTH_INTERVALS);

	public static final int[] MINOR_SEVENTH_FORMULA = {3,4,3};
	public static final String[] MINOR_SEVENTH_INTERVALS = {"m3", "M3", "m3"};
	public static final String[] MINOR_SEVENTH_SYMBOLS = {"m7", "min7", "-7"};
	public static final ChordFormula MINOR_SEVENTH = new ChordFormula("Minor seventh", null, MINOR_SEVENTH_SYMBOLS, "seventh", MINOR_SEVENTH_FORMULA, MINOR_SEVENTH_INTERVALS);

	public static final int[] HALF_DIMINISHED_SEVENTH_FORMULA = {3,3,4};
	public static final String[] HALF_DIMINISHED_SEVENTH_INTERVALS = {"m3", "m3", "M3"};
	public static final String[] HALF_DIMINISHED_SEVENTH_SYMBOLS = {"m7-5", "m7b5", "-7b5"};
	public static final String[] HALF_DIMINISHED_SEVENTH_ALTERNATE_NAMES = {"Minor seventh flat fifth", "Minor sixth"};
	public static final ChordFormula HALF_DIMINISHED_SEVENTH = new ChordFormula("Half-diminished seventh", HALF_DIMINISHED_SEVENTH_ALTERNATE_NAMES, HALF_DIMINISHED_SEVENTH_SYMBOLS, "seventh", HALF_DIMINISHED_SEVENTH_FORMULA, HALF_DIMINISHED_SEVENTH_INTERVALS);

	public static final int[] MINOR_SEVENTH_FLAT_FIFTH_FORMULA = {3,3,4};
	public static final String[] MINOR_SEVENTH_FLAT_FIFTH_INTERVALS = {"m3", "m3", "M3"};
	public static final String[] MINOR_SEVENTH_FLAT_FIFTH_SYMBOLS = {"m7-5", "m7b5", "-7b5"};
	public static final String[] MINOR_SEVENTH_FLAT_FIFTH_ALTERNATE_NAMES = {"Half-diminished seventh", "Minor sixth"};
	public static final ChordFormula MINOR_SEVENTH_FLAT_FIFTH = new ChordFormula("Minor seventh flat fifth", MINOR_SEVENTH_FLAT_FIFTH_ALTERNATE_NAMES, MINOR_SEVENTH_FLAT_FIFTH_SYMBOLS, "seventh", MINOR_SEVENTH_FLAT_FIFTH_FORMULA, MINOR_SEVENTH_FLAT_FIFTH_INTERVALS);
	
	public static final int[] MINOR_MAJOR_SEVENTH_FORMULA = {3,4,4};
	public static final String[] MINOR_MAJOR_SEVENTH_INTERVALS = {"m3", "M3", "M3"};
	public static final String[] MINOR_MAJOR_SEVENTH_SYMBOLS = {"m(M7)", "m maj7", "-M7"};
	public static final ChordFormula MINOR_MAJOR_SEVENTH = new ChordFormula("Minor major seventh", null, MINOR_MAJOR_SEVENTH_SYMBOLS, "seventh", MINOR_MAJOR_SEVENTH_FORMULA, MINOR_MAJOR_SEVENTH_INTERVALS);

	// 9th, 11th, 13th - extended group
	// { "name" : "Dominant ninth" , "symbols" : ["9"], "groups" : [ "extended"] , "formula" : [ 4, 3, 3, 4 ] , "intervals": ["M3", "m3", "m3", "M3"], "size" : 4, "chordSize" : 5, "formulaNumber" : "1173" }
	// { "name" : "Major ninth" , "symbols" : ["maj9", "M9"], "groups" : [ "extended"] , "formula" : [ 4, 3, 4, 3] , "intervals": ["M3", "m3", "M3", "m3"], "size" : 4, "chordSize" : 5, "formulaNumber" : "2197" }
	// { "name" : "Minor ninth" , "symbols" : ["m9"], "groups" : [ "extended"] , "formula" : [ 3, 4, 3, 4] , "intervals": ["m3", "M3", "m3", "M3"], "size" : 4, "chordSize" : 5, "formulaNumber" : "1165" }
	// { "name" : "Dominant eleventh" , "symbols" : ["11"], "groups" : [ "extended"] , "formula" : [ 4, 3, 3, 4, 3 ] , "intervals": ["M3", "m3", "m3", "M3", "m3"], "size" : 5, "chordSize" : 6, "formulaNumber" : "1205" }
	// { "name" : "Dominant thirteenth" , "symbols" : ["13"], "groups" : [ "extended"] , "formula" : [ 4, 3, 3, 4, 3, 4 ] , "intervals": ["M3", "m3", "m3", "M3", "m3", "M3"], "size" : 6, "chordSize" : 7, "formulaNumber" : "1717" }
	public static final int[] DOMINANT_NINTH_FORMULA = {4, 3, 3, 4};
	public static final String[] DOMINANT_NINTH_INTERVALS = {"M3", "m3", "m3", "M3"};
	public static final String[] DOMINANT_NINTH_SYMBOLS = {"9"};
	public static final ChordFormula DOMINANT_NINTH = new ChordFormula("Dominant ninth", null, DOMINANT_NINTH_SYMBOLS, "extended", DOMINANT_NINTH_FORMULA,DOMINANT_NINTH_INTERVALS);
	
	public static final int[] MAJOR_NINTH_FORMULA = {4, 3, 4, 3};
	public static final int[] MINOR_NINTH_FORMULA = { 3, 4, 3, 4};
	public static final int[] DOMINANT_ELEVENTH_FORMULA = {4, 3, 3, 4, 3 };
	public static final int[] DOMINANT_THIRTEENTH_FORMULA = {4, 3, 3, 4, 3, 4 };
	
	// altered group
	// { "name" : "Seventh raised fifth" , "symbols" : ["7+5", "7#5"], "groups" : [ "altered"] , "formula" : [ 4, 4, 2 ] , "intervals": ["M3", "M3", "M2"], "size" : 3, "chordSize" : 4, "formulaNumber" : "511" }
	// { "name" : "Seventh flat ninth" , "symbols" : ["7-9", "7b9"], "groups" : [ "altered"] , "formula" : [ 4, 3, 3, 3 ] , "intervals": ["M3", "m3", "m3", "m3"], "size" : 4, "chordSize" : 5, "formulaNumber" : "1171" }
	// { "name" : "Seventh sharp ninth", "description" : "The Hendrix chord", "symbols" : ["7+9", "7#9"], "groups" : [ "altered"] ,"formula" : [ 4, 3, 3, 5 ] , "intervals": ["M3", "m3", "m3", "P4"], "size" : 4, "chordSize" : 5, "formulaNumber" : "1177" }
	// { "name" : "Seventh augmented eleventh", "symbols" : ["7+11", "7#11"], "groups" : [ "altered"] , "formula" : [ 4, 3, 3, 8 ] , "intervals": ["M3", "m3", "m3", "m6" ], "size" : 4, "chordSize" : 5, "formulaNumber" : "1233" }
	// { "name" : "Ninth augmented eleventh", "symbols" : ["9+11", "9#11"], "groups" : [ "altered"] , "formula" : [ 4, 3, 3, 4, 4 ] , "intervals": ["M3", "m3", "m3", "M3", "M3"], "size" : 5, "chordSize" : 6, "formulaNumber" : "1237" }
	// { "name" : "Seventh flat thirteenth", "symbols" : ["7-13", "7b13"], "groups" : [ "altered"] ,"formula" : [ 4, 3, 3, 4, 6 ] , "intervals": ["M3", "m3", "m3", "M3", "d5"], "size" : 5, "chordSize" : 6, "formulaNumber" : "1429" }

	// added tone
	// { "name" : "Add ninth" , "symbols" : ["add9", "2"], "groups" : [ "added tone"] , "formula" : [ 4, 3, 7] , "intervals": ["M3", "m3", "P5"], "size" : 3, "chordSize" : 4, "formulaNumber" : "149" }
	// { "name" : "mu major-1" , "symbols" : ["mu", "add9", "Steely Dan Chord"], "groups" : [ "added tone"] , "formula" : [ 7, 7, 2] , "intervals": ["P5", "P5", "M2"], "size" : 3, "chordSize" : 4, "formulaNumber" : "149" }
	// { "name" : "mu major-2" , "symbols" : ["mu2"], "groups" : [ "added tone"] , "formula" : [ 14, 2, 3] , "intervals": ["M9", "M2", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "149" }
	// { "name" : "Add fourth" , "symbols" : ["add11", "4"], "groups" : [ "added tone"] , "formula" : [4, 3, 10], "intervals": ["M3", "m3", "M9"], "size" : 3, "chordSize" : 4, "formulaNumber" : "177" }
	// { "name" : "Add six" , "symbols" : ["6", "add6"], "groups" : [ "added tone"] , "formula" : [4, 3, 2], "intervals": ["M3", "m3", "M2"], "size" : 3, "chordSize" : 4, "formulaNumber" : "657" }
	// { "name" : "Six-nine" , "symbols" : ["6+9"], "groups" : [ "added tone"] , "formula" : [4, 3, 2, 5], "intervals": ["M3", "m3", "M2", "P4"], "size" : 4, "chordSize" : 5, "formulaNumber" : "661" }
	// { "name" : "Mixed third" , "symbols" : ["m add3"], "groups" : [ "added tone"] , "formula" : [3, 1, 3], "intervals": ["m3", "m2", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "153" }
	// { "name" : "Minor sixth" , "symbols" : ["m6"], "groups" : [ "added tone"] , "formula" : [ 3, 3, 4 ] , "intervals": ["m3", "m3", "M3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1097" }
	
	// suspended chords
	// { "name" : "Sus2" , "symbols" : ["sus2"], "groups" : [ "suspended"] , "formula" : [ 2, 7] , "intervals": ["M2", "P5"], "size" : 2, "chordSize" : 3, "formulaNumber" : "517" }
	// { "name" : "Sus4" , "symbols" : ["sus4"], "groups" : [ "suspended"] , "formula" : [ 5, 2] , "intervals": ["P4", "M2"], "size" : 2, "chordSize" : 3, "formulaNumber" : "161" }
	// { "name" : "7Sus4" , "symbols" : ["7sus4"], "groups" : [ "suspended"] , "formula" : [ 5, 2, 3] , "intervals": ["P4", "M2", "m3"], "size" : 3, "chordSize" : 4, "formulaNumber" : "1185" }
	// { "name" : "Sus6" , "symbols" : ["sus6"], "groups" : [ "suspended"] , "formula" : [ 7, 2] , "intervals": ["P5", "M2"], "size" : 2, "chordSize" : 3, "formulaNumber" : "641" }
	// { "name" : "Jazz sus" , "symbols" : ["9sus4"], "groups" : [ "suspended"] , "formula" : [ 5, 2, 3, 4] , "intervals": ["P4", "M2", "m3", "M3"], "size" : 4, "chordSize" : 5 , "formulaNumber" : "1189"}
	// { "name" : "5" , "symbols" : ["5"], "groups" : [ "suspended"] , "formula" : [ 7] , "intervals": ["P5"], "size" : 1, "chordSize" : 2, "formulaNumber" : "129" }


	static {
		chordFormulas.put(MAJOR.getName(), MAJOR);
		chordFormulas.put(MINOR.getName(), MINOR);
		chordFormulas.put(DIMINISHED.getName(), DIMINISHED);
		chordFormulas.put(AUGMENTED.getName(), AUGMENTED);
		chordFormulas.put(DIMINISHED_SEVENTH.getName(), DIMINISHED_SEVENTH);
		chordFormulas.put(MINOR_SEVENTH.getName(), MINOR_SEVENTH);
		chordFormulas.put(HALF_DIMINISHED_SEVENTH.getName(), HALF_DIMINISHED_SEVENTH);
		chordFormulas.put(MINOR_SEVENTH_FLAT_FIFTH.getName(), MINOR_SEVENTH_FLAT_FIFTH);
		chordFormulas.put(MINOR_MAJOR_SEVENTH.getName(), MINOR_MAJOR_SEVENTH);
		chordFormulas.put(DOMINANT_NINTH.getName(), DOMINANT_NINTH);
	}
	
	public static  TreeMap<String,ChordFormula> getChordFormulas() {
		return chordFormulas;
	}
	
	static ObjectMapper mapper = new ObjectMapper();
	static final String CHORD_FORMULAS = "/data/music/songs/allChordFormulas.json";
	
	public ChordLibrary() {
		
	}
}
