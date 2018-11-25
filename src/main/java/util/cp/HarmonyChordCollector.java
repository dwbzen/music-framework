package util.cp;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.cp.CollectorStats;
import mathlib.cp.ICollector;
import mathlib.cp.MarkovChain;
import music.element.song.ChordProgression;
import music.element.song.ChordProgressionComparator;
import music.element.song.HarmonyChord;
import music.element.song.Song;
import music.element.song.Songbook;
import util.IMapped;

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
public class HarmonyChordCollector implements ICollector<ChordProgression, MarkovChain<HarmonyChord, ChordProgression, Song>, Song> {
	protected static final Logger log = LogManager.getLogger(HarmonyChordCollector.class);
	public static final String CONFIG_FILENAME = "/config.properties";
	private int order;
	private Songbook songbook = null;
	private Song song = null;
	private boolean useOriginalKey = false;
	private boolean trace = false;
	
	/**
	 * Each IMapped<String> is a Song instance
	 */
	private Map<String, IMapped<String>> songMap = null;
	
	private MarkovChain<HarmonyChord, ChordProgression, Song> markovChain;
	
	/**
	 * Factory method.
	 * @param songbook Songbook
	 * @param order
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
	 * @param order
	 * @return
	 */
	public static HarmonyChordCollector getChordProgressionCollector(Song song, int order) {
		HarmonyChordCollector collector = new HarmonyChordCollector(order);
		collector.setSong(song);
		return collector;
	}

	protected HarmonyChordCollector(int order) {
		this.order = order;
		markovChain =
				new MarkovChain<HarmonyChord, ChordProgression, Song>(new ChordProgressionComparator(), order);
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
		log.debug("accept song '" + song.getName() + "'");
		this.song = song;
		song.setOriginalKey(useOriginalKey);
		/*
		 * Gather references to the HarmonyChords. ChordProgression by Section is the basis for the analysis.
		 * Make sure everything in the same key. SongManager does this automatically when Song are loaded
		 * as each Harmony has associated HarmonyChords in both the original and transposed key
		 * get() also adds a terminating HarmonyChord at the end (if not null). The default is HarmonyChord.TERMINAL_HARMONY_CHORD
		 */
		ChordProgression chordProgression = null;
		while((chordProgression = song.get()) != null) {
			apply(chordProgression);
		}
		log.debug("collection for '" + song.getName() + "' complete");
	}
	
	@Override
	public MarkovChain<HarmonyChord, ChordProgression, Song> apply(ChordProgression chordProgression) {
		ChordProgression subset = null;
		HarmonyChord nextHarmonyChord = null;
		
		if(chordProgression.size() > order - 1) {
			log.debug("apply: " + chordProgression);
			int numberOfTokens = chordProgression.size();
			int lim = numberOfTokens - order + 1;
			for(int i=0; i<lim; i++) {
				int index = i + order;
				if(index <= numberOfTokens) {		// don't run off the end of the List
					subset = chordProgression.subset(i, index);
					nextHarmonyChord =  (index == numberOfTokens) ? ChordProgression.TERMINAL : chordProgression.get(i+order);
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
		if(trace) { System.out.print(theChordProgression + ":"); }
		if(markovChain.containsKey(theChordProgression)) {
			CollectorStats<HarmonyChord, ChordProgression, Song> collectorStats = markovChain.get(theChordProgression);
			collectorStats.addOccurrence(theHarmonyChord, song);
			collectorStats.setTerminal(terminal);
		}
		else {
			CollectorStats<HarmonyChord, ChordProgression, Song> collectorStats = 
					new CollectorStats<HarmonyChord, ChordProgression, Song>();
			collectorStats.setSubset(theChordProgression);
			collectorStats.addOccurrence(theHarmonyChord, song);
			markovChain.put(theChordProgression, collectorStats);
			collectorStats.setTerminal(terminal);
		}
	}
	

	public int getOrder() {
		return order;
	}

	public void setOrder(int keylen) {
		this.order = keylen;
	}

	public Map<String, IMapped<String>> getSongMap() {
		return songMap;
	}

	public MarkovChain<HarmonyChord, ChordProgression, Song> getMarkovChain() {
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

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

}
