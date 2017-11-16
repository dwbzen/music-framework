package music.element.song;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

import music.element.IFormula;
import music.element.Key;
import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import util.Configuration;
import util.mongo.Find;

/**
 * Creates a JSON representation for chord formulas in all roots.
 * Results can be imported into MongoDB, for example:
 * 
 * grep -v "//" chord_formulas.json>chords.json
 * mongoimport --type json --collection chords --db test --file chords.json
 * 
 * Also has static utility methods to create HarmonyChords, compute chord and spelling numbers,
 * parse chords from the String representation (for example, "D7-5/C").
 * 
 * @author don_bacon
 *
 */
public class ChordManager {

	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	//static Morphia morphia = new Morphia();
	static List<Pitch> rootPitches = new ArrayList<Pitch>();
	static TreeMap<String,ChordFormula> chordFormulasMap = new TreeMap<String, ChordFormula>();
	static boolean printResults = false;
	static Datastore datastore = null;
	
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String defaultHost = "localhost";
	public static final int defaultPort = 27017;
	private static final int CHORD_NAME = 0;
	private static final int ROOT_NOTE = 1;
	private static final int BASS_NOTE = 2;
	private static final int SYMBOL = 3;
	
	public ChordManager() {
		
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
		
	public static Map<String, HarmonyChord> createHarmonyChords(List<Pitch> rootPitches, Map<String,ChordFormula> chordFormulas) {
		return createHarmonyChords(rootPitches, chordFormulas, Key.C_MAJOR);
	}
	
	/**
	 * Creates a Map of HarmonyChord given a List of root pitches
	 * and a Map of ChordFormula.
	 * @param rootPitches List<Pitch>
	 * @param chordFormulas Map<String,ChordFormula>
	 * @return Map<String, HarmonyChord> where key is name of the HarmonyChord
	 */
	public static Map<String, HarmonyChord> createHarmonyChords(List<Pitch> rootPitches, Map<String,ChordFormula> chordFormulas, Key key) {
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
	 * Finds and returns the HarmonyChord given a chord name, for example
	 * "Bb"  "F#m7" etc. The formula is accessed by chord symbol. Uses Key.C_MAJOR to determine accidental preferences.
	 * @param chordName as it would appear in a Harmony.
	 * @param formulas Map<String,ChordFormula>
	 * @return HarmonyChord or null if it can't find a match
	 */
	public static HarmonyChord createHarmonyChord(String chordName, Map<String,ChordFormula> formulas) {
		return createHarmonyChord(chordName, formulas, Key.C_MAJOR);
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
	 * "Bb"  "F#m7" etc. The formula is accessed by chord symbol.
	 * @param chordName as it would appear in a Harmony.
	 * @param formulas Map<String,ChordFormula>
	 * @param key the Key to use when determining accidentals (# or b)
	 * @return HarmonyChord or null if it can't find a match
	 */
	public static HarmonyChord createHarmonyChord(String chordName, Map<String,ChordFormula> formulas, Key key) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		ChordInfo chordInfo = parseChordName(chordName);
		
		if(formulas.containsKey(chordInfo.getChordSymbol())) {
			formula = formulas.get(chordInfo.getChordSymbol());
			hc = createHarmonyChord(formula, new Pitch(chordInfo.getRootNote()), key);
		}
		return hc;
	}
	
	public static HarmonyChord createHarmonyChord(ChordInfo chordInfo, Map<String,ChordFormula> formulas, Key key) {
		HarmonyChord hc = null;
		ChordFormula formula = null;
		
		if(formulas.containsKey(chordInfo.getChordSymbol())) {
			formula = formulas.get(chordInfo.getChordSymbol());
			hc = createHarmonyChord(formula, new Pitch(chordInfo.getRootNote()), key);
		}
		return hc;
	}
	
	public static HarmonyChord createHarmonyChord(ChordInfo chordInfo, Map<String,ChordFormula> formulas) {
		return createHarmonyChord(chordInfo, formulas, Key.C_MAJOR);
	}
	

	public static HarmonyChord createHarmonyChord(ChordFormula formula, Pitch root) {
		return createHarmonyChord(formula, root, Key.C_MAJOR);
	}
	
	public static HarmonyChord createHarmonyChord(ChordFormula formula, Pitch root, Key key) {
		HarmonyChord harmonyChord = new HarmonyChord(formula, root, key);
		return harmonyChord;
	}
	
	/**
	 * Loads ChordFormulas from a .JSON file. Recognizes lines that start with "//" or "/*" as comment lines
	 * Map keys are the name ("Major", "Diminished" etc)
	 * and the symbols "+(M7)", "M7+5", "M7#5" etc.
	 * NOTE - not recommended approach. Use MongoDB collection.
	 * @param inputFile file path to load
	 * @throws IOException RuntimeException if file not found
	 */
	public static Map<String,ChordFormula> loadChordFormulas(String inputFile) throws IOException {
		Morphia morphia = Find.morphia;
		morphia.map(ChordFormula.class);

		BufferedReader inputFileReader;
		try {
			inputFileReader = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + inputFile);
			throw new RuntimeException("File not found: " + inputFile);
		}
		String line;
		while((line = inputFileReader.readLine()) != null) {
			String jsonline = line.trim();
			if(jsonline.startsWith("//") || jsonline.startsWith("/*")) {
				continue;
			}
			Object dbObject = JSON.parse(jsonline);
			if(dbObject != null) {
				log.debug("parsed " + dbObject.toString());
				ChordFormula cf = morphia.fromDBObject(datastore, ChordFormula.class, (BasicDBObject)dbObject);
				if(cf != null) {
					addChordFormulaToMap(cf, chordFormulasMap);
				}
			}
			else {
				log.debug("null object returned " + jsonline);
				break;
			}
		}
		inputFileReader.close();
		return chordFormulasMap;
	}

	/**
	 * Loads chord formulas from configured MongoDB
	 * Map keys are the name ("Major", "Diminished" etc)
	 * and the symbols "+(M7)", "M7+5", "M7#5" etc.
	 */
	public static Map<String, ChordFormula> loadChordFormulas(String dbname, String collectionName) {
		Find.morphia.map(ChordFormula.class);
		Morphia morphia = Find.morphia;		
		Find find = new Find(dbname, collectionName, defaultHost, defaultPort);
		datastore = find.getDatastore();
		MongoCursor<Document> cursor = find.search();
	
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);
			ChordFormula cf = morphia.fromDBObject(datastore, ChordFormula.class, dbObject);
			String name = cf.getName();
			List<Integer>formula = cf.getFormula();
			StringBuffer formString = new StringBuffer();
			for(int n:formula) {
				formString.append(" " + n);
			}
			log.debug(name + ", symbols: " + cf.getSymbols() + ", formula: [" + formString.toString() + "]");
			addChordFormulaToMap(cf, chordFormulasMap);
			log.debug(cf.toJSON());
		}
		find.close();
		
		return chordFormulasMap;
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
	public static Map<String,ChordFormula> getChordFormulas() {
		return chordFormulasMap;
	}
	
	/**
	 * Test loading ChordFormulas.
	 * Usage: ChordManager -load  -print -roots C4 -file "C:\\data\\music\\chord_formulas.json"
	 * 		  ChordManager -load -print -collection chord_formulas -roots C4
	 * To display a tab-delimited table of harmony chords with corresponding chord formulas
	 * chord number (decimal and hex) and spelling number (hex).
	 * Suitable for import into Excel.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String[] roots = {"C"};
		String inputFile = null;		// complete path
		Map<String,ChordFormula> chordFormulas = null;
		boolean loadChordFormulas = true;
		String collectionName = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-roots")) {
				roots = args[++i].split(",");
			}
			else if(args[i].equalsIgnoreCase("-load")) {
				loadChordFormulas = true;
			}
			else if(args[i].startsWith("-collect")) {
				collectionName = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-print")) {
				printResults = true;
			}
		}
		Morphia morphia = Find.morphia;
		morphia.map(ScaleFormula.class);
		morphia.map(Scale.class);
		morphia.map(ChordFormula.class);
		morphia.map(HarmonyChord.class);
		morphia.map(Pitch.class);

		if(loadChordFormulas) {
			if(inputFile == null) {
				Configuration configuration =  Configuration.getInstance(CONFIG_FILENAME);
				Properties configProperties = configuration.getProperties();
				String dbname = configProperties.getProperty("mongodb.db.name");
				chordFormulas = loadChordFormulas(dbname, collectionName);
				// NOTE this should work now
				// Map<String, IMapped<String>> cmap = JSONUtil.loadJSONCollection("chord_formulas", ChordFormula.class);
			}
			else {
				chordFormulas = loadChordFormulas(inputFile);
			}
		}
		/*
		 * create a Pitch for each root specified on the command line
		 */
		for(int i=0; i<roots.length; i++) {
			Pitch p = new Pitch(roots[i]);
			rootPitches.add(p);
		}
			/*
			 * Create Chords with the root(s) specified
			 */
			Map<String, HarmonyChord> harmonyChords = createHarmonyChords(rootPitches, chordFormulas, Key.C_MAJOR);
			int n = 1;
			if(printResults) {
				System.out.println("Num\tChord Name\tFormula Name\tSymbols\tPitches\tChord Number\tChord Number (hex)\tSpellingNumber");
				for(String key : harmonyChords.keySet()) {
					HarmonyChord harmonyChord = harmonyChords.get(key);
					String harmonyChordName = harmonyChord.getName();
					ChordFormula formula = harmonyChord.getChordFormula();
					List<String> symbols = formula.getSymbols();
					for(String symbol : symbols ) {
						String displayText = n + "\t" + harmonyChordName + "\t\"" + formula.getName() + "\"\t\"" +  symbol
								+ "\"\t" + harmonyChord.getChordPitches() 
								+ "\t" + computeFormulaNumber(formula.getFormula())
								+ "\t0x" + Integer.toHexString(computeFormulaNumber(formula.getFormula()))
								+ "\t0x" + Integer.toHexString(formula.getSpellingNumber());
						System.out.println(displayText);
						n++;
					}
				}
			}
		}
}
