package music.action;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import util.Configuration;
import util.mongo.Find;

public class ScaleManager {
	static final Logger log = LogManager.getLogger(ScaleManager.class);
	static final String CONFIG_FILENAME = "/config.properties";
	
	private String dataSourceName;
	private Properties properties;
	private Morphia morphia = new Morphia();
	private Pitch defaultRootPitch = new Pitch("C");
	
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
			
			if(dataSourceName.equalsIgnoreCase("mongodb")) {
				scaleFormula = findScaleFormula(scaleName);
			}
			else  {
				String resource =  (scaleName.startsWith("Theoretical")) ?   
						properties.getProperty("dataSource.scaleFormulas.theoretical") : properties.getProperty("dataSource.scaleFormulas");
				scaleFormula = findScaleFormula(scaleName, resource);
			}
			
			if(scaleFormula != null) {
				mode = scaleFormula.getMode();
				st = scaleFormula.getScaleType();
				scale = new Scale(scaleName, mode, st, root, scaleFormula);
				log.debug("Found Scale: " + scale.toJSON());
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
    	Stream<String> stream = new BufferedReader(new InputStreamReader(is)).lines();

		Optional<String> optional = stream.filter(s -> s.contains(scaleName)).findFirst();
		if(optional.isPresent()) {
			String formulaString = optional.get();
			DBObject dbo = (DBObject) JSON.parse(formulaString);
			Morphia morphia = new Morphia();
			morphia.map(ScaleFormula.class);
			scaleFormula = morphia.fromDBObject(null, ScaleFormula.class, dbo);
			stream.close();
		}
		else {
    		System.err.println("No such scale: " + scaleName);
    	}
		return scaleFormula;
	}
	
	public ScaleFormula findScaleFormula(String scaleName) {
		ScaleFormula scaleFormula = null;
		String dbname = properties.getProperty("dataSource.mongodb.db.name");
		String collectionName = properties.getProperty("dataSource.mongodb.scaleFormulas");
		Find find = new Find(dbname, collectionName);
		String queryString = "name:" + scaleName;
		find.setQuery(queryString);
		MongoCursor<Document> cursor = find.search();
		long count = find.count();
		log.debug(queryString + " count: " + count);
		if(count > 0) {
			morphia.map(ScaleFormula.class);
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);	
			scaleFormula = morphia.fromDBObject(find.getDatastore(), ScaleFormula.class, dbObject);
		}
		find.close();
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
