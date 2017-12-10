package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

import util.mongo.Find;

public class JSONUtil {
	static final org.apache.log4j.Logger log = Logger.getLogger(JSONUtil.class);
	static Morphia morphia = new Morphia();
	static Datastore datastore = null;
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String defaultHost = "localhost";
	public static final int defaultPort = 27017;

	/** 
	 * Loads INameable Objects from flat JSON file. This expects one JSON object per line.
	 * If the mapped class implements IMapped<String> additional keys
	 * @param collectionName optional collection name (can be null) 
	 * @param inputFile
	 * @param mappedClass  a Class<IMapped<String>> that implements IMapped<String>. The name is used as the primary key in the Map returned.
	 * and additional keys are used if the class has a non-default implementation of keySet().
	 * 
	 * Note this uses the Morphia fromDBObject API and provides a null value for Datastore.
	 * This may not work in future releases of Morphia. I recommend using Mongo instead of JSON text files.
	 * 
	 * @deprecated 
	 */
	public static Map<String,IMapped<String>> 
					loadJSONCollection(String collectionName, 
										String inputFile,
										Class<?> mappedClass) throws IOException {
		morphia.map(mappedClass);

		Map<String,IMapped<String>> namableMap = new HashMap<String, IMapped<String>>();
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
			BasicDBObject dbObject = (BasicDBObject)JSON.parse(jsonline);
			if(dbObject != null) {
				log.debug("parsed " + dbObject.toString());
				BasicDBObject cf = (BasicDBObject) morphia.fromDBObject(datastore, mappedClass, dbObject);
				if(cf != null) {
					@SuppressWarnings("unchecked")
					IMapped<String> iMapped = (IMapped<String>)cf;
					addObjectToMap(iMapped, namableMap);
				}
			}
			else {
				log.info("null object returned " + jsonline);
				break;
			}
		}
		inputFileReader.close();
		return namableMap;
	}

	/**
	 * Loads INameable Objects from configured MongoDB. Tested with Song.class
	 * If the mapped class implements IMapped<String> additional keys from keySet() method.
	 * @param collectionName name of the MongoDB collection to retrieve 
	 * @param mappedClass a Class<IMapped<String>> that implements IMapped. 
	 * 		  The name is used as the primary key in the Map returned.
	 * 		  Additional keys if any in the form of a Set<String> come from keySet()
	 * @return Map<String,IMapped<String>>
	 * 
	 * @deprecated
	 */
	public static Map<String,IMapped<String>> 
					loadJSONCollection(String collectionName, 
										Class<?> mappedClass) {
		morphia.map(mappedClass);

		Map<String,IMapped<String>> namableMap = new HashMap<String, IMapped<String>>();
		String host = defaultHost;
		int port = defaultPort;
		Configuration configuration =  Configuration.getInstance(CONFIG_FILENAME);
		Properties configProperties = configuration.getProperties();
		String dbname = configProperties.getProperty("dataSource.mongodb.db.name");
		
		Find find = new Find(dbname, collectionName, host, port);
		MongoCursor<Document> cursor = find.search();
		long count = find.getCount();
		log.info(" count: " + count);
		if(count == 0) {
			log.warn("Nothing found in " + collectionName + " collection");
			return namableMap;
		}
		
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);
			String jsonString = dbObject.toString();
			log.debug("dbObject: " + jsonString);
			BasicDBObject obj = (BasicDBObject)JSON.parse(jsonString);
			@SuppressWarnings("unchecked")
			IMapped<String> iMapped = (IMapped<String>) morphia.fromDBObject(datastore, mappedClass, obj);
			addObjectToMap(iMapped, namableMap);
		}
		find.close();
		
		return namableMap;
	}

	public static void addObjectToMap(IMapped<String> cf, Map<String,IMapped<String>> map) {
		map.put(cf.getName(), cf);
		IMapped<String> iMapped = (IMapped<String>)cf;
		Set<String> keyList = iMapped.keySet();
		if(keyList != null && keyList.size() > 0) {
			for(String key : keyList) {
				if(!map.containsKey(key)) {
					map.put(key, cf);
				}
			}
		}
	}

}
