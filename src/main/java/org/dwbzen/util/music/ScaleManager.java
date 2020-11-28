package org.dwbzen.util.music;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.ScaleType;
import org.dwbzen.util.Configuration;

public class ScaleManager {
	static final Logger log = LogManager.getLogger(ScaleManager.class);
	static final String CONFIG_FILENAME = "/config.properties";
	private  ObjectMapper mapper = new ObjectMapper();
	private String dataSourceName;
	private Properties properties;
	private Pitch defaultRootPitch = new Pitch("C");
	
	/**
	 * Usage: ScaleManager --scale:<scale name> --root:<root note>\n
	 * Example: java -jar ScaleManager.jar --scale:"Hirajoshi Japan" --root:C4
	 * 
	 * @param properties
	 */
	public static void main(String...args) {
		String scaleName = null;
		String root = null;
		for(String arg : args) {
			if(arg.toLowerCase().startsWith("--scale")) {
				int ind = arg.indexOf(":");
				scaleName = arg.substring(ind+1);
			}
			if(arg.toLowerCase().startsWith("--root")) {
				int ind = arg.indexOf(":");
				root = arg.substring(ind+1);
			}
		}
		if(scaleName != null && root != null) {
			Pitch rootPitch = new Pitch(root);
			ScaleManager scaleManager = new ScaleManager();
			Scale scale = scaleManager.getScale(scaleName, rootPitch);
			System.out.println(scale.toString());
		}
	}
	
	public ScaleManager(Properties properties) {
		setDataSourceName(properties.getProperty("dataSource", "file"));
	}
	
	public ScaleManager() {
		Configuration configuration = Configuration.getInstance(CONFIG_FILENAME);
		properties = configuration.getProperties();
		setDataSourceName(properties.getProperty("dataSource", "file"));
	}
	
	/**
	 * Gets the named mapped scale. No root is needed as that is implicit
	 * in the scale itself.
	 * 
	 * @see music.element.Scale SCALE_MAP
	 * @param scaleName
	 * @return Scale or null if not found
	 */
	public Scale getScale(String scaleName) {
		return  Scale.getScale(scaleName);	// check any mapped scales
	}
	
	/**
	 * Gets the named scale and instantiates with the given root pitch
	 * Note that the scale returned has all the notes specified in the scale formula
	 * which repeats the root. For example "Harmonic minor" which has the formula  [ 2 , 1 , 2 , 2 , 1 , 3 , 1]
	 * with a root "D" returns:  "notes" : "D, E, F, G, A, Bb, Db, D"
	 * The #notes is always 1+length of the formula.
	 * 
	 * Note -- if the scale is a mapped scale ("GFlat-Major-Pentatonic" for example) the root
	 * is not used as it's given by the scale name.
	 * 
	 * Truncate the scale if that top note is not needed: Scale truncated = scale.truncate();
	 * 
	 * @param scaleName
	 * @param Pitch rootPitch
	 * @return Scale or null if not found
	 */
	public Scale getScale(String scaleName, Pitch root) {
		Scale scale =  Scale.getScale(scaleName);	// check any mapped scales first
		if(scale == null) {
			ScaleFormula scaleFormula = null;
			String mode = null;
			ScaleType st = null;
			
			String resource =  (scaleName.startsWith("Theoretical")) ?   
					properties.getProperty("dataSource.scaleFormulas.theoretical") : properties.getProperty("dataSource.scaleFormulas");
			scaleFormula = findScaleFormula(scaleName, resource);
			
			if(scaleFormula != null) {
				mode = scaleFormula.getMode();
				st = scaleFormula.getScaleType();
				scale = new Scale(scaleName, mode, st, root, scaleFormula);
				log.debug("Found Scale: " + scaleName);
			}
			else {
				System.out.println("No such scale found: " + scaleName);
			}
		}
		return scale;

	}
	
	/**
	 * Gets the named scale and instantiates with the given root pitch
	 * @param scaleName
	 * @param rootPitch
	 * @return Scale or null if not found
	 */
	public Scale getScale(String scaleName, String rootPitch) {
		Pitch p = new Pitch(rootPitch);
		return getScale(scaleName, p);
	}
	
	public ScaleFormula findScaleFormula(String scaleName, String resource) {
		ScaleFormula scaleFormula = null;
		InputStream is = this.getClass().getResourceAsStream("/data/music/" + resource);
		/*
		 * try-with-resource will auto close the stream
		 */
		try(Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines()) {

			Optional<String> optional = stream.filter(s -> s.contains(scaleName)).findFirst();
			if(optional.isPresent()) {
				String formulaString = optional.get();
				try {
					scaleFormula = mapper.readValue(formulaString, ScaleFormula.class);
				} catch (IOException e) {
					log.error("Cannot deserialize " + formulaString + " because " + e.toString());
				}
			}
			else {
	    		log.error("No such scale: " + scaleName);
	    	}
		}
		return scaleFormula;
	}
	
	public Set<String> getMappedScaleNames() {
		 Map<String, Scale> scaleMap = Scale.getScaleMap();
		 return scaleMap.keySet();
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public Pitch getDefaultRootPitch() {
		return defaultRootPitch;
	}

	public void setDefaultRootPitch(Pitch defaultRootPitch) {
		this.defaultRootPitch = defaultRootPitch;
	}

}
