package util.music;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import music.element.Scales;

/**
 * Creates a JSON representation for scale formulas in all roots.
 * Results can be imported into MongoDB or Mathematica depending on specified format
 * 
 * @author don_bacon
 *
 */
public class ScaleExportManager  {
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleExportManager.class);

	private StringBuffer stringBuffer = null;
	ObjectMapper mapper = new ObjectMapper();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, ScaleFormula> scaleFormulas = new TreeMap<String, ScaleFormula>();
	private String resourceFile = null;
	private String jsonFormat = null;
	
	public ScaleExportManager(String resource, String format) {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		resourceFile = resource;
		jsonFormat = format;
		loadScaleFormulas();
	}
	
	public static void main(String... args)  {
		String root = "C";
		Pitch pitch = null;
		String resourceFile = null;
		String jsonFormat = "JSON";	// the default, for Mathematica use "RawJSON"
		int size = -1;
		PrintStream ps = System.out;
		boolean export = false;
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-root")) {
    				root = args[++i];
    				pitch = new Pitch(root);
    			}
    			else if(args[i].equalsIgnoreCase("-size")) {
    				size = Integer.parseInt(args[++i]);
    			}
    			else if(args[i].equalsIgnoreCase("-resource")) {
    				resourceFile = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-format")) {
    				jsonFormat = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-export")) {
    				export = true;
    			}
    		}
    	}
		ScaleExportManager scaleManager = new ScaleExportManager(resourceFile, jsonFormat);
		if(pitch != null) { scaleManager.addRoot(pitch); }
		if(export && scaleManager.getScaleFormulas().size() > 0) {
			String exportString = size>0 ? scaleManager.exportScaleFormulas(size) :scaleManager.exportScaleFormulas();
			ps.println(exportString);
		}
	}
	
	public void addRoot(Pitch p) {
		rootPitches.add(p);
	}
	
	public void loadScaleFormulas() {
		
		InputStream is = this.getClass().getResourceAsStream("/data/music/" + resourceFile);
		if(is != null) {
			try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {
				stream.forEach(s -> accept(s));
			}
		}
		else {
			log.error("Unable to open " + resourceFile);
		}
	}
	
	/**
	 * Exports all scale formulas in Mathematica RawJSON format.
	 * For example: [ {"name":"Lydian Pentachord","groups":["lydian"],"formula":[2,2,2,1,5],"size":5},
					  {"name":"Major Pentachord","groups":["major"],"formula":[2,2,1,2,5],"size":5}  ]
	 * In Mathematica: [ {"Lydian Pentachord":{"groups":[ "lydian" ],"formula":[ 2, 2, 2, 1, 5 ],"size":5} },
						 {"Major Pentachord":{"groups":[ "major" ],"formula":[ 2, 2, 1, 2, 5 ],"size":5} }  ]
	 * The association key is the scale name.
	 * Sends output to stdout.
	 */
	public String exportScaleFormulas() {
		stringBuffer = new StringBuffer("[\n");
		scaleFormulas.keySet().forEach(s ->  exportScaleFormula(scaleFormulas.get(s)));
		stringBuffer.deleteCharAt(stringBuffer.length()-2);		// drop the trailing comma
		stringBuffer.append("]");
		return(stringBuffer.toString());
	}
	
	/**
	 * Exports scale formulas of a given length (size)
	 * @param size
	 */
	public String exportScaleFormulas(int size) {
		stringBuffer = new StringBuffer("[\n");
		for(ScaleFormula sf : scaleFormulas.values()) {
			if(sf.getSize() == size) {
				exportScaleFormula(sf);
			}
		}
		stringBuffer.deleteCharAt(stringBuffer.length()-2);		// drop the trailing comma
		stringBuffer.append("]");
		return(stringBuffer.toString());
	}
	
	private void exportScaleFormula(ScaleFormula sf) {
		String names = null;
		String groups = null;
		String formulaArrayString = null;
		if(jsonFormat.equalsIgnoreCase("JSON")) {
			stringBuffer.append(sf.toJson() + ",\n");
		}
		else if(jsonFormat.equalsIgnoreCase("RawJSON")) {
			// sample: {"Minor Melodic":{ "alternateNames" : [ "Jazz Minor"], "groups":["minor"], "formula":[2,1,2,2,2,2,1],"size":7} }
			stringBuffer.append("{\"" + sf.getName() + "\":{");
			
			try {
				if(!sf.getAlternateNames().isEmpty()) {
					names = mapper.writeValueAsString(sf.getAlternateNames());
					stringBuffer.append("\"alternateNames\":");
					stringBuffer.append(names + ",");
				}
				if(!sf.getGroups().isEmpty()) {
					groups = mapper.writeValueAsString(sf.getGroups());
					stringBuffer.append("\"groups\":");
					stringBuffer.append(groups + ",");
				}
				formulaArrayString = mapper.writeValueAsString(sf.getFormula());
				stringBuffer.append("\"formula\":");
				stringBuffer.append(formulaArrayString + ",");
				
				stringBuffer.append("\"size\":");
				stringBuffer.append(sf.getSize() + "} ");
				
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException");
			}

			stringBuffer.append("},\n");
		}
	}
	
	public void exportScales(String root) {
		rootPitches.add(new Pitch(root));
		/*
		 * Create a scale with the root(s) specified and export
		 */
		for(ScaleFormula formula : scaleFormulas.values() ) {
			String mode = getMode(formula);
			ScaleType st = getScaleType(formula);
			log.debug("formula: " + formula.toJson());
			for(Pitch pitch : rootPitches) {
				String name = formula.getName();	// + "-" + pitch.toString();
				Scale scale = new Scale(name, mode, st, pitch, formula);
				System.out.println(scale.toJson());
			}
		}
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

	/**
	 * Deserializes a JSON ScaleFormula and adds to scaleFormulas Map
	 */
	public void accept(String formulaString) {
		ScaleFormula scaleFormula = null;
		log.debug(formulaString);
		try {
			scaleFormula = mapper.readValue(formulaString, ScaleFormula.class);
		} catch (Exception e) {
			log.error("Cannot deserialize " + formulaString + "\nbecause " + e.toString());
		}
		scaleFormulas.put(scaleFormula.getName(), scaleFormula);
		
	}
}
