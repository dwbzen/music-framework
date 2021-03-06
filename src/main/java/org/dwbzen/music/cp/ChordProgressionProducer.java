package org.dwbzen.music.cp;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.dwbzen.common.cp.CollectorStats;
import org.dwbzen.common.cp.IProducer;
import org.dwbzen.common.cp.MarkovChain;
import org.dwbzen.common.math.OccurrenceProbability;
import org.dwbzen.music.element.song.ChordFormula;
import org.dwbzen.music.element.song.ChordProgression;
import org.dwbzen.music.element.song.ChordProgressionScrapbook;
import org.dwbzen.music.element.song.HarmonyChord;
import org.dwbzen.music.element.song.Song;
import org.dwbzen.util.music.ChordManager;

/**
 * Produces ChordProgressions from the results of HarmonyChordCollector and a starting seed ChordProgression.
 * 
 * @author Don_Bacon
 *
 */
public class ChordProgressionProducer 
				implements IProducer<MarkovChain<HarmonyChord, ChordProgression, Song> , ChordProgression> {

	protected static final Logger log = LogManager.getLogger(ChordProgressionProducer.class);
	public static final String CONFIG_FILENAME = "/config.properties";

	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private ChordProgression seed;
	private ChordProgression nextSeed;
	private ChordProgression originalSeed;
	private int order; 
	private MarkovChain<HarmonyChord, ChordProgression, Song> markovChain = null;
	private int numberToGenerate;		// number to produce
	private boolean reuseSeed = false;
	private boolean statisticalPick = true;
	private int recycleSeedNumber = 1;
	private int recycleSeedCount = 1;	// pick a new seed every recycleSeedNumber iterations
	private boolean sortedResult = false;
	private int minimumLength = 3;	// don't save ChordProgressions with fewer chords than this
	private int maximumLength = 20;	// don't save ChordProgressions with more chords than this
	private int count = 0;
	private boolean trace = false;

	public static ChordProgressionProducer getChordProgressionProducer(int keylen, MarkovChain<HarmonyChord, ChordProgression, Song> cstatsMap, ChordProgression seedProgression) {
		ChordProgressionProducer producer = new ChordProgressionProducer(keylen, cstatsMap);
		if(seedProgression.isEmpty()) {
			seedProgression.addAll(producer.pickSeed());
		}
		producer.setSeed(seedProgression);
		producer.setOriginalSeed(new ChordProgression(seedProgression));
		return producer;
	}
	protected ChordProgressionProducer(int keylen, MarkovChain<HarmonyChord, ChordProgression, Song> cstatsMap) {
		this.order = keylen;
		this.markovChain = cstatsMap;
	}

	/**
	 * Produces a Set<ChordProgression> as a ChordProgressionScrapbook
	 */
	@Override
	public ChordProgressionScrapbook produce(boolean enableDisplay) {
		ChordProgressionScrapbook chordProgressionSet = new ChordProgressionScrapbook(sortedResult);
		nextSeed = seed;
		for(count=0; count<numberToGenerate; ) {
			ChordProgression chordProgression = apply(markovChain);
			if(chordProgression != null && chordProgression.size() >= minimumLength) {
				log.debug(">>> " + count + ". adding: " + chordProgression);
				count++;
				chordProgressionSet.add(chordProgression);
				if(enableDisplay) {
					System.out.println(chordProgression);
				}
			}
			if(reuseSeed) {
				if(++recycleSeedCount <= recycleSeedNumber) {
					nextSeed = seed;
				}
				else {
					seed =  pickSeed();
					nextSeed = seed;
					recycleSeedCount = 0;
				}
			}
			else {
				seed = pickSeed();	// need a new seed for next iteration
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
	public ChordProgression apply(MarkovChain<HarmonyChord, ChordProgression, Song>  cstatsMap) {
		if(trace) {
			log.info("initial seed: " + seed);
		}
		ChordProgression generatedChordProgression = new ChordProgression(seed);	// deep copy constructor
		HarmonyChord nextHarmonyChord = null;
		do {
			nextHarmonyChord = getNextHarmonyChord();	// also sets nextSeed
			if(!nextHarmonyChord.isTerminalOrNull() ) {	// '�'  '�'  end of this ChordProgression
				generatedChordProgression.add(nextHarmonyChord);
				if(trace) {
					log.info("added: "  + nextHarmonyChord + " chordProgression: " + generatedChordProgression);
				}
			}
		} while(!nextHarmonyChord.isTerminalOrNull() && generatedChordProgression.length() < maximumLength);
		return generatedChordProgression;
	}
	
	private HarmonyChord getNextHarmonyChord() {
		HarmonyChord nextChord = null;
		CollectorStats<HarmonyChord, ChordProgression, Song> cstats  = markovChain.get(nextSeed);
		/*
		 * it's impossible that nextSeed does not occur in collectorStatsMap
		 * This would indicate some kind of internal error so throw a RuntimeException
		 */
		if(cstats == null) {
			log.debug("getNextHarmonyChord(): cstats null for nextSeed: " + nextSeed);
			return HarmonyChord.TERMINAL_HARMONY_CHORD;
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
		if(! nextChord.isNullValue() ) {
			nextSeed = new ChordProgression(nextSeed.subset(1), nextChord);
		}
		else {
			nextSeed = pickSeed();
		}

		log.debug(" nextHarmonyChord: " + nextChord);
		if(trace) {		
			log.info("nextSeed now: {" + nextSeed + "}"); 
		}
		
		return nextChord;
	}
	
	private ChordProgression pickSeed() {
		ChordProgression cp =  null; 
		do {
			cp = markovChain.pickSeed();
		} while(cp.length() < order || cp.get(cp.length()-1).isTerminalOrNull());
		
		return cp;
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int keylen) {
		this.order = keylen;
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
	public boolean isTrace() {
		return trace;
	}
	public void setTrace(boolean trace) {
		this.trace = trace;
	}
	
}
