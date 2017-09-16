package util.music;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Morphia;

import util.Configuration;
import util.mongo.Find;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;

/**
 * Creates a JSON representation for scale formulas in all roots.
 * Results can be imported into MongoDB, for example:
 * 
 * mongoimport --type json --collection scales --db test --file scales-C.json
 * 
 * @author don_bacon
 *
 */
public class ScaleExporter {
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleExporter.class);
	private static Morphia morphia = new Morphia();
	private static List<Pitch> rootPitches = new ArrayList<Pitch>();
	
	public static void main(String... args)  {
		String[] roots = { "C" };
		if(args.length != 0) {
			roots = args;
		}
		morphia.map(ScaleFormula.class);
		morphia.map(Scale.class);
		Configuration configuration = Configuration.getInstance("/config.properties");
		Properties configProperties = configuration.getProperties();
		String dbname = configProperties.getProperty("dataSource.mongodb.db.name");
		String collectionName = configProperties.getProperty("dataSource.mongodb.scaleFormulas");
		Find find = new Find(dbname, collectionName);
		find.setLimit(5000);
		MongoCursor<Document> cursor = find.search();
		long count = find.getCount();
		log.info(" count: " + count);
		Map<String, ScaleFormula> scaleFormulas = new TreeMap<String, ScaleFormula>();
		if(count > 0) {
			for(int i=0; i<roots.length; i++) {
				Pitch p = new Pitch(roots[i]);
				rootPitches.add(p);
			}
			
			while(cursor.hasNext()) {
				Document doc = cursor.next();	// int values come back as double unfortunately TODO - how to fix that?
				DBObject dbObject = new BasicDBObject(doc);
				ScaleFormula sf = morphia.fromDBObject(find.getDatastore(), ScaleFormula.class, dbObject);
				scaleFormulas.put(sf.getName(), sf);
			}
			find.close();
			/*
			 * export the mapped scales
			 */
			Map<String, Scale> mappedScales = Scale.getScaleMap();
			for(Scale scale : mappedScales.values()) {
				System.out.println(scale.toJSON());
			}
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
	}
	
	public static String getMode(ScaleFormula formula) {
		String mode = null;
		String name = formula.getName().toLowerCase();
		if(name.equalsIgnoreCase("major")) {
			mode = Scale.MAJOR;
		}
		else if(name.indexOf("minor") >= 0) {
			mode = Scale.MINOR;
		}
		else if(name.indexOf("mode") >= 0) {
			mode = Scale.MODE;
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
}
