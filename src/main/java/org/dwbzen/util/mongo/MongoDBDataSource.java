package org.dwbzen.util.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoCursor;

import org.dwbzen.util.Configuration;
import org.dwbzen.util.music.DataSource;

public class MongoDBDataSource extends DataSource {

	static Logger log = LogManager.getLogger(MongoDBDataSource.class);

	private String[] queryStrings = null;
	private String dbname;
	private int queryLimit;
	private Find find = null;
	private String collectionName = null;
	/*
	 * TODO use Random Predicate to filter stream
	 */
	ThreadLocalRandom random = ThreadLocalRandom.current();
	
	public MongoDBDataSource(Configuration config, String instrumentName) {
		super(config, instrumentName);
	}

	@Override
	public void configure() {
		queryStrings = configProperties.getProperty("mongodb.queryString").split(",");
		dbname = configProperties.getProperty("dataSource.mongodb.db.name");
		queryLimit = divisionsPerMeasure * 2 * measures;
		/*
		 * mongodb.queryString=stats,point,message
		 */
		queryStrings = configProperties.getProperty("mongodb.queryString").split(",");
	}

	@Override
	public Stream<String> stream() {
		collectionName = configProperties.getProperty("dataSource." + instrumentName);
		find = new Find(dbname, collectionName);
		find.setLimit(queryLimit);
		
		Collection<String> jsonRecords = readData();
		// close out the find()
		find.close();

		return jsonRecords.stream();
	}
	
	private Collection<String> readData() {
		Collection<String> jsonRecords = new ArrayList<String>();
		for(String query : queryStrings) {
			String queryString = NAME + collectionName + "," + TYPE + query;
			find.setQuery(queryString);
			find.setLimit(queryLimit);
			MongoCursor<Document> cursor = find.search();
			long count = find.count();
			log.info(queryString + " count: " + count);

			if(count > 0) {
				int recnum = 0;
				while(cursor.hasNext() && recnum < queryLimit) {
					Document doc = cursor.next();
					if(count <=2 || !randomSelection || random.nextBoolean() ) {
						recnum++;
						String jsonRecord = doc.toJson();
						jsonRecords.add(jsonRecord);
					}
				}
			}
		}
		return jsonRecords;
	}

	@Override
	public void close() {
		find.close();
	}


}
