package music.junit;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import music.element.Measure;

/**
 * @deprecated
 * @author bacond6
 *
 */
public class MeasureTest {
	public static void main(String[] args) throws UnknownHostException {
		if(args.length > 0) {
			Morphia morphia = new Morphia();
			MongoClient mongo = new MongoClient("localhost", 27017);
			
			Datastore ds = morphia.createDatastore(mongo, "test");
			morphia.map(Measure.class);
		}
	}
}
