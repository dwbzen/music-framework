package music.element.song;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;

import music.element.Key;
import util.Configuration;
import util.IMapped;
import util.JSONUtil;
import util.mongo.Find;

/**
 * Statefull song manager. Loads Songs from a JSON text file or a MongoDB collection.
 * Maintains a Map<String,ChordFormula> after loading songs.
 * Adds HarmonyChords to Songs in original and optionally a transposition key.
 * @author don_bacon
 *
 */
public class SongManager {
	static final Logger log = LogManager.getLogger(SongManager.class);
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String DEFAULT_CHORD_FORMULAS_COLLECTION = "chord_formulas";
	public static final String defaultHost = "localhost";
	public static final int defaultPort = 27017;
	
	private Map<String,ChordFormula> chordFormulas = null;
	private Key key = null;
	private Map<String,IMapped<String>> songs = new HashMap<String, IMapped<String>>();
	private Songbook songbook = null;
	private Configuration configuration =  null;
	private Properties configProperties = null;
	private String dbname = null;
	private String host = defaultHost;
	private int port = defaultPort;
	private String collectionName = null;
	private String chordFormulasCollection = null;
	private String inputFileName = null;
	private Datastore datastore = null;

	/**
	 * Loads Songs from a JSON text file or a MongoDB collection.
	 * From the command line:
	 * -collection <collection name for songs> typically "songs"
	 * -file <JSON file> if loading from file instead of MongoDB
	 * -load : if present will actually load songs
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		String inputFile = null;		// complete path
		boolean loadSong = false;
		String collection = null;
		Map<String, IMapped<String>> songMap = null;
		Songbook songbook = null;

		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-load")) {
				loadSong =  true;
			}
			else if(args[i].startsWith("-collect")) {
				collection = args[++i];
			}
		}
		SongManager songMgr = new SongManager(collection, inputFile);
		if(loadSong) {
			if(inputFile != null) {
				 songMap = JSONUtil.loadJSONCollection(collection, inputFile, Song.class);
					if(songMap != null && songMap.size() > 0) {
						log.warn("#songs: " + songMap.size());
					}
			}
			else {
				songMgr.loadSongs();
				songbook = songMgr.getSongbook();
				// This works too
				// Map<String,IMapped<String>> namableMap = JSONUtil.loadJSONCollection(collection, Song.class);
				if(songbook != null && songbook.size() > 0) {
					log.warn("#songs: " + songbook.size());
				}
			}	
		}
	}
	
	public SongManager(String collection, String filename) {
		Find.morphia.mapPackage("music.element.song");
		configuration =  Configuration.getInstance(CONFIG_FILENAME);
		configProperties = configuration.getProperties();
		dbname = configProperties.getProperty("mongodb.db.name");
		this.collectionName = collection;
		this.inputFileName = filename;
	}
	
	protected void loadSongs( ) {
		chordFormulasCollection = DEFAULT_CHORD_FORMULAS_COLLECTION;
		loadSongs(this.collectionName, chordFormulasCollection, null);
	}
	
	/**
	 * Loads Songs from configured MongoDB
	 * @param songCollectionName the Song collection name
	 * @param chordFormulaCollectionName the ChordFormula collection name. Defaults to "chord_formulas"
	 * @return Map<String,IMapped<String>> keyed by song name, available as getSongs().
	 */
	public void loadSongs(String songCollectionName, String chordFormulaCollectionName, String queryString) {

		Find find = new Find(dbname, songCollectionName, host, port);
		Morphia morphia = Find.morphia;
		datastore = find.getDatastore();
		if(queryString != null) {
			find.setQuery(queryString);
		}
		MongoCursor<Document> cursor = find.search();
		long count = find.getCount();
		log.info(" #songs loaded: " + count);
		if(count == 0) {
			log.warn("Nothing found in " + songCollectionName + " collection");
			//return;
		}
		chordFormulas = ChordManager.loadChordFormulas(dbname, chordFormulaCollectionName);

		while(cursor.hasNext()) {
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);
			String jsonString = dbObject.toString();
			log.debug("dbObject: " + jsonString);
			BasicDBObject obj = (BasicDBObject)JSON.parse(jsonString);
			Song song = morphia.fromDBObject(datastore, Song.class, obj);
			addObjectToMap(song, songs);
			/*
			 * for analysis purposes add HarmonyChords and a transposition to C-Major
			 * The Scales also added to the Key
			 */
			Key transposedKey = Key.C_MAJOR;
			addHarmonyChordsToSong(transposedKey, song);
		}
		find.close();
		
		return;
	}
	
	public void addHarmonyChordsToSong(Song song) {
		addHarmonyChordsToSong(Key.C_MAJOR, song);
	}
	

	/**
	 * Adds HarmonyChord for each Harmony in the Key specified in the measure(s).
	 * If newKey is not null, also adds a HarmonyChord relative to the newKey.
	 * Also sets back references: Song in each Section, Section in each Measure
	 * @param newKey
	 * @param song
	 */
	public void addHarmonyChordsToSong(Key transposedKey, Song song) {
		for(Section section : song.getSections()) {
			section.setSong(song);
			for(SongMeasure measure : section.getSongMeasures()) {
				measure.setSection(section);
				if(measure.getKey() != null) {
					// Key appear only in measure 1 and when it changes
					key = measure.getKey();
					// and we need the complete Key - with designation and signature
					// signature is not in the song but can be derived from the name - needed to set accidental preference for chords
					key.setDesignationAndSignature();
					key.setAssociatedScale();
					log.debug("key: " + key.toJSON());
				}
				for(Harmony harmony : measure.getHarmony()) {
					String chordName = harmony.getName();
					log.debug("chord: " + chordName);
					if(chordName.equals("0")) {
						log.debug("0");
						continue;	// okay to let this go through
					}
					HarmonyChord hc = ChordManager.createHarmonyChord(chordName, chordFormulas, key);
					if(hc != null) {
						harmony.setHarmonyChord(hc);
						if(transposedKey != null) {
							harmony.setTransposedKey(transposedKey);
							harmony.setOriginalKey(key);
							HarmonyChord transposedHarmonyChord = new HarmonyChord(hc, key, transposedKey);
							harmony.setTransposedHarmonyChord(transposedHarmonyChord);
							log.debug("original key " + key.getName() + " chord: " + hc.toString() + " transposed to C-Major: " + transposedHarmonyChord.toString());
						}
					}
				}
			}
		}
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

	public Map<String,ChordFormula> getChordFormulas() {
		return chordFormulas;
	}

	public Key getKey() {
		return key;
	}

	public Map<String, IMapped<String>> getSongs() {
		return songs;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	/**
	 * Gets the Songbook view of the Song(s) in the collection.
	 * If there aren't any, well, you get an empty Songbook.
	 * 
	 * @return
	 */
	public Songbook getSongbook() {
		if(songbook == null) {
			songbook = new Songbook();
			for(IMapped<String> sm : songs.values()) {
				songbook.add((Song)sm);
			}
		}
		return songbook;
	}
}
