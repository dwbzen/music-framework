package org.dwbzen.util.music;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;

import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.song.ChordFormula;
import org.dwbzen.music.element.song.ChordFormulas;
import org.dwbzen.music.element.song.Harmony;
import org.dwbzen.music.element.song.HarmonyChord;
import org.dwbzen.music.element.song.Section;
import org.dwbzen.music.element.song.Song;
import org.dwbzen.music.element.song.SongMeasure;
import org.dwbzen.music.element.song.Songbook;
import org.dwbzen.util.Configuration;
import org.dwbzen.util.mongo.Find;

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
	public static final String defaultHost = "localhost";
	public static final int defaultPort = 27017;
	
	static final String[] songKeys = {"name", "album", "artist" };
	
	private Map<String, ChordFormula> chordFormulas = null;
	private Key key = null;
	private Map<String, Song> songMap = new HashMap<String, Song>();	// Map of songs by name
	private Songbook songbook = null;
	private Configuration configuration =  null;
	private Properties configProperties = null;
	private ChordManager chordManager = null;
	
	private String dbname = null;
	private String host = defaultHost;
	private int port = defaultPort;
	private String songCollectionName = null;
	private String inputFileName = null;
	private String queryString = null;
	ObjectMapper mapper = new ObjectMapper();


	/**
	 * Loads Songs from a MongoDB collection or a single file.
	 * From the command line:
	 * -collection <collection name for songs> typically "songs"
	 * -file <name.json> a single song file in JSON format
	 * -query <string> 	optional query string for collection
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String... args) throws IOException {
		String inputFile = null;		// complete path
		String songcollection = null;
		Songbook songbook = null;
		String query = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-collect")) {
				songcollection = args[++i];
			}
			else if(args[i].startsWith("-file")) {
				inputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-query")) {
				query = args[++i];
			}
		}
		SongManager songMgr = new SongManager(songcollection, inputFile, query);
		songMgr.loadSongs();
		songbook = songMgr.getSongbook();
		if(songbook != null && songbook.size() > 0) {
			log.info("#songs loaded: " + songbook.size());
		}
	}
	
	/**
	 * Constructor. Specify a collection or a filename but not both (one must be null).
	 * If both ARE specified, it loadsSongs from the specified file.
	 * A query string is optional and used if a collection is specified.
	 * @param collection
	 * @param filename
	 * @param queryString
	 */
	public SongManager(String collection, String filename, String query) {
		chordManager = new ChordManager();	// also loads chord_formulas
		ChordFormulas cf = chordManager.getChordFormulas();
		for(ChordFormula chordFormula : cf.getChordFormulas()) {
			chordFormulas.put(chordFormula.getName(), chordFormula);
			for(String s : chordFormula.getSymbols()) {
				chordFormulas.put(s, chordFormula);
			}
		}
		
		configuration =  Configuration.getInstance(CONFIG_FILENAME);
		configProperties = configuration.getProperties();
		dbname = configProperties.getProperty("dataSource.mongodb.db.name", "music");
		inputFileName = filename;
		songCollectionName = collection;
		queryString = query;
	}
	
	public void loadSongs( ) {
		if(inputFileName != null) {
			loadSongs(inputFileName);
		}
		else {
			loadSongs(songCollectionName, queryString);
		}
	}
	
	void loadSongs(String filename) {
		Song song = loadSongFile(filename);
		if(song != null) {
			addObjectToMap(song);
			/*
			 * for analysis purposes add HarmonyChords and a transposition to C-Major
			 * The Scales also added to the Key
			 */
			Key transposedKey = Key.C_MAJOR;
			addHarmonyChordsToSong(transposedKey, song);
		}
	}
	
	/**
	 * Loads Songs from configured MongoDB
	 * @param songCollectionName the Song collection name
	 * @param chordFormulaCollectionName the ChordFormula collection name. Defaults to "chord_formulas"
	 * @return Map<String,IMapped<String>> keyed by song name, available as getSongs().
	 */
	void loadSongs(String songCollectionName, String queryString) {

		Find find = new Find(dbname, songCollectionName, host, port);
		if(queryString != null) {
			find.setQuery(queryString);
		}
		MongoCursor<Document> cursor = find.search();
		long count = find.getCount();
		log.debug(" #songs loaded: " + count);
		if(count == 0) {
			log.warn("Nothing found in " + songCollectionName + " collection");
		}

		while(cursor.hasNext()) {
			Document doc = cursor.next();
			DBObject dbObject = new BasicDBObject(doc);
			String jsonString = dbObject.toString();
			log.debug("dbObject: " + jsonString);
			Song song = accept(jsonString);
			addObjectToMap(song);
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
					HarmonyChord hc = chordManager.createHarmonyChord(chordName, key);
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

	private void addObjectToMap(Song song) {
		songMap.put(song.getName(), song);
	}

	public Map<String, ChordFormula> getChordFormulas() {
		return chordFormulas;
	}

	public Key getKey() {
		return key;
	}

	public Map<String, Song> getSongs() {
		return songMap;
	}

	public String getSongCollectionName() {
		return songCollectionName;
	}

	public void setSongCollectionName(String collectionName) {
		songCollectionName = collectionName;
	}

	public String getInputFileName() {
		return inputFileName;
	}

	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}

	public String getQueryString() {
		return queryString;
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
			for(Song sm : songMap.values()) {
				songbook.add((Song)sm);
			}
		}
		return songbook;
	}
	
	/**
	 * Deserializes a JSON Song
	 */
	public Song accept(String songJsonString) {
		Song song = null;
		log.debug(songJsonString);
		try {
			song = mapper.readValue(songJsonString, Song.class);
		} catch (Exception e) {
			log.error("Cannot deserialize " + songJsonString + "\nbecause " + e.toString());
		}
		return song;
	}
	
	private Song loadSongFile(String filename) {
		BufferedReader inputFileReader;
		StringBuffer sb = new StringBuffer();
		Song song = null;
		try {
			inputFileReader = new BufferedReader(new FileReader(filename));
			String line;
			while((line = inputFileReader.readLine()) != null) {
				String jsonline = line.trim();
				if(jsonline.startsWith("//") || jsonline.startsWith("/*")) {
					continue;
				}
				sb.append(jsonline);
			}
			song = accept(sb.toString());
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + filename);
		} catch (IOException e) {
			System.err.println("IO Exception: " + filename);
		}
		return song;
	}
}
