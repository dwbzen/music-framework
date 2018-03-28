package music.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import music.element.song.HarmonyList;
import music.element.song.Section;
import music.element.song.Song;
import music.element.song.SongMeasure;
import util.music.SongManager;

/**
 * Analyzes the chord progressions and/or melodies of a collection of Songs
 * Chord progression analysis types:
 * a. formula only (eg. M M m6 M) key is the chord formulas
 * b. formula + interval (eg. Key E-Major: 7bM 4M 6bM 4m6 1M )
 * c. Relative HarmonyChord (chords transposed to C-Major, eg. Bb, F, Ab, Fm6, C ) 
 * d. Absolute HarmonyChord (eg. Key E-Major: D, A, C, Am6, E )
 * Records #occurrences of each for lengths of 2,3,4,5
 * occurrences has 2 dimensions: #occurrences overall, and #songs
 * A given HarmonyChord progression may occurr for example 6 times, but only in 1 song.
 * Example query strings:
 *   -query "artist:The Beatles"
 *   -query "name:Penny Lane"
 *   -query "name:With A Little Help From My Friends"
 *   -query "album:"Sergeant Pepper's Lonely Hearts Club Band"
 */
public class SongAnalyzer {
	protected static final Logger log = LogManager.getLogger(SongAnalyzer.class);

	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String defaultHost = "localhost";
	public static final int defaultPort = 27017;
	static int mininumMemeLength = 1;
	static int maximumMemeLength = 5;
	
	public static enum KeyType {
		FORMULA, FORMULA_INTERVAL, RELATIVE_HARMONY_CHORD, ABSOLUTE_HARMONY_CHORD
	}
	public static KeyType[] analysisTypes = 
		{KeyType.FORMULA, KeyType.FORMULA_INTERVAL, KeyType.RELATIVE_HARMONY_CHORD, KeyType.ABSOLUTE_HARMONY_CHORD};
		
	/*
	 * TreeMap<String, Integer> memeCollectionMap = null;
	 * Key + count for that key. Sorted by key.
	 *
	 * Map<Integer, TreeMap<String, Integer>> memeCollectionMapCounts = null;
	 * Length + a memeCollectionMap for that length
	 */

	/**
	 * AnalysisType + map of memeCollectionMapCounts (by instance) for that type
	 */
	private Map<KeyType, Map<Integer, TreeMap<String, Integer>>> memeKeyTypeMap = 
			new HashMap<KeyType, Map<Integer, TreeMap<String, Integer>>>();
	
	/**
	 * AnalysisType + map of memeCollectionMap (by song) for that type
	 */
	private Map<KeyType, Map<Integer, TreeMap<String, List<Song>>>> memeKeyTypeBySongMap = 
			new HashMap<KeyType, Map<Integer, TreeMap<String, List<Song>>>>();


	/**
	 * Each IMapped<String> is a Song instance
	 */
	private Map<String, Song> songMap = null;
	/*
	 * The current Song under analysis
	 */
	private Song currentSong = null;

	public SongAnalyzer(Map<String, Song> songMap) {
		this.songMap = songMap;
		for(KeyType atype : analysisTypes) {
			Map<Integer, TreeMap<String, Integer>> memeCollectionMapCounts = new HashMap<Integer, TreeMap<String, Integer>>();
			Map<Integer, TreeMap<String, List<Song>>> memeCollectionMapBySong = new HashMap<Integer, TreeMap<String, List<Song>>>();
			
			for(int mlen = mininumMemeLength; mlen<=maximumMemeLength; mlen++) {
				TreeMap<String, Integer> memeCollectionMap = new TreeMap<String, Integer>();	// key counts
				TreeMap<String, List<Song>> memeCollectionSongMap = new TreeMap<String, List<Song>>();	// key - List<Song>
				memeCollectionMapCounts.put(Integer.valueOf(mlen), memeCollectionMap);
				memeCollectionMapBySong.put(Integer.valueOf(mlen), memeCollectionSongMap);
			}
			memeKeyTypeMap.put(atype, memeCollectionMapCounts);
			memeKeyTypeBySongMap.put(atype, memeCollectionMapBySong);
		}
	}
	
	/**
	 * Syntax:  SongAnalyzer [-file filename | -collection collectionName  [-query queryString] ]
	 * example: SongAnalyzer -collection songs -query "artist:The Beatles"
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String songInputFile = null;			// complete path to .JSON Song file 
		String songCollectionName = null;
		boolean quiet = false;
		String query = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-collection")) {
				songCollectionName = args[++i];
			}
			else if(args[i].startsWith("-file")) {
				songInputFile = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-query")) {
				query = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-quiet")) {
				// don't output the results
				quiet = true;
			}
		}
		SongManager songMgr = new SongManager(songCollectionName, songInputFile, query);
		songMgr.loadSongs();
		Map<String, Song> songMap = songMgr.getSongs();
		
		SongAnalyzer songAnalyzer = new SongAnalyzer(songMap);
		songAnalyzer.analyze();
		if(!quiet) {
			songAnalyzer.displayResults();
		}
	}
	
	/**
	 * Display all results, comma-separated for spreadsheet.
	 */
	public void displayResults() {
		System.out.println("Key Type,Key,Key Length,Count, Songs");
		for(KeyType atype : analysisTypes) {
			displayResults(atype);
		}
	}

	/**
	 * Display results for a given KeyType
	 * @param atype
	 */
	private void displayResults(KeyType atype) {
		Map<Integer, TreeMap<String, Integer>> memeCollectionMapCounts = memeKeyTypeMap.get(atype);
		Map<Integer, TreeMap<String, List<Song>>> memeCollectionMapBySong = memeKeyTypeBySongMap.get(atype);
		for(int keylen=mininumMemeLength; keylen <= maximumMemeLength; keylen++) {
			TreeMap<String, Integer> memeCollectionMap = memeCollectionMapCounts.get(keylen);
			TreeMap<String, List<Song>> memeCollectionSongMap = memeCollectionMapBySong.get(keylen);
			for(String key: memeCollectionMap.keySet()) {
				if(key.equalsIgnoreCase("none") || key.equals("0")) {
					continue;
				}
				System.out.print(atype + "," + key + "," + keylen + "," + memeCollectionMap.get(key) );
				List<Song> sl = memeCollectionSongMap.get(key);
				System.out.println( "," + sl.size());
			}
		}
	}

	public void analyze() {
		log.info("Start analysis");
		for(Song sm : songMap.values()) {
			Song song = (Song)sm;
			analyze(song);
		}
		log.info("song analysis complete");
	}

	public void analyze(Song song) {
		currentSong = song;
		log.info("analyzing song '" + currentSong.getName() + "'");
		for(Section section : song.getSections()) {
			analyze(section);
		}
	}

	/**
	 * Analyzes one Section of a Song.
	 * For analysis purposes, A slash chord is treated as a normal chord with the root in the bass.
	 * So A7 and A7/G have the same formula number, but different spelling numbers.
	 * @param section
	 */
	public void analyze(Section section) {
		Song sectionSong = section.getSong();
		log.info("  analyzing section '" + section.getName() + "'" + " for song: " + sectionSong.getName());
		List<SongMeasure> measures = section.getSongMeasures();
		/*
		 * Gather references to the Harmony. This list is the basis for the analysis, not the SongMeasures
		 */
		HarmonyList harmonyList = new HarmonyList();
		harmonyList.addAll(measures);
		int numberOfChords = harmonyList.size();
		log.info("    #chords this section: " + numberOfChords);

		for(int index=0; index<numberOfChords; index++) {
			// look ahead current Harmony + next 1,2,3,4
			// for each analysis type
			for(KeyType atype : analysisTypes) {	// for each type of analysis
				for(int mlen = mininumMemeLength; mlen<=maximumMemeLength; mlen++) {	// for each meme length
					memeCollector(atype, mlen, index, harmonyList, sectionSong);
				}
			}
		}
		log.info("  section analysis complete");
	}
	
	/**
	 * 
	 * @param keyType the KeyType being collected
	 * @param mlen #tokens to collect for the key
	 * @param startIndex where to start the collection in the HarmonyList
	 * @param harmonyList any HarmonyList to analyze, typically a Section's worth
	 * @param song the Song the HarmonyList belongs to
	 */
	public void memeCollector(KeyType keyType, int mlen, int startIndex, HarmonyList harmonyList, Song song) {
		log.debug("  Key type: " + keyType + ", mlen: " + mlen + " starting at " + startIndex);
		int endIndex = startIndex + mlen - 1;
		int count = 1;
		// get the collection map for this KeyType and meme length
		TreeMap<String, Integer> memeCollectionMap = memeKeyTypeMap.get(keyType).get(mlen);
		TreeMap<String, List<Song>> memeCollectionSongMap = memeKeyTypeBySongMap.get(keyType).get(mlen);
		String key = harmonyList.getAnalysisKey(keyType, startIndex, endIndex);
		if(key != null && key.length() > 0) {
			// update or set instance count
			if(memeCollectionMap.containsKey(key)) {
				count = memeCollectionMap.get(key).intValue() + 1;
			}
			memeCollectionMap.put(key, count);
			// add to memeCollectionMap (by song)
			List<Song> songList = memeCollectionSongMap.get(key);
			if(songList != null) {
				if(!songList.contains(song)) {
					songList.add(song);
				}
			}
			else {
				songList = new ArrayList<Song>();
				songList.add(song);
				memeCollectionSongMap.put(key, songList);
			}
			log.debug("  Key type: " + keyType + " complete, key is: '" + key + "', count = " + count);
		}
	}

	public Map<String, Song> getSongMap() {
		return songMap;
	}
	
	public Map<KeyType, Map<Integer, TreeMap<String, Integer>>> getAnalysisResults() {
		return this.memeKeyTypeMap;
	}
	
	/**
	 * Clears Song map and results for another run
	 */
	public void clear() {
		memeKeyTypeMap.clear();
		songMap.clear();
	}
	
}
