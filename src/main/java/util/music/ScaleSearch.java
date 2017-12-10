package util.music;
import java.util.Properties;

import music.element.Key;
import music.element.Key.Mode;
import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import music.element.Step;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Morphia;

import util.Configuration;
import util.mongo.Find;
import util.music.ScaleManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;

/**
 * Search routines for Scale and ScaleFormula for use in web services.
 * find methods return JSON String
 * get methods return instances (Scale or ScaleFormula)
 * 
 */
public class ScaleSearch {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ScaleSearch.class);
	private Morphia morphia = new Morphia();
	private Configuration configuration;
	private String dbname;
	private Properties configProperties = null;
	private String scaleFormulas_collectionName;
	private String scales_collectionName;
	private Find findScale = null;
	private Find findScaleFormula = null;
	private String uri;
	
	public static final String CONFIG_FILENAME = "/config.properties";


	public ScaleSearch() {
		this.configuration = Configuration.getInstance(CONFIG_FILENAME);
		this.configProperties = configuration.getProperties();
		configure();
		morphia.map(ScaleFormula.class);
		morphia.map(Scale.class);
		morphia.map(Pitch.class);
		morphia.map(ScaleType.class);
		morphia.map(Step.class);
		morphia.map(Key.class);
		morphia.map(Mode.class);
	}

	public ScaleFormula getScaleFormula(String formulaName){
		ScaleFormula scaleFormula = scaleFormulaSearch(formulaName);
		return scaleFormula;		// could be null if not found
	}
	
	public String findScaleFormula(String formulaName){
		String sfJSON = null;
		ScaleFormula scaleFormula = scaleFormulaSearch(formulaName);
		if(scaleFormula != null) {
			sfJSON = scaleFormula.toJSON();
		}
		return sfJSON;
	}
	
	private ScaleFormula scaleFormulaSearch(String formulaName) {
		ScaleFormula scaleFormula = null;
		if(findScaleFormula == null) {
			findScaleFormula = new Find(dbname, scaleFormulas_collectionName, uri);
		}
		String queryString = "name:" + formulaName;
		findScaleFormula.setQuery(queryString);
		MongoCursor<Document> cursor = findScaleFormula.search();
		long count = findScaleFormula.getCount();
		log.debug(queryString + " count: " + count);
		if(count > 0) {
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);
			scaleFormula = morphia.fromDBObject(null, ScaleFormula.class, dbObject);
		}
		findScaleFormula.close();
		return scaleFormula;
	}
	
	public Scale getScaleFromFormula(String formulaName, String root) {
		ScaleFormula formula = scaleFormulaSearch(formulaName);
		Scale scale = null;
		if(formula != null) {
			String mode = ScaleManager.getMode(formula);
			ScaleType st = ScaleManager.getScaleType(formula);
			String name = formula.getName();
			Pitch rootPitch = new Pitch(root);
			scale = new Scale(name, mode, st, rootPitch, formula);
		}
		return scale;
	}
	
	public String findScaleFromFormula(String formulaName, String root) {
		String scaleString = null;
		ScaleFormula formula = scaleFormulaSearch(formulaName);
		if(formula != null) {
			String mode = ScaleManager.getMode(formula);
			ScaleType st = ScaleManager.getScaleType(formula);
			String name = formula.getName();
			Pitch rootPitch = new Pitch(root);
			Scale scale = new Scale(name, mode, st, rootPitch, formula);
			scaleString = scale.toJSON();
		}
		return scaleString;
	}
	
	
	public Scale getScale(String scaleName, String root) {
		Scale scale = scaleSearch(scaleName, root);
		return scale;
	}

	public String findScale(String scaleName, String root) {
		String sfJSON = null;
		Scale scale = scaleSearch(scaleName, root);
		if(scale != null) {
			sfJSON = scale.toJSON();
		}
		return sfJSON;
	}

	private Scale scaleSearch(String scaleName, String root) {
		Scale scale = null;
		if(findScale == null) {
			findScale = new Find(dbname, scales_collectionName, uri);
		}
		String queryString = "name:" + scaleName + ",root:" + root;
		findScale.setQuery(queryString);
		MongoCursor<Document> cursor = findScale.search();
		long count = findScale.getCount();
		log.debug(queryString + " count: " + count);
		if(count > 0) {
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);			
			log.debug("scale: " + dbObject.toString());
			
			scale = morphia.fromDBObject(null, Scale.class, dbObject);
		}
		findScale.close();
		return scale;
	}
	
	private void configure() {
    	/*
    	 * MongoDB parameters
    	 */
		this.dbname = configProperties.getProperty("dataSource.mongodb.db.name");
		this.scaleFormulas_collectionName = configProperties.getProperty("dataSource.mongodb.scaleFormulas", "scale_formulas");
		this.scales_collectionName = configProperties.getProperty("dataSource.mongodb.scales");
		this.uri = configProperties.getProperty("dataSource.mongodb.connectionURI", "mongodb://localhost:27017");
	}
	
	/**
	 * Search for a scale formula by name
	 * Usage: ScaleSearch -formula name -root pitch 
	 * @param args
	 */
	public static void main(String... args) {
		String formulaName = null;
		String root = null;
		for(int i = 0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-formula")) {
				formulaName = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-root")) {
				root = args[++i];
			}
		}
		ScaleSearch search = new ScaleSearch();
		if(formulaName != null && root != null) {
			Scale scale = search.getScaleFromFormula(formulaName, root);
			if(scale != null) {
				System.out.println(scale.toJSON());
			}
		}
	}
	
}
