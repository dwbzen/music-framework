package org.dwbzen.util.music;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.ScaleType;
import org.dwbzen.music.element.Scales;

/**
 * Creates representation for scale formulas in the desired root(s).
 * Output formats are specified using the -format command line option.
 * <dl>
 * <dt>json</dt> <dd>standard json format</dd>
 * <dt>rawjson</dt> <dd>Mathematica json format</dd>
 * <dt>musicxml</dt> <dd>a musicXML score that can be openned with notation software.</dd>
 * </dl>
 * Export line options are:
 * <dl>
 * <dt>-export true|false</dt> <dd>export scale formulas in the desired format. Default is false.</dd>
 * <dt>-scales true|false</dt> <dd>export the scales in the desired format. Default is false</dd>
 * <dt>-root note</dt> <dd>Use the specified note as the root note for scales. Valid values are A through G, pound sign for sharps, lower base 'b' for flats. </dd>
 * <dt>-resource filename</dt>  <dd>Use the specified JSON file instead of the default "common_scaleFormulas.json"</dd>
 * </dl>
 * When using -resource, specify the filename only, as in "myResourceFile.json" Files are assumed to be in /data/music project folder.</p>
 * JSON out put can be imported into MongoDB or Mathematica depending on specified format</p>
 * The musicXML for each exported scale consists of a 2-octave range ascending and descending<br>
 * starting with the specified root note(s).</p>
 * Scales can be searched using command line options:
 * <dl>
 * <dt>-size n</dt>  <dd>exports scales/formulas having n-notes</dd>
 * <dt>-group name</dt>  <dd>exports only scales/formulas from the specified group(s)</dd>
 * </dl>
 * NOTES<br>
 * (1) the scale output includes only "name" and "notes", for example:  [ {"name":"Hirajoshi Japan","notes":[ "C", "D", "Eb", "G", "Ab", "C" ] } ]<br>
 * (2)-musicxm implementation is incomplete.
 * </p>
 * @author don_bacon
 *
 */
public class ScaleExportManager  {
	static final Logger log = LogManager.getLogger(ScaleExportManager.class);

	private StringBuilder stringBuilder = null;
	ObjectMapper mapper = new ObjectMapper();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, ScaleFormula> scaleFormulas = new TreeMap<String, ScaleFormula>();
	private String resourceFile = null;
	private String group = null;
	private String outputFormat = null;
	private int size = 0;
	
	public ScaleExportManager(String resource, String format, String group, int size) {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		resourceFile = resource;
		outputFormat = format;
		this.size = size;
		this.group = group;
		loadScaleFormulas();
	}
	
	public static void main(String... args)  {
		String root = null;
		Pitch pitch = null;
		String resourceFile = "common_scaleFormulas.json";
		String outputFormat = "JSON";	// the default, for Mathematica use "RawJSON"
		String group = null;
		int size = 0;
		PrintStream ps = System.out;
		boolean export = false;
		boolean scales = false;
		boolean musicXML = false;
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
    				outputFormat = args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-export")) {
    				export = true;
    			}
    		}
    	}
		ScaleExportManager scaleExportManager = new ScaleExportManager(resourceFile, outputFormat, group, size);
		musicXML = outputFormat.equalsIgnoreCase("musicxml");
		
		if(export) {
			String exportString = scaleExportManager.exportScaleFormulas();
			ps.println(exportString);
		}
		if(scales) {
			scaleExportManager.addRoot(pitch);
			String scalesString = scaleExportManager.exportScales();
			ps.println(scalesString);
		}
		if(musicXML) {
			throw new RuntimeException("musicXML not yet implemented");
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
	 * For example:<br> [ {"name":"Lydian Pentachord","groups":["lydian"],"formula":[2,2,2,1,5],"size":5},<br>
					  {"name":"Major Pentachord","groups":["major"],"formula":[2,2,1,2,5],"size":5}  ]<br><br>
	 * In Mathematica:<br> [ {"Lydian Pentachord":{"groups":[ "lydian" ],"formula":[ 2, 2, 2, 1, 5 ],"size":5} },<br>
						 {"Major Pentachord":{"groups":[ "major" ],"formula":[ 2, 2, 1, 2, 5 ],"size":5} }  ]<br>
	 * The association key is the scale name.
	 * Sends output to stdout.
	 */
	public String exportScaleFormulas() {
		stringBuilder = new StringBuilder("[\n");
		Set<String> kset = scaleFormulas.keySet();
			kset.stream()
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
		if(scaleFormula != null) {	scaleFormulas.put(scaleFormula.getName(), scaleFormula); }
		
	}
}
