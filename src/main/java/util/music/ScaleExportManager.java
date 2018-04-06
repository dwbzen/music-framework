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
 * Results can be imported into MongoDB or Mathematica depending on specified format:</p>
 * JSON for importing into MongoDB (the default), 
 * RawJSON for importing into Mathematica
 * 
 * @author don_bacon
 *
 */
public class ScaleExportManager  {
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleExportManager.class);

	private StringBuilder stringBuilder = null;
	ObjectMapper mapper = new ObjectMapper();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, ScaleFormula> scaleFormulas = new TreeMap<String, ScaleFormula>();
	private String resourceFile = null;
	private String group = null;
	private String jsonFormat = null;
	private int size = 0;
	
	public ScaleExportManager(String resource, String format, String group, int size) {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		resourceFile = resource;
		jsonFormat = format;
		this.size = size;
		this.group = group;
		loadScaleFormulas();
	}
	
	public static void main(String... args)  {
		String root = null;
		Pitch pitch = null;
		String resourceFile = null;
		String jsonFormat = "JSON";	// the default, for Mathematica use "RawJSON"
		String group = null;
		int size = 0;
		PrintStream ps = System.out;
		boolean export = false;
		boolean scales = false;
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
    			else if(args[i].equalsIgnoreCase("-group")) {
    				group = args[++i];
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
		ScaleExportManager scaleExportManager = new ScaleExportManager(resourceFile, jsonFormat, group, size);

		if(export) {
			String exportString = scaleExportManager.exportScaleFormulas();
			ps.println(exportString);
		}
		if(scales) {
			scaleExportManager.addRoot(pitch);
			String scalesString = scaleExportManager.exportScales();
			ps.println(scalesString);
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
		stringBuilder = new StringBuilder("[\n");
		scaleFormulas.keySet()
			.stream()
			.filter(s -> size == 0 || scaleFormulas.get(s).getSize() == size)
			.filter(s -> group == null || scaleFormulas.get(s).getGroups().contains(group))
			.forEach(s ->  exportScaleFormula(scaleFormulas.get(s)));
		stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
		stringBuilder.append("]");
		return(stringBuilder.toString());
	}
	
	private void exportScaleFormula(ScaleFormula sf) {
		String names = null;
		String groups = null;
		String formulaArrayString = null;
		if(jsonFormat.equalsIgnoreCase("JSON")) {
			stringBuilder.append(sf.toJson() + ",\n");
		}
		else if(jsonFormat.equalsIgnoreCase("RawJSON")) {
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
				stringBuilder.append(sf.getSize() + "} ");
				
			} catch (JsonProcessingException e) {
				log.error("JsonProcessingException");
			}

			stringBuilder.append("},\n");
		}
	}
	public String exportScales() {
		stringBuilder = new StringBuilder("[\n");
		scaleFormulas.keySet()
			.stream()
			.filter(s -> size == 0 || scaleFormulas.get(s).getSize() == size)
			.filter(s -> group == null || scaleFormulas.get(s).getGroups().contains(group))
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
			if(jsonFormat.equalsIgnoreCase("RawJSON")) {
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
		if(scaleFormula != null) {	scaleFormulas.put(scaleFormula.getName(), scaleFormula); }
		
	}
}
