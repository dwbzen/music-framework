package util.mongo;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

/**
 * Wrapper for db.collection.find()
 * Usage: 
 *  -db dbname			(optional, default = "test")
 *  -collection name
 *  -limit <num>		(limits # of results, defaults to 100 if not specified)
 *  -query <string>		query string (see examples below)
 *  				    delimit regular expressions with / /
 *
 * Example command line arguments:
 * -collection ifs1 -query "name:ifs1,type:point" -limit 100
 *  
 * @author dbacon
 *
 */
public class Find {
	protected static String[] pointsDisplayFields = {
		"name",  "type", "point", "Point2D"
	};
	
	public static String[] statsDisplayFields = {
		"name", "type",  "n",
		"minX",	"minY",
		"maxX", "maxY",
		"minPoint", "maxPoint"
	};

	
	/**
	 * field map by record type
	 */
	private static Map<String, String[]> fieldMap = new HashMap<String, String[]>();
	
	static {
		fieldMap.put("point", pointsDisplayFields);
		fieldMap.put("stats", statsDisplayFields);
	}
	
	static final Logger log = LogManager.getLogger(Find.class);
	public static final String DBNAME = "music";
	public static final String COLLECTION = "ifs1";
	
	private MongoClient mongoClient;
	private MongoDatabase db;
	private String collectionName;
	private int limit = 0;
	private String query = null;
	private MongoCollection<Document> collection = null;
	private String[] displayFields = pointsDisplayFields;
	
	public Find(String dbname, String cname) {
		connect(dbname, cname, "localhost", 27017);
	}
	
	public Find(String dbname, String cname, String uriString) {
		connect(dbname, cname, uriString);
	}

	public Find(String dbname, String cname, String hostName, int port) {
		connect(dbname, cname, hostName, port);
	}
	
	public void connect(String databaseName, String cname, String uriString) {
		try {
			MongoClientURI clientURI = new MongoClientURI(uriString);
			mongoClient = new MongoClient(clientURI);
			db = mongoClient.getDatabase( databaseName );
			collectionName = cname;
			collection = db.getCollection(collectionName);
			log.info("collection size: " + collection.count());
		} catch (Exception e) {
			System.err.println("Execption: " + e.toString());
			e.printStackTrace();
		}
		return;
	}
	
	public void connect(String databaseName, String cname, String hostName, int port) {
		try {
			mongoClient = new MongoClient(hostName, port );
			db = mongoClient.getDatabase(databaseName);
			collectionName = cname;
			collection = db.getCollection(collectionName);
			log.info("collection size: " + collection.count());
		} catch (Exception e) {
			System.err.println("Mongo Connection Exception: " + e.toString());
			e.printStackTrace();
		}
		return;
	}

	public static void main(String[] args) {
		String db = DBNAME;
		String cname = COLLECTION;
		int lim = 0;
		String query = null;
		String type = "NA";	// not assigned - point or stats are valid
		
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-db")) {
				db = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-collection")) {
				cname = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-type")) {
				type = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-limit")) {
				lim = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-query")) {
				query = args[++i];
			}
		}
		Find find = new Find(db, cname);
		find.setLimit(lim);
		find.setQuery(query);

		MongoCursor<Document> cursor = find.search();
		long count = find.getCount();
		System.out.println("count: " + count);

		List<String> jsonResults = null;
		if(fieldMap.containsKey(type)) {
			jsonResults = find.getSearchResults(cursor, fieldMap.get(type));
		}
		else {
			jsonResults = find.getSearchResults(cursor);
		}
		display(jsonResults, System.out);
		find.close();
	}

	private static void display(List<String> list, PrintStream out) {
		for(String l : list) {
			out.println(l);
		}
	}
	
	public long count() {
		return getCount();
	}

	public long getCount() {
		long count = 0;
		BasicDBObject dbObject = buildDBObject(getQuery());
		count = collection.count(dbObject);
		return count;
	}
	
	private List<String> getSearchResults(MongoCursor<Document> cursor) {
		List<String> sb = new ArrayList<String>();
		int recnum=0;
		while(cursor.hasNext()) {
			Document obj = cursor.next();
			sb.add( MongoUtil.recordToJSONString(++recnum, obj) );
		}
		return sb;
	}
	
	private List<String> getSearchResults(MongoCursor<Document> cursor, String[] displayFields) {
		List<String> sb = new ArrayList<String>();
		int recnum=0;
		while(cursor.hasNext()) {
			Document obj = cursor.next();
			sb.add(recnum + " " + obj.toJson());
			recnum++;
		}

		return sb;
	}

	public BasicDBObject buildDBQueryObject() {
		BasicDBObject dbObject = buildDBObject(getQuery());
		log.info("query dbObject: " + dbObject);
		return dbObject;
	}
	
	public MongoCursor<Document> search() {
		MongoCursor<Document> cursor = null;
		BasicDBObject dbObject = buildDBQueryObject();
		
		/*
		 * Query string format is field:value[,field:value...]
		 * For example: -query "city:ST PETERSBURG, state:FL"
		 * the query string must be converted to a DBObject (JSON)
		 * this is a requirement of the java API.
		 * Alternatively could use third-party tool such as Morphia
		 */
		if(this.limit > 0) {
			cursor = collection.find(dbObject).projection( Projections.excludeId()).limit(limit).iterator();
		}
		else {
			cursor = collection.find(dbObject).projection( Projections.excludeId()).iterator();
		}
		return cursor;
	}
	
	public void close() {
		this.mongoClient.close();
	}
	
	/**
	 * Example query string: name:ifs5, type:stats
	 * Format is field1:value1[,field2:value2 ...] or
	 * field1:/regex1/, etc
	 * Commas become "AND"
	 * 
	 * @param queryString
	 * @param locn
	 * @param maxdistance
	 * @return
	 */
	public BasicDBObject buildDBObject(String queryString) {
		BasicDBObject dbObject =  new BasicDBObject();
		if(queryString != null && queryString.length() >0 ) {
			String[] fqs = queryString.split(",");
			for(String fq: fqs) {
				String[] fvals = fq.split(":");
				// should do some format checking here to avoid possible index out of range
				String fval =  fvals[1].trim();
				if(fval.startsWith("/") && fval.endsWith("/")) {
					// well looky what we got here - one of them regular-type expression
					// strip off the leading and trailing "/" and compile
					Pattern pattern = Pattern.compile(fval.substring(1, fval.length()-1), Pattern.CASE_INSENSITIVE);
					dbObject.append(fvals[0], pattern);
				}
				else {
					dbObject.append(fvals[0].trim(), fval);
				}
			}
		}

		return dbObject;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public MongoDatabase getDb() {
		return db;
	}

	public MongoCollection<Document> getCollection() {
		return collection;
	}

	public String[] getDisplayFields() {
		return displayFields;
	}

	public void setDisplayFields(String[] displayFields) {
		this.displayFields = displayFields;
	}

	public static Map<String, String[]> getFieldMap() {
		return fieldMap;
	}
}
