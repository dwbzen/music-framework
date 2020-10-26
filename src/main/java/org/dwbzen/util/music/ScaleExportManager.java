package org.dwbzen.util.music;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.ScaleType;
import org.dwbzen.music.element.Scales;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.musicxml.MusicXMLHelper;
import org.dwbzen.util.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Creates representation for scale formulas in the desired root(s).
 * Output/input formats are specified using the -format command line option.
 * <dl>
 * <dt>json</dt> <dd>standard json format, the default output format</dd>
 * <dt>rawjson</dt> <dd>RawJSON format</dd>
 * <dt>musicxml</dt> <dd>a musicXML score that can be openned with notation software.</dd>
 * </dl>
 * JSON outputs the scale name as the "name" attribute. For example:<br>
     [ {"name":"Lydian Pentachord","groups":["lydian"],"formula":[2,2,2,1,5],"size":5},<br>
	   {"name":"Major Pentachord","groups":["major"],"formula":[2,2,1,2,5],"size":5}  ]</p>
	   
 * RawJSON treats the scale name as the association key. For example:<br>
	 [ {"Lydian Pentachord":{"groups":[ "lydian" ],"formula":[ 2, 2, 2, 1, 5 ],"size":5} },<br>
	   {"Major Pentachord":{"groups":[ "major" ],"formula":[ 2, 2, 1, 2, 5 ],"size":5} }  ]</p>

 * Export line options are:
 * <dl>
 * <dt>-export true|false</dt> <dd>export scale formulas in the desired format. Default is false.</dd>
 * <dt>-scales true|false</dt> <dd>export the scales in the desired format. Default is false, set to true if a root note is specified</dd>
 * <dt>-root note</dt> <dd>Use the specified note as the root note for scales. Valid values are A through G, pound sign for sharps, lower base 'b' for flats. </dd>
 * <dt>-resource filename</dt>  <dd>Use the specified JSON file instead of the default "common_scaleFormulas.json"</dd>
 * <dt>-collectionName name</dt> <dd>Use 'name' as the collection name. Applies to RawJSON output format only.</dd>
 * <dt>-inputFormat format</dt>  <dd>JSON input format can be json, rawjson, or musicxml</dd>
 * <dt>-file file_name</dt> <dd>Output to file: &lt;file_name>+yyyy-MM-dd.&lt;extension> where extension is ".json" or ".musicxml" </dd>
 * <dt>-name scale_name</dt>  <dd>export only the named scale (case insensitive)</dd>
 * </dl>
 * When using -resource, specify the filename only, as in "myResourceFile.json" Files are assumed to be in /data/music project folder.</p>
 * JSON out put can be imported into MongoDB or Mathematica depending on specified format</p>
 * 
 * The musicXML for each exported scale consists of a 2-octave range ascending and descending<br>
 * starting with the specified root note(s).</p>
 * 
 * Scales can be searched using command line options:
 * <dl>
 * <dt>-size n</dt>  <dd>exports scales/formulas having n-notes</dd>
 * <dt>-group name</dt>  <dd>exports only scales/formulas from the specified group(s)</dd>
 * </dl>
 * NOTES<br>
 * (1) the scale output includes only "name" and "notes", for example:  [ {"name":"Hirajoshi Japan","notes":[ "C", "D", "Eb", "G", "Ab", "C" ] } ]<br>
 * (2)-musicxml implementation is incomplete.
 * </p>
 * @author don_bacon
 *
 */
public class ScaleExportManager  {
	static final Logger log = LogManager.getLogger(ScaleExportManager.class);
	
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String ORCHESTRA_CONFIG_FILENAME="/orchestra.properties";
	
	private Configuration configuration = null;
	
	private BiFunction<List<ScaleFormula>, List<Pitch>, Score> scaleCreator = null;

	private StringBuilder stringBuilder = null;
	ObjectMapper mapper = new ObjectMapper();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, ScaleFormula> scaleFormulas = new TreeMap<>();
	private Map<String,  List<ScaleFormula>> scaleFormulasGroupMap = new TreeMap<>();
	private String resourceFile = null;
	private String group = null;
	private String outputFormat = null;
	private String importFormat = "JSON";	// resource file JSON format
	private String collectionName = null;
	private String scaleName = null;	// regex scale name matching
	private int size = 0;
	private String outputFileName = null;
	
	public ScaleExportManager(String resource, String format, String group, int size) {
		configure();
		resourceFile = resource;
		outputFormat = format;
		this.size = size;
		this.group = group;
		loadScaleFormulas();
	}
	
	private void configure() {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configuration.addConfiguration(Configuration.getInstance(ORCHESTRA_CONFIG_FILENAME));
	}
	
	public static void main(String... args)  {
		String root = null;
		Pitch pitch = null;
		String resourceFile = "common_scaleFormulas.json";
		String outputFormat = "JSON";
		String importFormat = null;
		String group = null;
		String collectionName = null;
		String scaleName = null;
		int size = 0;
		PrintStream ps = System.out;
		boolean exportFormulas = false;
		boolean scales = false;
		String fileName = null;
		boolean createXml = false;
		boolean showStats = false;
		boolean listFormulas = false;
		
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-root")) {
    				root = args[++i];
    				pitch = new Pitch(root);
    				scales = true;
    			}
    			else if(args[i].equalsIgnoreCase("-size")) {
    				size = Integer.parseInt(args[++i]);
    			}
    			else if(args[i].startsWith("-group")) {
    				group = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-resource")) {
    				resourceFile = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-format")) {
    				outputFormat = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-importformat")) {
    				importFormat = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-formulas")) {
    				exportFormulas = true;
    			}
    			else if(args[i].equalsIgnoreCase("-collection")) {
    				collectionName = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-name")) {
    				scaleName = args[++i].toLowerCase();
    			}
    			else if(args[i].equalsIgnoreCase("-stats")) {
    				showStats = Boolean.valueOf(args[++i]);
    			}
    			else if(args[i].equalsIgnoreCase("-list")) {
    				listFormulas = Boolean.valueOf(args[++i]);
    			}    			
    			else if(args[i].equalsIgnoreCase("-file")) {
    				// base filename including the full path - extension added later depending on output format
    				// for example, "/Users/DWBZe/Documents/Music/Scores/musicXML/Scales"
    				String date = dateFormat.format(new Date());
    				fileName = args[++i] + date;
    			}
    		}
    	}
    	
		ScaleExportManager scaleExportManager = new ScaleExportManager(resourceFile, outputFormat, group, size);
		if(showStats) {
			String s = scaleExportManager.getStats(listFormulas);
			System.out.println(s);
		}
		
		boolean isMusicXML = outputFormat.equalsIgnoreCase("musicxml");
		if(importFormat != null) {
			scaleExportManager.setImportFormat(importFormat);
		}
		if(scaleName != null) {
			scaleExportManager.setScaleName(scaleName);
		}
		if(collectionName != null) {
			scaleExportManager.setCollectionName(collectionName);
		}
		if(fileName != null) {
			String extension = isMusicXML ? ".musicxml" : ".json";
			scaleExportManager.setOutputFileName(fileName + extension);
		}
		if(scales) {
			scaleExportManager.addRoot(pitch);
		}
		
		if(isMusicXML) {
			Score score = scaleExportManager.exportScalesScore();
			String outputFile = scaleExportManager.getOutputFileName();
			log.info("*** Score created ***");
			createXML(outputFile, score, scaleExportManager.getConfiguration());
			log.info("*** musicXML written to " + outputFile + " ***");
		}
		else {
			String exportFormulasString = null;
			String exportScalesString = null;
			if(exportFormulas) {
				exportFormulasString = scaleExportManager.exportScaleFormulas();
			}
			if(scales) {
				exportScalesString = scaleExportManager.exportScales();
			}
			if(exportFormulasString != null) {
				ps.println(exportFormulasString);
			}
			if(exportScalesString != null) {
				ps.println(exportScalesString);
			}
			if(fileName != null) {
				// write the export string(s) to designated file
				// TODO
			}
		}
	}

	public void addRoot(Pitch p) {
		rootPitches.add(p);
	}
	
	public void loadScaleFormulas() {
		
		if(importFormat.equalsIgnoreCase("json")) {
			InputStream is = this.getClass().getResourceAsStream("/data/music/" + resourceFile);
			if(is != null) {
				try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
					stream.forEach(s -> accept(s));
				}
			}
		}
		else if(importFormat.equalsIgnoreCase("rawjson")) {
			// TODO
		}
		else {
			log.error("Unable to open " + resourceFile);
		}
	}
	
	/**
	 * Exports all scale formulas in the specified JSON format.<br>
	 * Mathematica can import either JSON or RawJSON. It really depends on what the Mathematica function/program expects.<br>
	 * RawJSON may work best because the key IS the scale name instead of an attribute value.<br>
	 * 
	 * Sends output to stdout.
	 */
	public String exportScaleFormulas() {
		stringBuilder = new StringBuilder("[\n");
		Set<String> kset = scaleFormulas.keySet();
			kset.stream()
			.filter(s -> size == 0 || scaleFormulas.get(s).getSize() == size)
			.filter(s -> group == null || scaleFormulas.get(s).getGroups().contains(group))
			.filter(s -> scaleName == null || scaleFormulas.get(s).getName().toLowerCase().equals(scaleName))
			.forEach(s ->  exportScaleFormula(scaleFormulas.get(s)));
		stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
		stringBuilder.append("]");
		return(stringBuilder.toString());
	}
	

	private void exportScaleFormula(ScaleFormula sf) {
		String names = null;
		String groups = null;
		String formulaArrayString = null;
		if(outputFormat.equalsIgnoreCase("JSON")) {
			stringBuilder.append(sf.toJson() + ",\n");
		}
		else if(outputFormat.equalsIgnoreCase("RawJSON")) {
			// sample: {"Minor Melodic":{ "alternateNames" : [ "Jazz Minor"], "groups":["minor"], "formula":[2,1,2,2,2,2,1],"size":7} }
			stringBuilder.append("{\"" + sf.getName() + "\":{");
			
			try {
				if(!sf.getAlternateNames().isEmpty()) {
					names = mapper.writeValueAsString(sf.getAlternateNames());
					stringBuilder.append("\"alternateNames\":");
					stringBuilder.append(names + ",");
				}
				if(!sf.getGroups().isEmpty()) {
					groups = mapper.writeValueAsString(sf.getGroups());
					stringBuilder.append("\"groups\":");
					stringBuilder.append(groups + ",");
				}
				formulaArrayString = mapper.writeValueAsString(sf.getFormula());
				stringBuilder.append("\"formula\":");
				stringBuilder.append(formulaArrayString + ",");
				
				stringBuilder.append("\"size\":");
				stringBuilder.append(sf.getSize() );
				
				if(sf.getDescription() != null) {
					stringBuilder.append(", \"description\":");
					stringBuilder.append("\"" + sf.getDescription() + "\"} ");
				}
				else {
					stringBuilder.append(" } ");
				}
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException");
			}
			stringBuilder.append("},\n");
		}
	}
	
	/**
	 * Finds ScaleFormulas matching criteria for size, group and/or name.
	 * @return a List<ScaleFormula> sorted by formula name.
	 */
	public List<ScaleFormula> findScaleFormulas() {
		List<ScaleFormula> formulas = new ArrayList<ScaleFormula>();
		scaleFormulas.keySet()
		.stream()
		.filter(s -> size == 0 || scaleFormulas.get(s).getSize() == size)
		.filter(s -> group == null || scaleFormulas.get(s).getGroups().contains(group))
		.filter(s -> scaleName == null || scaleFormulas.get(s).getName().toLowerCase().equals(scaleName))
		.sorted()
		.forEach(s ->  formulas.add(scaleFormulas.get(s)));
		return formulas;
	}
	
	public String exportScales() {
		stringBuilder = new StringBuilder("[\n");
		scaleFormulas.keySet()
			.stream()
			.filter(s -> size == 0 || scaleFormulas.get(s).getSize() == size)
			.filter(s -> group == null || scaleFormulas.get(s).getGroups().contains(group))
			.filter(s -> scaleName == null || scaleFormulas.get(s).getName().toLowerCase().equals(scaleName))
			.forEach(s ->  exportScales(scaleFormulas.get(s)));
		stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
		stringBuilder.append("]");
		return(stringBuilder.toString());
	}
	
	
	private String exportScales(ScaleFormula formula) {
		String mode = getMode(formula);
		ScaleType st = getScaleType(formula);
		for(Pitch pitch : rootPitches) {
			String name = formula.getName();
			Scale scale = new Scale(name, mode, st, pitch, formula);
			if(outputFormat.equalsIgnoreCase("RawJSON")) {
				stringBuilder.append("\"" + name + "\":\"notes\":[ ");
			}
			else {
				stringBuilder.append("{\"name\":\"" + name + "\",\"notes\":[ ");
			}
			stringBuilder.append(scale.toString(true));
			stringBuilder.append(" ] },\n");
		}
		return stringBuilder.toString();
	}

	/**
	 * Creates a Score consisting of the Scales specified in the search criteria.<br>
	 * Converts that to musicXML format which can be saved and imported into a notation package such as MuseScore.<br>
	 * If no roots have been specified, let's assume 'C'.<br>
	 * Creates a title for the score: "Scales yyyyMMdd".
	 * 
	 * @return musicXML String.
	 */
	public Score exportScalesScore() {
		List<ScaleFormula> scaleFormulas =  findScaleFormulas();
		if(rootPitches.isEmpty()) {
			rootPitches.add(Pitch.C);
		}
		String scoreTitle = "Scales_"  + dateFormat.format(new Date());
		scaleCreator = new ScoreScaleCreator(scoreTitle);
		Score theScore = scaleCreator.apply(scaleFormulas, rootPitches);
		return theScore;
	}
	
	public static String getMode(ScaleFormula formula) {
		String mode = null;
		String name = formula.getName().toLowerCase();
		if(name.equalsIgnoreCase("major")) {
			mode = Scales.MAJOR;
		}
		else if(name.indexOf("minor") >= 0) {
			mode = Scales.MINOR;
		}
		else if(name.indexOf("mode") >= 0) {
			mode = Scales.MODE;
		}
		return mode;
	}

	public static ScaleType getScaleType(ScaleFormula formula) {
		ScaleType st = null;
		int n = formula.getFormula().size();
		switch(n) {
			case 1: st = ScaleType.MONOTONIC;
					break;
			case 2: st = ScaleType.DITONIC;
					break;
			case 3: st=ScaleType.TRITONIC;
					break;
			case 4: st = ScaleType.TETRATONIC;
					break;
			case 5: st = ScaleType.PENTATONIC;
					break;
			case 6: st = ScaleType.HEXATONIC;
					break;
			case 7: st = ScaleType.DIATONIC;
					break;
			case 8: st = ScaleType.OCTATONIC;
					break;
			case 9: st = ScaleType.NONATONIC;
					break;
			default: st = ScaleType.CHROMATIC;
		}
		return st;
	}

	public Map<String, ScaleFormula> getScaleFormulas() {
		return scaleFormulas;
	}

	public void setScaleFormulas(Map<String, ScaleFormula> scaleFormulas) {
		this.scaleFormulas = scaleFormulas;
	}

	public List<Pitch> getRootPitches() {
		return rootPitches;
	}

	public String getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	public String getImportFormat() {
		return importFormat;
	}

	public void setImportFormat(String importFormat) {
		this.importFormat = importFormat;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getScaleName() {
		return scaleName;
	}

	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}

	public String getResourceFile() {
		return resourceFile;
	}

	public int getSize() {
		return size;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public Configuration getConfiguration() {
		return configuration;
	}


	public Map<String, List<ScaleFormula>> getScaleFormulasGroupMap() {
		return scaleFormulasGroupMap;
	}

	/**
	 * Gets group stats - number of scales in each group.<br>
	 * If listFormulas is true, also lists the scale names in the group.
	 * 
	 * @param listFormulas
	 * @return
	 */
	public String getStats(boolean listFormulas) {
		StringBuilder sb = new StringBuilder();
		for(String groupName : scaleFormulasGroupMap.keySet()) {
			sb.append(groupName);
			List<ScaleFormula> flist = scaleFormulasGroupMap.get(groupName);
			sb.append("\t" + flist.size() + "\n");
			if(listFormulas) {
				for(ScaleFormula sf : flist) {
					sb.append("\t\t" + sf.getName());
					String pitches = sf.createPitches(Pitch.C).toString();
					sb.append("\t" +sf.getFormula().toString() );
					sb.append("\t" + pitches);
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Deserializes a JSON ScaleFormula and adds to scaleFormulas Map
	 */
	public void accept(String formulaString) {
		ScaleFormula scaleFormula = null;
		log.debug(formulaString);
		try {
			scaleFormula = ScaleFormula.deserialize(formulaString);
		} catch (Exception e) {
			log.error("Cannot deserialize " + formulaString + "\nbecause " + e.toString());
		}
		if(scaleFormula != null) {	
			scaleFormulas.put(scaleFormula.getName(), scaleFormula);
			for(String group : scaleFormula.getGroups()) {
				if(scaleFormulasGroupMap.containsKey(group)) {
					scaleFormulasGroupMap.get(group).add(scaleFormula);
				}
				else {
					List<ScaleFormula> glist = new ArrayList<>();
					glist.add(scaleFormula);
					scaleFormulasGroupMap.put(group, glist);
				}
			}
		}
	}
	
	public static void createXML(String filename, Score score,  Configuration config) {
		MusicXMLHelper helper = new MusicXMLHelper(score, config.getProperties());
		helper.setSuppressTempoMarking(true);
		helper.convert();	// creates and returns a com.audiveris.proxymusic.ScorePartwise
		PrintStream ps = System.out;
		if(filename != null) {
			try {
				ps = new PrintStream(new FileOutputStream(filename));
			}
			catch(FileNotFoundException e) {
				log.warn(filename + " not available. Writing to System.out");
			}
		}
		helper.marshall(ps);	// marshals the ScorePartwise instance to an XML file
		if(filename != null) {
			ps.close();
		}
	}
}
