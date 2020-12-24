package org.dwbzen.util.music;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dwbzen.music.element.Chord;
import org.dwbzen.music.element.IFormula;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.song.ChordFormula;
import org.dwbzen.music.element.song.ChordFormulas;
import org.dwbzen.music.element.song.ChordInfo;
import org.dwbzen.music.element.song.HarmonyChord;

/**
 * Creates a JSON representation for chord formulas in all roots.</p>
 * 
 * Also has static utility methods to create HarmonyChords, compute chord number and spelling number,<br>
 * parse chords from the String representation (for example, "D7-5/C").<br>
 * chordFormulasMap maps chordFormula by both name and all the symbols.<br>
 * 
 * Load and/or export chord formulas or chords (HarmonyChords instanced from root list)<br>
 * Usage examples:
 * <p>
 * <code>
 * 	ChordManager -export formulas -format mathJSON<br>
 *  ChirdManager -export formulas -format json -pretty true<br>
 *  ChordManager -export chords -root C,D<br>
 * 	ChordManager -print -root C -resource my_formulas.json<br>
 * 	ChordManager -print -group "altered" -root C4<br>
 * </code></p>
 * <p>
 * Use -print To display a tab-delimited table of harmony chords with corresponding chord formula(s),<br>
 * chord number (decimal and hex) and spelling number (hex). Suitable for import into Excel.</p>
 * Use mathJSON format for Mathematica import.<br>
 * If unspecified, resource file defaults to "allChordFormulas.json"<br>
 * Specify "-roots all" to generate chords for all 24 root notes.<br>
 * All roots: { "Ab", "A", "A#", "Bb", "B", "B#", "Cb", "C", "C#", "Db", "D", "D#", "Eb", "E", "E#", "Fb", "F", "F#", "Gb", "G", "G#" }<br>
 * If unspecified, roots defaults to { "C" }
 * <p>Valid -format values are "JSON", "mathJSON",  "rawJSON" and "csv" (not case sensitive)<br>
 * JSON is the default for export. Results can be imported into MongoDB.<br>
 * Use mathJSON for importing the result into Mathematica.<br>
 * rawJSON deserializes ChordFormulas directly using the "pretty" option specified which if true indents the output for readability.<br>
 * Use csv to open with Excel.
 * </p>
 * 
 * @author don_bacon
 * @see org.dwbzen.common.util.IJson
 *
 */
public class ChordManager {

	static final org.apache.log4j.Logger log = Logger.getLogger(ChordManager.class);
	static ObjectMapper mapper = new ObjectMapper();
	static List<Pitch> allRootPitches = new ArrayList<Pitch>();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, HarmonyChord> harmonyChords = null;
	private Map<Pitch, Map<String, HarmonyChord>> harmonyChordsRootMap = new TreeMap<Pitch, Map<String, HarmonyChord>>(new PitchComparator());
	private Map<String,ChordFormula> chordFormulasMap = new TreeMap<>();
	private Map<Integer, ChordFormula> chordFormulaNumberMap = new TreeMap<>();		// chord formulas indexed by formulaNumber
	private String resourceFile = null;
	private String outputFormat = null;
	private StringBuilder stringBuilder = null;
	private List<String> groupsFilter = new ArrayList<String>();
	/**
	 * Used for exporting chords. If true, exports unique chords by Name.
	 * Otherwise exports a chord for each symbol. Default is true.
	 */
	private boolean exportUnique = true;
	private boolean jsonPretty = false;			// JSON "pretty" format (indented)
	
	private ChordFormulas chordFormulas = null;
	
	public static final String CONFIG_FILENAME = "/config.properties";
	static String TAB = "\t";
	public static boolean showAllSymbols = false;
	public static boolean showHexFormulaNumbers = true;
	
	static {
		String[] roots = { "Ab", "A", "A#", "Bb", "B", "B#", "Cb", "C", "C#", "Db", "D", "D#", "Eb", "E", "E#", "Fb", "F", "F#", "Gb", "G", "G#" };
		Arrays.asList(roots).forEach(r -> allRootPitches.add(new Pitch(r)));
	}
	
	public ChordManager() {
		this("allChordFormulas.json");
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
		List<Integer> ps = IFormula.formulaToPitchIndexes(formula);
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
		createHarmonyChords(rootPitches, chordFormulasMap);
		return harmonyChords;
	}
	
	public Map<String, HarmonyChord> createHarmonyChords(List<Pitch> rootPitches) {
		createHarmonyChords(rootPitches, chordFormulasMap);
		return harmonyChords;
	}
	
	
	/**
	 * Creates a Map<Pitch, Map<String, HarmonyChord>> given a List of root pitches and a Map of ChordFormula.
	 * ChordFormulas are filtered by groupsFilter List.
	 * 
	 * @param rootPitches List<Pitch>
	 * @param chordFormulas Map<String,ChordFormula>
	 * @return Map<Pitch, Map<String, HarmonyChord>> keyed by rootPitches
	 */
	public void createHarmonyChords(List<Pitch> rootPitches, Map<String,ChordFormula> chordFormulas) {
		for(Pitch root : rootPitches) {
			harmonyChords = new TreeMap<String, HarmonyChord>();
			for(ChordFormula formula: chordFormulas.values()) {
				log.debug("formula: " + formula.toJson());
				if(filter(groupsFilter, formula.getGroups())) {
					Key key = Key.rootKeyMap.get(root.toString(-1));
					HarmonyChord harmonyChord = new HarmonyChord(formula, root, key);
					if(harmonyChord != null) {
						if(exportUnique) {
							harmonyChords.put(root.getStep().name() + formula.getSymbols().get(0), harmonyChord);
						}
						else {
							formula.getSymbols().forEach(symbol -> harmonyChords.put(root.getStep().name() + symbol, harmonyChord));
						}
						 
					}
				}
			}
			harmonyChordsRootMap.put(root, harmonyChords);
		}
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
		ChordInfo chordInfo = ChordInfo.parseChordName(chordName);
		
		if(chordFormulasMap.containsKey(chordInfo.getChordSymbol())) {
			formula = chordFormulasMap.get(chordInfo.getChordSymbol());
			hc = new HarmonyChord(formula, new Pitch(chordInfo.getRootNote()), key);
		}
		return hc;
	}
	
	public static HarmonyChord createHarmonyChord(String chordName, Map<String,ChordFormula> chordFormulaMap) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		ChordInfo chordInfo =  ChordInfo.parseChordName(chordName);
		Pitch root = new Pitch( chordInfo.getRootNote());
		if(chordFormulaMap.containsKey(chordInfo.getChordSymbol())) {
			formula = chordFormulaMap.get(chordInfo.getChordSymbol());
			hc = new HarmonyChord(formula, root);
		}
		return hc;
	}
	
	public HarmonyChord createHarmonyChord(ChordInfo chordInfo,  Key key) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		
		if(chordFormulasMap.containsKey(chordInfo.getChordSymbol())) {
			formula = chordFormulasMap.get(chordInfo.getChordSymbol());
			hc =  new HarmonyChord(formula, new Pitch(chordInfo.getRootNote()), key);
		}
		return hc;
	}
	
	public HarmonyChord createHarmonyChord(ChordInfo chordInfo) {
		return createHarmonyChord(chordInfo, Key.C_MAJOR);
	}
	
	/**
	 * Outputs the HarmonyChords built on root "C" for all chord formulas
	 * in a tab-separated format for importing into Excel.<br>
	 * Outputting symbols is suppressed unless the showAllSymbols is true.<br>
	 * Result can be pasted into an Excel spreadsheet.
	 * 
	 * @param harmonyChords Map<String, HarmonyChord>
	 * @return String 
	 */
	public static String harmonyChordsToString( Map<Pitch, Map<String, HarmonyChord>> rootMap) {
		StringBuilder sb = new StringBuilder("Num\tChord Name\tFormula Name\tFormula\tSymbol(s)\tPitches\tGroups\tChord Number\tSpellingNumber");
		if(showHexFormulaNumbers) {
			sb.append("\tChord Number(hex)\tSpellingNumber(hex)");
		}
		sb.append("\n");
		int n = 1;
		String symbols = "";
		String formulaText = "";
		String groups = null;
		for(Pitch p : rootMap.keySet()) {
			Map<String, HarmonyChord> harmonyChords = rootMap.get(p);
			for(String key : harmonyChords.keySet()) {
				HarmonyChord harmonyChord = harmonyChords.get(key);
				String harmonyChordName = harmonyChord.getName();
				ChordFormula formula = harmonyChord.getChordFormula();
				String rootString = harmonyChord.getRoot().toString(-1);
				try {
					formulaText = mapper.writeValueAsString(formula.getFormula());
					symbols = mapper.writeValueAsString(formula.getSymbols());
					groups = mapper.writeValueAsString(formula.getGroups());
				} catch (JsonProcessingException e) {
					log.error("Cannot deserialize symbols because " + e.toString());
				}
				String symbol = harmonyChordName.substring(harmonyChordName.indexOf(rootString) + rootString.length() );
				sb.append(n + "\t" + harmonyChordName + "\t" + formula.getName());
				sb.append(TAB +  formulaText);
				sb.append(TAB + (showAllSymbols ?  symbols : symbol));
				sb.append(TAB + harmonyChord.getChordPitches());
				sb.append(TAB + groups);
				sb.append(TAB + computeFormulaNumber(formula.getFormula()));
				sb.append(TAB + formula.getSpellingNumber());
				if(showHexFormulaNumbers) {
					 sb.append(TAB + "0x" + Integer.toHexString(computeFormulaNumber(formula.getFormula())));
					 sb.append(TAB + "0x" + Integer.toHexString(formula.getSpellingNumber()));
				}
				sb.append("\n");
				n++;
			}
		}
		return sb.toString();
	}
	
	public String exportChordFormulas() {
		stringBuilder = new StringBuilder();
		if(outputFormat.equalsIgnoreCase("rawjson")) {
			stringBuilder.append(this.chordFormulas.toJson(jsonPretty));
		}
		else if(outputFormat.equalsIgnoreCase("csv")) {
			stringBuilder.append("name,symbols,groups,formula,intervals,size,chord size,formula number,spelling\n");	// heading row
			if(groupsFilter.size() > 0) {
				chordFormulasMap.keySet().stream().filter(s -> groupsFilter.contains(s)).forEach(s ->  exportChordFormula(chordFormulasMap.get(s)));
			}
			else {
				for(ChordFormula chordFormula : chordFormulas.getChordFormulas()) {
					exportChordFormula(chordFormula);
				}
			}
		}
		else {
			stringBuilder = stringBuilder.append("[\n");
			if(groupsFilter.size() > 0) {
				chordFormulasMap.keySet().stream().filter(s -> groupsFilter.contains(s)).forEach(s ->  exportChordFormula(chordFormulasMap.get(s)));
			}
			else {
				for(ChordFormula chordFormula : chordFormulas.getChordFormulas()) {
					exportChordFormula(chordFormula);
				}
			}
			stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
			stringBuilder.append("]");
		}

		return stringBuilder.toString();
	}
	
	/**
	 * @param chordFormula
	 * @return
	 */
	public void exportChordFormula(ChordFormula chordFormula) {
		/*
		 * write the formula as a JSON string according to the specified format
		 */

		if(outputFormat.equalsIgnoreCase("mathJSON")) {
			stringBuilder.append(chordFormula.toJson(jsonPretty) + ",\n");
		}
		else {
			try {
				String symbols = mapper.writeValueAsString(chordFormula.getSymbols());
				String groups = mapper.writeValueAsString(chordFormula.getGroups());
				String formulaArrayString = mapper.writeValueAsString(chordFormula.getFormula());
				String intervalsArrayString = mapper.writeValueAsString(chordFormula.getIntervals());

				if(outputFormat.equalsIgnoreCase("json")) {
					stringBuilder.append("{\"name\":" + chordFormula.getName() + "\"");			
					if(!chordFormula.getSymbols().isEmpty()) {				
						stringBuilder.append("\"symbols\":");
						stringBuilder.append(symbols + ",");
					}
					if(!chordFormula.getGroups().isEmpty()) {
						stringBuilder.append("\"groups\":");
						stringBuilder.append(groups + ",");
					}
					stringBuilder.append("\"formula\":");
					stringBuilder.append(formulaArrayString + ",");
					
					stringBuilder.append("\"intervals\":");
					stringBuilder.append(intervalsArrayString + ",");
					
					stringBuilder.append("\"size\":");
					stringBuilder.append(chordFormula.getSize() + ",");
					stringBuilder.append("\"chordSize\":");
					stringBuilder.append(chordFormula.getChordSize());
					
					stringBuilder.append("\"formulaNumber\":");
					stringBuilder.append(chordFormula.getFormulaNumber() + ",");
					
					stringBuilder.deleteCharAt(stringBuilder.length()-1);
					stringBuilder.append("\"");
					
					stringBuilder.append("},\n");
				}
				else if(outputFormat.equalsIgnoreCase("csv")) {
					/*
					 * For csv format each single quote needs to be a double quote
					 * and if there are embedded commas, the whole string needs to be quoted
					 * For example, [4,3,10] ==> "[4,3,10]"
					 * ["M3", "m3", "M9"]  ==> "[""M3"", ""m3"", ""M9""]"
					 * ["maj9","M9"]  ==> "[""maj9"",""M9""]"
					 * To simplify, this strips all the quotes and brackets and enclose in quotes
					 * so ["+9","aug9"]  ==>  "+9, aug9"
					 * TODO finish me
					 */
					stringBuilder.append(chordFormula.getName() + ",");
					stringBuilder.append(stripChars(symbols) + ",");
					stringBuilder.append(stripChars(groups) + ",");
					stringBuilder.append(stripChars(formulaArrayString) + ",");
					stringBuilder.append(stripChars(intervalsArrayString) + ",");
					stringBuilder.append(chordFormula.getSize() + ",");
					stringBuilder.append(chordFormula.getChordSize() + ",");
					stringBuilder.append(chordFormula.getFormulaNumber() + ",");
					stringBuilder.append(chordFormula.getSpelling()); 
					stringBuilder.append("\n");
				}
			}
			catch(JsonProcessingException e) {
				System.err.println("JsonProcessingException: " + e.toString());
			}
		}
		return;
	}
	
	private String stripChars(String s) {
		String s2 = s.replaceAll("\"", "").replaceAll(",", ", ");	// replaceAll("\\[", "").replaceAll("\\]", "")
		return "\"" + s2 + "\"";
	}
	
	public String exportChords() {
		stringBuilder = new StringBuilder("{\"chordLibrary\":[");
		for(Pitch root : harmonyChordsRootMap.keySet()) {
			log.debug("WORKING on root: " + root.toString());
			stringBuilder.append("\n{ \"root\":\"" + root.toString(-1) + "\",");
			stringBuilder.append(" \"chords\":[\n" );
			
			Map<String, HarmonyChord> hcmap = harmonyChordsRootMap.get(root);
			for(String name : hcmap.keySet()) {
				HarmonyChord hc = hcmap.get(name);
				exportChord(hc);
			}
			stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
			stringBuilder.append("  ]\n},");
		}
		stringBuilder.deleteCharAt(stringBuilder.length()-1);		// drop the trailing comma
		stringBuilder.append("\n]}\n");
		return stringBuilder.toString();
	}
	
	private void exportChord(HarmonyChord harmonyChord) {
		/*
		 * write the chord as a JSON string according to the specified format
		 */

		if(outputFormat.equalsIgnoreCase("mathJSON")) {
			stringBuilder.append(harmonyChord.toJson(HarmonyChord.OutputFormat.FULL) + ",\n");
		}
		else if(outputFormat.equalsIgnoreCase("JSON")) {
			String symbols = null;
			String groups = null;
			String formulaArrayString = null;
			String intervalsArrayString = null;
			ChordFormula chordFormula = harmonyChord.getChordFormula();
			stringBuilder.append("  {\"name\":\"" + harmonyChord.getName() + "\", ");
			try {
				if(!chordFormula.getSymbols().isEmpty()) {
					symbols = mapper.writeValueAsString(chordFormula.getSymbols());
					stringBuilder.append("\"symbols\":");
					stringBuilder.append(symbols + ", ");
				}
				if(!chordFormula.getGroups().isEmpty()) {
					groups = mapper.writeValueAsString(chordFormula.getGroups());
					stringBuilder.append("\"groups\":");
					stringBuilder.append(groups + ", ");
				}
				formulaArrayString = mapper.writeValueAsString(chordFormula.getFormula());
				stringBuilder.append("\"formula\":");
				stringBuilder.append(formulaArrayString + ", ");
				
				intervalsArrayString = mapper.writeValueAsString(chordFormula.getIntervals());
				stringBuilder.append("\"intervals\":");
				stringBuilder.append(intervalsArrayString + ", ");
				
				stringBuilder.append("\"spelling\":[ ");
				harmonyChord.getChordPitches().forEach(p -> stringBuilder.append("\"" + p.toString(-1) + "\", "));
				stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
				stringBuilder.append("]");
				
			} catch(JsonProcessingException e) {
				log.error("JsonProcessingException");
			}
			stringBuilder.append("},\n");
		}
		return;
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
		StringBuffer sb = new StringBuffer();
		InputStream is = this.getClass().getResourceAsStream("/data/music/" + resourceFile);
    	Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
    	stream.filter(s -> !s.startsWith("/") && s.length() > 1).forEach(s -> sb.append(s));
    	stream.close();
    	try {
    		chordFormulas = mapper.readValue(sb.toString(), ChordFormulas.class);
    	} catch (Exception e) {
    		log.error("Cannot deserialize ChordFormulas because " + e.toString());
    	}
    	if(chordFormulas != null) {
    		for(ChordFormula chordFormula : chordFormulas.getChordFormulas()) {
    			chordFormulasMap.put(chordFormula.getName(), chordFormula);
    			for(String s : chordFormula.getSymbols()) {
    				chordFormulasMap.put(s, chordFormula);
    			}
    			chordFormulaNumberMap.put(chordFormula.getFormulaNumber(), chordFormula);
    		}
    	}
		return;
	}
	
	private boolean filter(List<String> includeList, List<String> myList) {
		boolean include = true;
		if(includeList != null && includeList.size()>0) {
			include = includeList.containsAll(myList);
		}
		return include;
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
	public ChordFormulas getChordFormulas() {
		return chordFormulas;
	}

	public List<Pitch> getRootPitches() {
		return rootPitches;
	}

	/**
	 * ChordFormulasMap is keyed by chord name AND symbol(s).<br>
	 * For example,"Augmented seventh sharp nine" has 4 entries as there are<br>
	 * 3 symbols:"+7#9", "aug7#9" and "7+#9"
	 * @return Map<String, ChordFormula> 
	 */
	public Map<String, ChordFormula> getChordFormulasMap() {
		return chordFormulasMap;
	}

	public Map<Integer, ChordFormula> getChordFormulaNumberMap() {
		return chordFormulaNumberMap;
	}

	public String getResourceFile() {
		return resourceFile;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public Map<String, HarmonyChord> getHarmonyChords() {
		return harmonyChords;
	}

	public List<String> getGroupsFilter() {
		return groupsFilter;
	}

	public Map<Pitch, Map<String, HarmonyChord>> getHarmonyChordsRootMap() {
		return harmonyChordsRootMap;
	}

	public boolean isExportUnique() {
		return exportUnique;
	}

	public void setExportUnique(boolean exportUnique) {
		this.exportUnique = exportUnique;
	}
	
	public boolean isJsonPretty() {
		return jsonPretty;
	}

	public void setJsonPretty(boolean jsonPretty) {
		this.jsonPretty = jsonPretty;
	}

	/**
	 * Finds the ChordFormula for the specified Chord and if it exists, adds it to the Chord.<br>
	 * Unlike a HarmonyChord, which is derived from a ChordFormula,<br>
	 * a Chord is an arbitrary set of Notes (a Measureable) that can be added<br>
	 * to a Measure and may or may not have a chord formula.
	 * 
	 * @param chord
	 * @return the associated ChordFormula or null if none exists.
	 */
	public ChordFormula addChordFormulaToChord(Chord chord) {
		ChordFormula chordFormula = null;
		int size = chord.size();
		int[] intervals = new int[size-1];
		List<Note> chordNotes = chord.getChordNotes();
		/*
		 * find the intervals to determine the formula.
		 */
		Note prevNote = chord.getRoot();
		int index = 0;
		for(int i = 0; i< size; i++) {
			Note note = chordNotes.get(i);
			int interval = prevNote.absoluteDifference(note);
			if(interval > 0) {
				intervals[index++] = interval;
			}
			prevNote = note;
		}
		/*
		 * Search chordFormulasMap for those intervals
		 */
		Integer formulaNumber = 0;
		if(intervals.length > 0) {
			formulaNumber = ChordFormula.computeFormulaNumber(intervals);
			if(chordFormulaNumberMap.containsKey(formulaNumber)) {
				chordFormula = chordFormulaNumberMap.get(formulaNumber);
				chord.setChordFormula(chordFormula);
			}
			else {
				/*
				 * may be an inversion or slash chord
				 */
				for(String symbol:chordFormulasMap.keySet()) {
					ChordFormula cf = chordFormulasMap.get(symbol);
					List<Integer> inversionFormulaNumbers = cf.getInversionFormulaNumbers();
					for(int i = 0; i < inversionFormulaNumbers.size(); i++) {
						if(formulaNumber.equals(inversionFormulaNumbers.get(i))) {
							chordFormula = (ChordFormula)cf.clone();
							if(chordFormula != null) {
								chordFormula.setSlash(i);
								Note bassNote = chord.getRoot();
								Pitch bassPitch = bassNote.getPitch();
								chord.setBassPitch(bassPitch);
								List<Integer> inv = cf.getInversions().get(i);		// the inversion that matched
								int step = inv.get(0);
								Pitch rootPitch = bassPitch.increment(step, bassPitch.getAlteration());
								Note rootNote = new Note(rootPitch, bassNote.getDuration());
								chord.setRoot(rootNote);
								break;
							}
						}
					}
					if(chordFormula != null) {
						chord.setChordFormula(chordFormula);
						break;
					}
				}
			}
		}
		
		return chordFormula;
	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] roots = {"C"};
		String resourceFile = "allChordFormulas.json";
		String outputFormat = "JSON";	// the default, for Mathematica use "mathJSON", "rawJSON" serializes ChordFormulas.
		boolean printResults = false;
		PrintStream ps = System.out;
		String exportClass = null;		// chords or formulas
		String[] groups = null;			// optional chord formula group(s) to export
		boolean uniq = true;
		boolean pretty = false;
		String[] chordNotes = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-resource")) {
				resourceFile = args[++i];
			}
			else if(args[i].startsWith("-root")) {
				roots = args[++i].split(",");
			}
			else if(args[i].startsWith("-format")) {
				outputFormat = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-print")) {
				printResults = true;
			}
			else if(args[i].startsWith("-group")) {
				groups =  args[++i].split(",");
			}
			else if(args[i].equalsIgnoreCase("-export")) {
				exportClass = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-unique")) {
				uniq = args[++i].equalsIgnoreCase("true");
			}
			else if(args[i].equalsIgnoreCase("-pretty")) {
				pretty = args[++i].equalsIgnoreCase("true");
			}
			else if(args[i].equalsIgnoreCase("-find")) {	// find the chord symbol corresponding to given list of pitches
				/*
				 * -find "B4,Db5,F5,Ab5" for example
				 */
				chordNotes = args[++i].split(",");
			}
		}
		ChordManager chordManager = (resourceFile != null) ? new ChordManager(resourceFile) : new ChordManager();
		if(chordNotes != null) {
			Chord chord = Chord.createChord(chordNotes, 60);
			ChordFormula cf = chordManager.addChordFormulaToChord(chord);
			if(cf != null) {
				String symbol = chord.toString(true);
				System.out.println(symbol);
			}
			else {
				System.out.println("Symbol not found");
			}
			return;
		}
		chordManager.setOutputFormat(outputFormat);
		chordManager.setExportUnique(uniq);
		chordManager.setJsonPretty(pretty);
		List<String> groupList = (groups != null) ? Arrays.asList(groups) : new ArrayList<String>();
		chordManager.getGroupsFilter().addAll(groupList);
		/*
		 * create a Pitch for each root specified on the command line
		 */
		if(roots != null && roots.length >0 && roots[0].equalsIgnoreCase("all")) {
			chordManager.getRootPitches().addAll(allRootPitches);
		}
		else {
			Arrays.stream(roots).forEach(p -> chordManager.getRootPitches().add(new Pitch(p)) );
		}

		/*
		 * Create HarmonyChords with the root(s) specified
		 */
		chordManager.createHarmonyChords();
		if(exportClass != null) {
			String exportString = null;
			if(exportClass.equalsIgnoreCase("formulas")) {
				exportString = chordManager.exportChordFormulas();
			}
			if(exportClass.equalsIgnoreCase("chords")) {
				exportString = chordManager.exportChords();
			}
			ps.print(exportString);
		}
		
		if(printResults) {
			String harmonyChordString = ChordManager.harmonyChordsToString(chordManager.getHarmonyChordsRootMap());
			ps.print(harmonyChordString);
		}
	}
}

class PitchComparator implements Comparator<Pitch>{

	@Override
	public int compare(Pitch o1, Pitch o2) {
		return o1.toString(-1).compareTo(o2.toString(-1));
	}
}

class PitchStringComparator implements Comparator<String>{

	@Override
	public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	}
}