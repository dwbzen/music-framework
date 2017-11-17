package music.element.song;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import music.element.IFormula;
import music.element.Key;
import music.element.Pitch;

/**
 * Creates a JSON representation for chord formulas in all roots.
 * 
 * Also has static utility methods to create HarmonyChords, compute chord and spelling numbers,
 * parse chords from the String representation (for example, "D7-5/C").
 * 
 * @author don_bacon
 *
 */
public class ChordManager {

	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	static ObjectMapper mapper = new ObjectMapper();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String,ChordFormula> chordFormulasMap = new TreeMap<String, ChordFormula>();
	private String resourceFile = null;
	private String jsonFormat = null;
	
	public static final String CONFIG_FILENAME = "/config.properties";
	private static final int CHORD_NAME = 0;
	private static final int ROOT_NOTE = 1;
	private static final int BASS_NOTE = 2;
	private static final int SYMBOL = 3;
	
	public ChordManager() {
		this("chord_formulas.json");
	}
	
	public ChordManager(String resource) {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		resourceFile = resource;
		try {
			loadChordFormulas(resourceFile);
		} catch (IOException e) {
			log.error("Unable to load chord formulas " + e.toString());
		}
	}
	
	public static int computeFormulaNumber(ChordFormula chordFormula) {
		return chordFormula.computeFormulaNumber();
	}
	
	public static int computeFormulaNumber(List<Integer> formula) {
		int fnum = 0;
		List<Integer> ps = IFormula.formulaToPitchSet(formula);
		for(Integer i:ps) {
			int shiftamt = (i>=12) ? i-12 : i;
			fnum += (1<<shiftamt);
		}
		return fnum;
	}
	
	public static int computeSpellingNumber(ChordFormula chordFormula) {
		return chordFormula.computeSpellingNumber();
	}
		
	public Map<String, HarmonyChord> createHarmonyChords() {
		return createHarmonyChords(rootPitches, chordFormulasMap, Key.C_MAJOR);
	}
	
	public Map<String, HarmonyChord> createHarmonyChords(List<Pitch> rootPitches, Key key) {
		return createHarmonyChords(rootPitches, chordFormulasMap, key);
	}
	
	
	/**
	 * Creates a Map of HarmonyChord given a List of root pitches
	 * and a Map of ChordFormula.
	 * @param rootPitches List<Pitch>
	 * @param chordFormulas Map<String,ChordFormula>
	 * @return Map<String, HarmonyChord> where key is name of the HarmonyChord
	 */
	protected Map<String, HarmonyChord> createHarmonyChords(List<Pitch> rootPitches, Map<String,ChordFormula> chordFormulas, Key key) {
		Map<String, HarmonyChord> harmonyChords = new TreeMap<String, HarmonyChord>();

		for(ChordFormula formula: chordFormulas.values()) {
			log.debug("formula: " + formula.toJSON());
			for(Pitch root: rootPitches) {
				HarmonyChord harmonyChord = createHarmonyChord(formula, root, key);
				if(harmonyChord != null) {
					harmonyChords.put(harmonyChord.getName(), harmonyChord);
				}
			}
		}
		return harmonyChords;
	}

	/**
	 * In the case of slash chords, the chord name will have the / removed
	 * @param chordName for example, "D7-5/C"
	 * @return ChordInfo
	 */
	public static ChordInfo parseChordName(String chordName) {
		String[] result = new String[4];
		boolean isSlash = false;
		result[CHORD_NAME] = chordName;
		result[ROOT_NOTE] = chordName;
		result[BASS_NOTE] = chordName;
		result[SYMBOL] = chordName;
		if(!chordName.equals("0")) {
			int slash = chordName.indexOf("/");
			int len = chordName.length();
			if(slash > 0 ) {
				isSlash = true;
				result[BASS_NOTE] =  chordName.substring(slash + 1);
				result[CHORD_NAME] = chordName.substring(0, slash);
				len = result[CHORD_NAME].length();
			}
			char possibleAccidental = (len == 1) ? '0' : result[CHORD_NAME].charAt(1);
			if(len == 1 ) {		// "C"
				result[SYMBOL] = "M";	// no symbol is major
				if(isSlash) { result[ROOT_NOTE] = result[CHORD_NAME]; }
			}
			else if(len==2) {
				if(possibleAccidental=='b' || possibleAccidental=='#') {	// "F#" or "Eb"
					result[SYMBOL] = "M";
				}
				else {		// "Cm"
					result[SYMBOL] = "" + possibleAccidental;
					result[ROOT_NOTE] = "" + result[CHORD_NAME].charAt(0);
				}
			}
			else {	// Cm7, Ab7b13 etc.
				if(possibleAccidental=='b' || possibleAccidental=='#') {
					result[ROOT_NOTE] = result[CHORD_NAME].substring(0, 2);
					result[SYMBOL] = result[CHORD_NAME].substring(2);
				}
				else {
					result[ROOT_NOTE] = result[CHORD_NAME].substring(0, 1);
					result[SYMBOL] = result[CHORD_NAME].substring(1);
				}
			}
			if(!isSlash) { result[BASS_NOTE] = result[ROOT_NOTE]; }
		}
		return new ChordInfo(result[CHORD_NAME], result[ROOT_NOTE], result[BASS_NOTE], result[SYMBOL]);
	}
	

	/**
	 * Finds and returns the HarmonyChord given a chord name, for example
	 * "Bb"  "F#m7" etc. The formula is accessed by chord symbol. Uses Key.C_MAJOR to determine accidental preferences.
	 * @param chordName as it would appear in a Harmony.
	 * @return HarmonyChord or null if it can't find a match
	 */
	public HarmonyChord createHarmonyChord(String chordName) {
		return createHarmonyChord(chordName, Key.C_MAJOR);
	}
	
	/**
	 * Finds and returns the HarmonyChord given a chord name, for example
	 * "Bb"  "F#m7" etc. The formula is accessed by chord symbol.
	 * @param chordName as it would appear in a Harmony.
	 * @param key the Key to use when determining accidentals (# or b)
	 * @return HarmonyChord or null if it can't find a match
	 */
	public HarmonyChord createHarmonyChord(String chordName, Key key) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		ChordInfo chordInfo = parseChordName(chordName);
		
		if(chordFormulasMap.containsKey(chordInfo.getChordSymbol())) {
			formula = chordFormulasMap.get(chordInfo.getChordSymbol());
			hc = createHarmonyChord(formula, new Pitch(chordInfo.getRootNote()), key);
		}
		return hc;
	}
	
	public static HarmonyChord createHarmonyChord(String chordName, Map<String,ChordFormula> chordFormulas) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		ChordInfo chordInfo = parseChordName(chordName);
		Pitch root = new Pitch( chordInfo.getRootNote());
		if(chordFormulas.containsKey(chordInfo.getChordSymbol())) {
			formula = chordFormulas.get(chordInfo.getChordSymbol());
			hc = new HarmonyChord(formula, root);
		}
		return hc;
	}
	
	public HarmonyChord createHarmonyChord(ChordInfo chordInfo,  Key key) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		
		if(chordFormulasMap.containsKey(chordInfo.getChordSymbol())) {
			formula = chordFormulasMap.get(chordInfo.getChordSymbol());
			hc = createHarmonyChord(formula, new Pitch(chordInfo.getRootNote()), key);
		}
		return hc;
	}
	
	public HarmonyChord createHarmonyChord(ChordInfo chordInfo) {
		return createHarmonyChord(chordInfo, Key.C_MAJOR);
	}
	

	public HarmonyChord createHarmonyChord(ChordFormula formula, Pitch root) {
		return createHarmonyChord(formula, root, Key.C_MAJOR);
	}
	
	public HarmonyChord createHarmonyChord(ChordFormula formula, Pitch root, Key key) {
		HarmonyChord harmonyChord = new HarmonyChord(formula, root, key);
		return harmonyChord;
	}
	
	/**
	 * Outputs the HarmonyChords built on root "C" for all chord formulas
	 * in a tab-separated format for importing into Excel.
	 * 
	 * @param harmonyChords Map<String, HarmonyChord>
	 * @return String 
	 */
	public static String harmonyChordsToString(Map<String, HarmonyChord> harmonyChords) {
		StringBuffer sb = new StringBuffer("Num\tChord Name\tFormula Name\tSymbols\tFormula\tPitches\tChord Number\tChord Number (hex)\tSpellingNumber\n");
		int n = 1;
		String symbols = "";
		String formulaText = "";
		for(String key : harmonyChords.keySet()) {
			HarmonyChord harmonyChord = harmonyChords.get(key);
			String harmonyChordName = harmonyChord.getName();
			ChordFormula formula = harmonyChord.getChordFormula();
			try {
				formulaText = mapper.writeValueAsString(formula.getFormula());
				symbols = mapper.writeValueAsString(formula.getSymbols());
			} catch (JsonProcessingException e) {
				log.error("Cannot deserialize symbols because " + e.toString());
			}
			
			String displayText = n + "\t" + harmonyChordName + "\t\"" + formula.getName()
					+ "\"\t\"" +  formulaText
					+ "\"\t\"" +  symbols
					+ "\"\t" + harmonyChord.getChordPitches() 
					+ "\t" + computeFormulaNumber(formula.getFormula())
					+ "\t0x" + Integer.toHexString(computeFormulaNumber(formula.getFormula()))
					+ "\t0x" + Integer.toHexString(formula.getSpellingNumber())
					+ "\n";
			sb.append(displayText);
			n++;
		}
		return sb.toString();
	}
	
	/**
	 * Loads ChordFormulas from a JSON resource file. Recognizes lines that start with "//" or "/*" as comment lines
	 * Map keys are the name ("Major", "Diminished" etc)
	 * and the symbols "+(M7)", "M7+5", "M7#5" etc.
	 * 
	 * @param inputFile file path to load
	 * @throws IOException RuntimeException if file not found
	 */
	public void loadChordFormulas(String resourceFile) throws IOException {
		InputStream is = this.getClass().getResourceAsStream("/data/music/" + resourceFile);
    	Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
    	stream.filter(s -> !s.startsWith("/") && s.length() > 1).forEach(s -> accept(s));
    	stream.close();
		return;
	}

	/**
	 * Deserializes a JSON ChordFormula and adds to chordFormulasMap
	 */
	public void accept(String formulaString) {
		ChordFormula chordFormula = null;
		log.debug(formulaString);
		try {
			chordFormula = mapper.readValue(formulaString, ChordFormula.class);
		} catch (Exception e) {
			log.error("Cannot deserialize " + formulaString + "\nbecause " + e.toString());
		}
		chordFormulasMap.put(chordFormula.getName(), chordFormula);
	}


	/**
	 * Adds a ChordFormula to a given Map of ChordFormulas.
	 * The ChordFormula is added once for each key: name and symbols (because there are different ways to designate a given chord)
	 * For example, C7#9 is added 3 times with the keys "Seventh sharp ninth", "7+9", "7#9"
	 * @param cf ChordFormula to add
	 * @param chordFormulas Map<String,ChordFormula> target Map
	 */
	public static void addChordFormulaToMap(ChordFormula cf, Map<String,ChordFormula> chordFormulas) {
		chordFormulas.put(cf.getName(), cf);
		Set<String> keySet = cf.keySet();
		for(String key : keySet) {
			if(!chordFormulas.containsKey(key)) {
				chordFormulas.put(key, cf);
			}
		}
	}

	/**
	 * 
	 * @return Map<String,ChordFormula> could be empty map if not loaded!
	 */
	public Map<String,ChordFormula> getChordFormulas() {
		return chordFormulasMap;
	}

	public List<Pitch> getRootPitches() {
		return rootPitches;
	}

	public Map<String, ChordFormula> getChordFormulasMap() {
		return chordFormulasMap;
	}

	public String getResourceFile() {
		return resourceFile;
	}

	public String getJsonFormat() {
		return jsonFormat;
	}

	public void setJsonFormat(String jsonFormat) {
		this.jsonFormat = jsonFormat;
	}

	/**
	 * Test loading ChordFormulas.
	 * Usage: ChordManager -print -roots C4 -resource "chord_formulas.json"
	 * 		  ChordManager -print -resource "chord_formulas.json" -roots C4
	 * To display a tab-delimited table of harmony chords with corresponding chord formulas
	 * chord number (decimal and hex) and spelling number (hex).
	 * Suitable for import into Excel.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] roots = {"C"};
		String resourceFile = null;
		String jsonFormat = "JSON";	// the default, for Mathematica use "RawJSON"
		boolean printResults = false;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-resource")) {
				resourceFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-roots")) {
				roots = args[++i].split(",");
			}
			else if(args[i].startsWith("-format")) {
				jsonFormat = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-print")) {
				printResults = true;
			}
		}
		ChordManager chordManager = (resourceFile != null) ? new ChordManager(resourceFile) : new ChordManager();
		chordManager.setJsonFormat(jsonFormat);
		/*
		 * create a Pitch for each root specified on the command line
		 */
		Arrays.stream(roots).forEach(p -> chordManager.getRootPitches().add(new Pitch(p)) );

		/*
		 * Create Chords with the root(s) specified
		 */
		Map<String, HarmonyChord> harmonyChords = chordManager.createHarmonyChords();
			
		if(printResults) {
			PrintStream ps = System.out;
			String harmonyChordString = ChordManager.harmonyChordsToString(harmonyChords);
			ps.print(harmonyChordString);
		}
	}
}
