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

import org.dwbzen.music.element.IFormula;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.song.ChordFormula;
import org.dwbzen.music.element.song.ChordFormulas;
import org.dwbzen.music.element.song.ChordInfo;
import org.dwbzen.music.element.song.HarmonyChord;

/**
 * Creates a JSON representation for chord formulas in all roots.</p>
 * 
 * Also has static utility methods to create HarmonyChords, compute chord and spelling numbers,<br>
 * parse chords from the String representation (for example, "D7-5/C").<br>
 * chordFormulasMap maps chordFormula by both name and all the symbols.<br>
 * 
 * Load and/or export chord formulas or chords (HarmonyChords instanced from root list)
 * <code>
 * Usage: ChordManager -export formulas -format RawJSON
 * 		  ChordManager -export chords -root C,D
 * 		  ChordManager -print -root C -resource my_formulas.json
 * 		  ChordManager -print -group "altered" -root C4
 * </code>
 * 
 * Use -print To display a tab-delimited table of harmony chords with corresponding chord formula(s),<br>
 * chord number (decimal and hex) and spelling number (hex). Suitable for import into Excel.</p>
 * Use RawJSON format for Mathematica import.<br>
 * If unspecified, resource file defaults to "allChordFormulas.json"<br>
 * Specify -roots all for all 24 root notes.<br>
 * All roots: { "Ab", "A", "A#", "Bb", "B", "B#", "Cb", "C", "C#", "Db", "D", "D#", "Eb", "E", "E#", "Fb", "F", "F#", "Gb", "G", "G#" }<br>
 * If unspecified, roots defaults to { "C" }<br>
 * 
 * @author don_bacon
 *
 */
public class ChordManager {

	static final org.apache.log4j.Logger log = Logger.getLogger(ChordManager.class);
	static ObjectMapper mapper = new ObjectMapper();
	static List<Pitch> allRootPitches = new ArrayList<Pitch>();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, HarmonyChord> harmonyChords = null;
	private Map<Pitch, Map<String, HarmonyChord>> harmonyChordsRootMap = new TreeMap<Pitch, Map<String, HarmonyChord>>(new PitchComparator());
	private Map<String,ChordFormula> chordFormulasMap = new TreeMap<String, ChordFormula>();	// by name and symbol(s)
	private Map<String,ChordFormula> chordFormulasByNameMap = new TreeMap<String, ChordFormula>();	// by name only
	private String resourceFile = null;
	private String jsonFormat = null;
	private StringBuffer stringBuffer = null;
	private List<String> groupsFilter = new ArrayList<String>();
	
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
		createHarmonyChords(rootPitches, chordFormulasByNameMap);
		return harmonyChords;
	}
	
	public Map<String, HarmonyChord> createHarmonyChords(List<Pitch> rootPitches) {
		createHarmonyChords(rootPitches, chordFormulasByNameMap);
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
	protected void createHarmonyChords(List<Pitch> rootPitches, Map<String,ChordFormula> chordFormulas) {
		for(Pitch root : rootPitches) {
			harmonyChords = new TreeMap<String, HarmonyChord>();
			for(ChordFormula formula: chordFormulas.values()) {
				log.debug("formula: " + formula.toJson());
				if(filter(groupsFilter, formula.getGroups())) {
					Key key = Key.rootKeyMap.get(root.toString(-1));
					HarmonyChord harmonyChord = new HarmonyChord(formula, root, key);
					if(harmonyChord != null) {
						 harmonyChords.put(root.getStep().name() + formula.getSymbols().get(0), harmonyChord);
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
		stringBuffer = new StringBuffer("[\n");
		if(groupsFilter.size() > 0) {
			chordFormulasMap.keySet().stream().filter(s -> groupsFilter.contains(s)).forEach(s ->  exportChordFormula(chordFormulasMap.get(s)));
		}
		else {
			chordFormulasMap.keySet().forEach(s ->  exportChordFormula(chordFormulasMap.get(s)));
		}
		stringBuffer.deleteCharAt(stringBuffer.length()-2);		// drop the trailing comma
		stringBuffer.append("]");
		return stringBuffer.toString();
	}
	
	/**
	 * @param chordFormula
	 * @return
	 */
	public void exportChordFormula(ChordFormula chordFormula) {
		/*
		 * write the formula as a JSON string according to the specified format
		 */

		if(jsonFormat.equalsIgnoreCase("RawJSON")) {
			stringBuffer.append(chordFormula.toJson() + ",\n");
		}
		else if(jsonFormat.equalsIgnoreCase("JSON")) {
			String symbols = null;
			String groups = null;
			String formulaArrayString = null;
			String intervalsArrayString = null;
			stringBuffer.append("{\"name\":" + chordFormula.getName() + "\"");
			
			try {
				if(!chordFormula.getSymbols().isEmpty()) {
					symbols = mapper.writeValueAsString(chordFormula.getSymbols());
					stringBuffer.append("\"symbols\":");
					stringBuffer.append(symbols + ",");
				}
				if(!chordFormula.getGroups().isEmpty()) {
					groups = mapper.writeValueAsString(chordFormula.getGroups());
					stringBuffer.append("\"groups\":");
					stringBuffer.append(groups + ",");
				}
				formulaArrayString = mapper.writeValueAsString(chordFormula.getFormula());
				stringBuffer.append("\"formula\":");
				stringBuffer.append(formulaArrayString + ",");
				
				intervalsArrayString = mapper.writeValueAsString(chordFormula.getIntervals());
				stringBuffer.append("\"intervals\":");
				stringBuffer.append(intervalsArrayString + ",");
				
				stringBuffer.append("\"size\":");
				stringBuffer.append(chordFormula.getSize() + ",");
				stringBuffer.append("\"chordSize\":");
				stringBuffer.append(chordFormula.getChordSize());
				
				stringBuffer.append("\"formulaNumber\":");
				stringBuffer.append(chordFormula.getFormulaNumber() + ",");
				
				stringBuffer.deleteCharAt(stringBuffer.length()-1);
				stringBuffer.append("\"");
			} catch(JsonProcessingException e) {
				log.error("JsonProcessingException");
			}
			stringBuffer.append("},\n");
		}
		return;
	}

	public String exportChords() {
		stringBuffer = new StringBuffer("{\"chordLibrary\":[");
		for(Pitch root : harmonyChordsRootMap.keySet()) {
			log.debug("WORKING on root: " + root.toString());
			stringBuffer.append("\n{ \"root\":\"" + root.toString(-1) + "\",");
			stringBuffer.append(" \"chords\":[\n" );
			
			Map<String, HarmonyChord> hcmap = harmonyChordsRootMap.get(root);
			for(String name : hcmap.keySet()) {
				HarmonyChord hc = hcmap.get(name);
				exportChord(hc);
			}
			stringBuffer.deleteCharAt(stringBuffer.length()-2);		// drop the trailing comma
			stringBuffer.append("  ]\n},");
		}
		stringBuffer.deleteCharAt(stringBuffer.length()-1);		// drop the trailing comma
		stringBuffer.append("\n]}\n");
		return stringBuffer.toString();
	}
	
	private void exportChord(HarmonyChord harmonyChord) {
		/*
		 * write the chord as a JSON string according to the specified format
		 */

		if(jsonFormat.equalsIgnoreCase("RawJSON")) {
			stringBuffer.append(harmonyChord.toJson(HarmonyChord.OutputFormat.FULL) + ",\n");
		}
		else if(jsonFormat.equalsIgnoreCase("JSON")) {
			String symbols = null;
			String groups = null;
			String formulaArrayString = null;
			String intervalsArrayString = null;
			ChordFormula chordFormula = harmonyChord.getChordFormula();
			stringBuffer.append("  {\"name\":\"" + harmonyChord.getName() + "\", ");
			try {
				if(!chordFormula.getSymbols().isEmpty()) {
					symbols = mapper.writeValueAsString(chordFormula.getSymbols());
					stringBuffer.append("\"symbols\":");
					stringBuffer.append(symbols + ", ");
				}
				if(!chordFormula.getGroups().isEmpty()) {
					groups = mapper.writeValueAsString(chordFormula.getGroups());
					stringBuffer.append("\"groups\":");
					stringBuffer.append(groups + ", ");
				}
				formulaArrayString = mapper.writeValueAsString(chordFormula.getFormula());
				stringBuffer.append("\"formula\":");
				stringBuffer.append(formulaArrayString + ", ");
				
				intervalsArrayString = mapper.writeValueAsString(chordFormula.getIntervals());
				stringBuffer.append("\"intervals\":");
				stringBuffer.append(intervalsArrayString + ", ");
				
				stringBuffer.append("\"spelling\":[ ");
				harmonyChord.getChordPitches().forEach(p -> stringBuffer.append("\"" + p.toString(-1) + "\", "));
				stringBuffer.deleteCharAt(stringBuffer.length()-2);		// drop the trailing comma
				stringBuffer.append("]");
				
			} catch(JsonProcessingException e) {
				log.error("JsonProcessingException");
			}
			stringBuffer.append("},\n");
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
    			chordFormulasByNameMap.put(chordFormula.getName(), chordFormula);
    			for(String s : chordFormula.getSymbols()) {
    				chordFormulasMap.put(s, chordFormula);
    			}
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

	public Map<String, HarmonyChord> getHarmonyChords() {
		return harmonyChords;
	}

	public List<String> getGroupsFilter() {
		return groupsFilter;
	}

	public Map<Pitch, Map<String, HarmonyChord>> getHarmonyChordsRootMap() {
		return harmonyChordsRootMap;
	}

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] roots = {"C"};
		String resourceFile = "allChordFormulas.json";
		String jsonFormat = "JSON";	// the default, for Mathematica use "RawJSON"
		boolean printResults = false;
		PrintStream ps = System.out;
		String exportClass = null;		// chords or formulas
		String[] groups = null;			// optional chord formula group(s) to export
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-resource")) {
				resourceFile = args[++i];
			}
			else if(args[i].startsWith("-root")) {
				roots = args[++i].split(",");
			}
			else if(args[i].startsWith("-format")) {
				jsonFormat = args[++i];
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
		}
		ChordManager chordManager = (resourceFile != null) ? new ChordManager(resourceFile) : new ChordManager();
		chordManager.setJsonFormat(jsonFormat);
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