package util.cp;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.cp.CollectorStats;
import mathlib.cp.ICollector;
import mathlib.cp.MarkovChain;
import music.element.song.*;
import util.IMapped;
import util.music.SongManager;

/**
 * Collects chord progressions of a given length from Song instances.
 * Similar to SongAnalyzer, the ChordProgressionCollector gathers statistics about
 * what HarmonyChord follows a HarmonyChord progression of length n (usually 2, but set
 * by the user). This info is saved as CollectorStats where it can
 * be used by ChordProgressionProducer to generate chord progressions starting with
 * an initial seed HarmonyChord.
 * Note that the transposed ChordProgression (a List<HarmonyChord>) forms subsets. 
 * The SongManager adds the transposition to C-Major/A-Minor key to each Harmony instance.
 * 
 * @author Don_Bacon
 *
 */
public class HarmonyChordCollector implements ICollector<ChordProgression, MarkovChain<HarmonyChord, ChordProgression>, Song> {
	protected static final Logger log = LogManager.getLogger(HarmonyChordCollector.class);
	public static final String CONFIG_FILENAME = "/config.properties";
	private int keylen;
	private Songbook songbook = null;
	private Song song = null;
	private boolean useOriginalKey = false;
	
	/**
	 * Each IMapped<String> is a Song instance
	 */
	private Map<String, IMapped<String>> songMap = null;
	
	private MarkovChain<HarmonyChord, ChordProgression> markovChain;
	
	/**
	 * Factory method.
	 * @param songbook Songbook
	 * @param keylen
	 * @return
	 */
	public static HarmonyChordCollector getChordProgressionCollector(Songbook songbook, int order) {
		HarmonyChordCollector collector = new HarmonyChordCollector(order);
		collector.setSongbook(songbook);
		return collector;
	}
	
	/**
	 * Factory method.
	 * @param song
	 * @param keylen
	 * @return
	 */
	public static HarmonyChordCollector getChordProgressionCollector(Song song, int order) {
		HarmonyChordCollector collector = new HarmonyChordCollector(order);
		collector.setSong(song);
		return collector;
	}

	protected HarmonyChordCollector(int order) {
		this.keylen = order;
		markovChain =
				new MarkovChain<HarmonyChord, ChordProgression>(new ChordProgressionComparator(), order);
	}
	
	@Override
	public void collect() {
		if(song != null) {
			accept(song);
		}
		else if(songbook != null && songbook.size()>0) {
			for(Song asong : songbook) {
				accept(asong);
			}
		}
	}
	
	@Override
	public void accept(Song song) {
		log.info("accept song '" + song.getName() + "'");
		song.setOriginalKey(useOriginalKey);
		/*
		 * Gather references to the HarmonyChords. This ChordProgression is the basis for the analysis.
		 * Make sure everything in the same key. SongManager does this automatically when Song are loaded
		 * as each Harmony has associated HarmonyChords in both the original and transposed key
		 * get() also adds a terminating HarmonyChord at the end (if not null). The default is HarmonyChord.TERMINAL_HARMONY_CHORD
		 */
		ChordProgression chordProgression = null;
		while((chordProgression = song.get()) != null) {
			apply(chordProgression);
		}
		log.info("collection for '" + song.getName() + "' complete");
	}
	
	@Override
	public MarkovChain<HarmonyChord, ChordProgression> apply(ChordProgression chordProgression) {
		int numberOfTokens = chordProgression.size();
		ChordProgression subset = null;
		HarmonyChord nextHarmonyChord = null;
		if(numberOfTokens > 0) {
			log.debug("apply: " + chordProgression);
			int lim = numberOfTokens - keylen + 1;
			for(int i=0; i<lim; i++) {
				int index = i + keylen;
				if(index <= numberOfTokens) {		// don't run off the end of the List
					subset = chordProgression.subset(i, i+keylen);
					nextHarmonyChord =  (index == numberOfTokens) ? ChordProgression.TERMINAL : chordProgression.get(i+keylen);
					log.debug("  subset: '" + subset + " next HarmonyChord: " + nextHarmonyChord);
					addOccurrence(subset, nextHarmonyChord);
				}
			}
			// add terminal state
			subset = chordProgression.subset(lim);
			subset.add(ChordProgression.TERMINAL);
			log.debug("  terminal subset: '" + subset + "'");
			addOccurrence(subset, ChordProgression.NULL_VALUE);

		}
		return markovChain;
	}

	private void addOccurrence(ChordProgression theChordProgression, HarmonyChord theHarmonyChord ) {
		boolean terminal = theHarmonyChord.equals(ChordProgression.NULL_VALUE);
		if(markovChain.containsKey(theChordProgression)) {
			CollectorStats<HarmonyChord, ChordProgression> collectorStats = markovChain.get(theChordProgression);
			collectorStats.addOccurrence(theHarmonyChord);
			collectorStats.setTerminal(terminal);
		}
		else {
			CollectorStats<HarmonyChord, ChordProgression> collectorStats = 
					new CollectorStats<HarmonyChord, ChordProgression>();
			collectorStats.setSubset(theChordProgression);
			collectorStats.addOccurrence(theHarmonyChord);
			markovChain.put(theChordProgression, collectorStats);
			collectorStats.setTerminal(terminal);
		}
	}
	
	/**
	 * Syntax:  SongCollector -songs [file:filename | collection:collectionName] [-query queryString] -keylen n [-print | -noprint] [-summary] 
	 * example: SongCollector -songs collection:songs -query "artist:The Beatles" -keylen 2 -print -summary
	 * Uses the MongoDB "chord_formulas" collection for chords; can override in command line.
	 * 
	 * @param args
	 * @throws IOException
	 * 
	 */public static void main(String... args) throws IOException {
		String songInputFile = null;			// complete path to .JSON Song file TODO
		String songCollectionName = null;
		String chordFormulaCollectionName = "chord_formulas";
		String query = null;
		boolean print = false;
		boolean summary = false;
		boolean useOriginalKey = false;
		int keylen = 2;

		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-song")) {
				String[] songargs = args[++i].split(":");
				if(songargs[0].equalsIgnoreCase("file")) {
					songInputFile = songargs[1];
				}
				else if(songargs[0].equalsIgnoreCase("collection")) {
					songCollectionName = songargs[1];
				}
			}
			else if(args[i].startsWith("-chords")) {
				String[] chordargs = args[++i].split(":");
				if(chordargs[0].equalsIgnoreCase("collection")) {
					chordFormulaCollectionName = chordargs[1];
				}
			}
			else if(args[i].equalsIgnoreCase("-query")) {
				query = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-keylen")) {
				keylen = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-print")) {
				print = true;
			}
			else if(args[i].equalsIgnoreCase("-noprint")) {
				print = false;
			}
			else if(args[i].startsWith("-original")) {	// as in originalKey
				useOriginalKey = true;
			}
			else if(args[i].equalsIgnoreCase("-summary")) {
				summary = true;
			}
		}
		SongManager songMgr = new SongManager(songCollectionName, songInputFile, query);
		songMgr.loadSongs();
		Songbook songbook = songMgr.getSongbook();
		HarmonyChordCollector collector = HarmonyChordCollector.getChordProgressionCollector(songbook, keylen);
		collector.setUseOriginalKey(useOriginalKey);
		collector.collect();
		
		MarkovChain<HarmonyChord, ChordProgression> markovChain = collector.getMarkovChain();
		HarmonyChord.setIncludeSpellingInToString(false);	// set to true if you want to see the spelling of each chord
		if(print) {
			System.out.print(markovChain.getMarkovChainDisplayText());
		}
		if(summary) {
			System.out.print(markovChain.getSummaryMapText());
		}
	}

	public int getKeylen() {
		return keylen;
	}

	public void setKeylen(int keylen) {
		this.keylen = keylen;
	}

	public Map<String, IMapped<String>> getSongMap() {
		return songMap;
	}

	public MarkovChain<HarmonyChord, ChordProgression> getMarkovChain() {
		return markovChain;
	}

	public Map<ChordProgression, Integer> getSummaryMap() {
		return getMarkovChain().getSummaryMap();
	}

	public Songbook getSongbook() {
		return songbook;
	}

	protected void setSongbook(Songbook songbook) {
		this.songbook = songbook;
	}

	public Song getSong() {
		return song;
	}

	protected void setSong(Song song) {
		this.song = song;
	}

	public boolean isUseOriginalKey() {
		return useOriginalKey;
	}

	public void setUseOriginalKey(boolean useOriginalKey) {
		this.useOriginalKey = useOriginalKey;
	}

}
