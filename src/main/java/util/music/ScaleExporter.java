package util.music;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.mongodb.morphia.Morphia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import music.element.Scales;
import util.Configuration;

/**
 * Creates a JSON representation for scale formulas in all roots.
 * Results can be imported into MongoDB, for example:
 * 
 * mongoimport --type json --collection scales --db test --file scales-C.json
 * 
 * @author don_bacon
 *
 */
public class ScaleExporter implements Consumer<String> {
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleExporter.class);
	private static Morphia morphia = new Morphia();
	private StringBuffer stringBuffer = null;
	static ObjectMapper mapper = new ObjectMapper();
	
	private List<Pitch> rootPitches = new ArrayList<Pitch>();
	private Map<String, ScaleFormula> scaleFormulas = new TreeMap<String, ScaleFormula>();
	
	public ScaleExporter() {
		morphia.map(ScaleFormula.class);
		morphia.map(Scale.class);
		loadScaleFormulas();
	}
	
	public static void main(String... args)  {
		String root = "C";
		Pitch pitch = null;
		int size = -1;
		PrintStream ps = System.out;
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-root")) {
    				root = args[++i];
    				pitch = new Pitch(root);
    			}
    			else if(args[i].equalsIgnoreCase("-size")) {
    				size = Integer.parseInt(args[++i]);
    			}
    		}
    	}
		ScaleExporter exporter = new ScaleExporter();
		if(pitch != null) { 	exporter.addRoot(pitch); }
		String exportString = size>0 ? exporter.exportScaleFormulas(size) :exporter.exportScaleFormulas();
		ps.println(exportString);
	}
	
	public void addRoot(Pitch p) {
		rootPitches.add(p);
	}
	
	public void loadScaleFormulas() {
		
		Configuration configuration = Configuration.getInstance("/config.properties");
		Properties configProperties = configuration.getProperties();
		String resource =   configProperties.getProperty("dataSource.scaleFormulas");
		InputStream is = this.getClass().getResourceAsStream("/data/music/" + resource);
    	Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();
    	stream.forEach(s -> accept(s));
    	stream.close();
	}
	
	/**
	 * Exports all scale formulas in Mathematica RawJSON format.
	 * For example: [ {"Major":{"groups":["major"],"formula":[2,2,1,2,2,2,1],"size":7}},
					  {"Pentatonic minor":{"groups":["pentatonic"],"formula":[3,2,3,2,2],"size":5}} ]
	 * In Mathematica: { 
	 * 				"Major" -> {"size" -> 7, "groups" -> {"major"}, "formula" -> {2, 2, 1, 2, 2, 2, 1}}, 
	 				"Pentatonic minor" -> {"size" -> 5, "groups" -> {"pentatonic"}, "formula" -> {3, 2, 3, 2, 2} }
	 				   }
	 * The association key is the scale name.
	 * Sends output to stdout.
	 */
	public String exportScaleFormulas() {
		stringBuffer = new StringBuffer("[\n");
		scaleFormulas.keySet().forEach(s ->  exportScaleFormula(scaleFormulas.get(s)));
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
		stringBuffer.append("]");
		return(stringBuffer.toString());
	}
	
	private void exportScaleFormula(ScaleFormula sf) {
		stringBuffer.append("{\"" + sf.getName() + "\":{\"groups\":");
		try {
			stringBuffer.append(mapper.writeValueAsString(sf.getGroups()));
			stringBuffer.append(", \"formula\":").append(mapper.writeValueAsString(sf.getFormula()));
		} catch (JsonProcessingException e) {
			System.err.println(e.toString());
		}
		stringBuffer.append(",\"size\":" + (int)sf.getSize());
		stringBuffer.append("} },\n");
	}
	
	public void exportScales(String root) {
		rootPitches.add(new Pitch(root));
		/*
		 * Create a scale with the root(s) specified and export
		 */
		for(ScaleFormula formula : scaleFormulas.values() ) {
			String mode = getMode(formula);
			ScaleType st = getScaleType(formula);
			log.debug("formula: " + formula.toJSON());
			for(Pitch pitch : rootPitches) {
				String name = formula.getName();	// + "-" + pitch.toString();
				Scale scale = new Scale(name, mode, st, pitch, formula);
				System.out.println(scale.toJSON());
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
		int n = formula.getFormula().length;
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

	@Override
	/**
	 * Deserializes a JSON ScaleFormula and adds to scaleFormulas Map
	 */
	public void accept(String formulaString) {
		DBObject dbo = (DBObject) JSON.parse(formulaString);
		ScaleFormula scaleFormula = morphia.fromDBObject(null, ScaleFormula.class, dbo);
		scaleFormulas.put(scaleFormula.getName(), scaleFormula);
		
	}
}
