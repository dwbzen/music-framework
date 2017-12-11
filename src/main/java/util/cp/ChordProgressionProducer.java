package util.cp;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.cp.CollectorStats;
import mathlib.cp.IProducer;
import mathlib.cp.MarkovChain;
import mathlib.cp.OccurrenceProbability;
import music.element.song.ChordFormula;
import util.music.ChordManager;
import music.element.song.ChordProgression;
import music.element.song.ChordProgressionScrapbook;
import music.element.song.HarmonyChord;
import music.element.song.Song;
import util.music.SongManager;
import music.element.song.Songbook;

/**
 * Produces ChordProgressions from the results of HarmonyChordCollector and a starting seed ChordProgression.
 * 
 * @author Don_Bacon
 *
 */
public class ChordProgressionProducer 
				implements IProducer<MarkovChain<HarmonyChord, ChordProgression> , ChordProgression> {

	protected static final Logger log = LogManager.getLogger(ChordProgressionProducer.class);
	public static final String CONFIG_FILENAME = "/config.properties";

	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private ChordProgression seed;
	private ChordProgression nextSeed;
	private ChordProgression originalSeed;
	private int keylen; 
	private MarkovChain<HarmonyChord, ChordProgression> markovChain = null;
	private int numberToGenerate;		// number to produce
	private boolean reuseSeed = false;
	private boolean statisticalPick = true;
	private int recycleSeedNumber = 1;
	private int recycleSeedCount = 1;	// pick a new seed every recycleSeedNumber iterations
	private boolean sortedResult = false;
	private int minimumLength = 3;	// don't save ChordProgressions with fewer chords than this
	private int maximumLength = 20;	// don't save ChordProgressions with more chords than this
	private int count = 0;

	public static ChordProgressionProducer getChordProgressionProducer(int keylen, MarkovChain<HarmonyChord, ChordProgression> cstatsMap, ChordProgression seedProgression) {
		ChordProgressionProducer producer = new ChordProgressionProducer(keylen, cstatsMap);
		producer.setSeed(seedProgression);
		producer.setOriginalSeed(new ChordProgression(seedProgression));
		return producer;
	}
	protected ChordProgressionProducer(int keylen, MarkovChain<HarmonyChord, ChordProgression> cstatsMap) {
		this.keylen = keylen;
		this.markovChain = cstatsMap;
	}

	/**
	 * Produces a Set<ChordProgression> as a ChordProgressionScrapbook
	 */
	@Override
	public ChordProgressionScrapbook produce() {
		ChordProgressionScrapbook chordProgressionSet = new ChordProgressionScrapbook(sortedResult);
		nextSeed = seed;
		for(count=0; count<numberToGenerate; ) {
			ChordProgression chordProgression = apply(markovChain);
			if(chordProgression != null && chordProgression.size() >= minimumLength) {
				log.debug(">>> " + count + ". adding: " + chordProgression);
				count++;
				chordProgressionSet.add(chordProgression);
			}
			if(reuseSeed) {
				if(++recycleSeedCount <= recycleSeedNumber) {
					nextSeed = seed;
				}
				else {
					seed =  markovChain.pickSeed();
					nextSeed = seed;
					recycleSeedCount = 0;
				}
			}
			else {
				seed = markovChain.pickSeed();	// need a new seed for next iteration
				nextSeed = seed;
			}
		}
		return chordProgressionSet;
	}
 
	/**
	 * @param markovChain the Map<ChordProgression, MarkovChain<HarmonyChord, ChordProgression>> result
	 * 		  of HarmonyChordCollector
	 * @return ChordProgression result
	 * TODO: loop detection. Save the initial starting seed and compare to each nextSeed generated
	 * TODO: return when nextSeed == initial starting seed at any point AND there is only a single entry
	 * 		 in cstats for that key
	 */
	@Override
	public ChordProgression apply(MarkovChain<HarmonyChord, ChordProgression>  cstatsMap) {
		ChordProgression generatedChordProgression = new ChordProgression(seed);	// deep copy constructor
		HarmonyChord nextHarmonyChord = null;
		do {
			nextHarmonyChord = getNextHarmonyChord();	// also sets nextSeed
			if(!nextHarmonyChord.equals(ChordProgression.TERMINAL)) {	// end of this ChordProgression
				generatedChordProgression.add(nextHarmonyChord);
				log.debug("added: "  + nextHarmonyChord);
			}
		} while(!nextHarmonyChord.equals(ChordProgression.TERMINAL) && generatedChordProgression.length() < maximumLength);
		return generatedChordProgression;
	}
	
	private HarmonyChord getNextHarmonyChord() {
		HarmonyChord nextChord = null;
		CollectorStats<HarmonyChord, ChordProgression> cstats  = markovChain.get(nextSeed);
		/*
		 * it's impossible that nextSeed does not occur in collectorStatsMap
		 * This would indicate some kind of internal error so throw a RuntimeException
		 */
		if(cstats == null) {
			throw new RuntimeException("getNextHarmonyChord(): cstats null for nextSeed: " + nextSeed);
		}
		int occur = cstats.getTotalOccurrance();
		int keysize = cstats.size();
		int spick = random.nextInt(1, occur+1);
		int npick = random.nextInt(0, keysize);
		int i = 0;
		Map<HarmonyChord, OccurrenceProbability> occurrenceProbabilityMap = cstats.getOccurrenceProbabilityMap();
		for(HarmonyChord hc :  occurrenceProbabilityMap.keySet()) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(hc);
			int[] range = op.getRange();
			if(statisticalPick) {
				if(spick >= range[CollectorStats.LOW] && spick <= range[CollectorStats.HIGH]) {
					nextChord = hc;
					break;
				}
			}
			else if(npick == i++){
				nextChord = hc;
				break;
			}
		}
		// set the nextSeed which is an instance variable
		nextSeed = new ChordProgression(nextSeed.subset(1), nextChord);
		log.debug(" nextHarmonyChord: " + nextChord);
		log.debug("nextSeed now: {" + nextSeed + "}");
		return nextChord;
	}


	/**
	 * Create a ChordProgression from String representations of chord names.
	 * @param chordFormulas a Map<String, ChordFormula> of ChordFormulas
	 * @param seedString chord names delimited by space. For example, "C#7 Bbm7" or "D E7"
	 * @return ChordProgression created from seed string.
	 */
	public static ChordProgression createSeedChordProgression(Map<String, ChordFormula> chordFormulas, String seedString) {
		ChordProgression cp = new ChordProgression();
		String[] seeds = seedString.split(" ");
		for(int i=0; i<seeds.length; i++) {
			HarmonyChord hc = ChordManager.createHarmonyChord(seeds[i], chordFormulas);
			cp.add(hc);
		}
		return cp;
	}

	public int getKeylen() {
		return keylen;
	}

	public void setKeylen(int keylen) {
		this.keylen = keylen;
	}

	public ChordProgression getSeed() {
		return seed;
	}

	public void setSeed(ChordProgression seed) {
		this.seed = seed;
	}

	public ChordProgression getNextSeed() {
		return nextSeed;
	}

	public void setNextSeed(ChordProgression nextSeed) {
		this.nextSeed = nextSeed;
	}

	public void setOriginalSeed(ChordProgression originalSeed) {
		this.originalSeed = originalSeed;
	}
	public int getNumberToGenerate() {
		return numberToGenerate;
	}

	public void setNumberToGenerate(int numberOfProgressions) {
		this.numberToGenerate = numberOfProgressions;
	}

	public boolean isSortedResult() {
		return sortedResult;
	}

	public void setSortedResult(boolean sortedResult) {
		this.sortedResult = sortedResult;
	}
	
	public boolean isStatisticalPick() {
		return statisticalPick;
	}

	public void setStatisticalPick(boolean statisticalPick) {
		this.statisticalPick = statisticalPick;
	}

	public int getRecycleSeedNumber() {
		return recycleSeedNumber;
	}

	public void setRecycleSeedNumber(int recycleSeedNumber) {
		this.recycleSeedNumber = recycleSeedNumber;
	}
	
	public ChordProgression getOriginalSeed() {
		return originalSeed;
	}
	public boolean isReuseSeed() {
		return reuseSeed;
	}

	public void setReuseSeed(boolean reuseSeed) {
		this.reuseSeed = reuseSeed;
	}

	public int getMinimumLength() {
		return minimumLength;
	}

	public void setMinimumLength(int minimumLength) {
		this.minimumLength = minimumLength;
	}

	public int getRecycleSeedCount() {
		return recycleSeedCount;
	}
	public void setRecycleSeedCount(int recycleSeedCount) {
		this.recycleSeedCount = recycleSeedCount;
	}
	public int getMaximumLength() {
		return maximumLength;
	}
	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}
	/**
	 * -seed : the chord names to use as a seed for production. For example, "G7 C".
	 * 		   The #chords must equal keylen specified.
	 * @param args
	 */
	public static void main(String...args) {
		
		String songInputFile = null;			// complete path to .JSON Song file TODO
		String songCollectionName = null;
		String chordFormulaCollectionName = "chord_formulas";
		String query = null;
		int num = 5;
		int keylen = 2;
		boolean sort = false;
		boolean reuse = false;
		boolean statistical = true;
		int recycleSeedNumber = 10;
		String seedString = null;		// specify as Chord names as in "C7 F" for example
		int repeats = 1;
		boolean useOriginalKey = false;
		boolean debug = false;
		int minlength = 0;
		int maxlength = 0;

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
			else if(args[i].equalsIgnoreCase("-num")) {
				num = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-sort")) {
				sort = true;
			}
			else if(args[i].equalsIgnoreCase("-debug")) {
				debug = true;
			}
			else if(args[i].equalsIgnoreCase("-repeat")) {
				repeats = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-minLength")) {
				minlength = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-maxLength")) {
				maxlength = Integer.parseInt(args[++i]);
			}
			else if(args[i].startsWith("-recycle")) {
				recycleSeedNumber = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-seed")) {
				seedString = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-reuse")) {
				reuse = true;
			}
			else if(args[i].startsWith("-original")) {	// as in originalKey
				useOriginalKey = true;
			}
		}
		
		if(debug) {
			HarmonyChord.setIncludeSpellingInToString(false);	// set to true if you want to see the spelling of each chord
		}
		SongManager songMgr = new SongManager(songCollectionName, songInputFile, query);
		songMgr.loadSongs();
		Songbook songbook = songMgr.getSongbook();

		Map<String, ChordFormula> chordFormulas = songMgr.getChordFormulas();
		HarmonyChordCollector collector = new HarmonyChordCollector(keylen);
		collector.setUseOriginalKey(useOriginalKey);
		for(Song song : songbook) {
			collector.accept(song);
		}
		MarkovChain<HarmonyChord, ChordProgression> markovChain = collector.getMarkovChain();
		
		ChordProgression seedProgression = null;
		if(seedString != null) {
			/*
			 * Create a ChordProgression from the seed on the command line
			 */
			seedProgression = createSeedChordProgression(chordFormulas, seedString);
		}
		else {
			seedProgression = markovChain.pickSeed();
		}
		log.debug("seed ChordProgression: " + seedProgression.toString());
		
		ChordProgressionProducer chordProgressionProducer = ChordProgressionProducer.getChordProgressionProducer(keylen, markovChain, seedProgression);
		chordProgressionProducer.setNumberToGenerate(num);
		chordProgressionProducer.setSortedResult(sort);
		chordProgressionProducer.setStatisticalPick(statistical);
		chordProgressionProducer.setRecycleSeedNumber(recycleSeedNumber);
		chordProgressionProducer.setReuseSeed(reuse);
		if(minlength > 0) {
			chordProgressionProducer.setMinimumLength(minlength);
		}
		if(maxlength > 0) {
			chordProgressionProducer.setMaximumLength(maxlength);
		}

		for(int nr=1; nr<=repeats; nr++) {
			ChordProgressionScrapbook chordProgressionSet = chordProgressionProducer.produce();
			if(chordProgressionSet != null) {
				for(ChordProgression cp : chordProgressionSet) {
					System.out.println(cp);
				}
			}
		}
		
	}
}
