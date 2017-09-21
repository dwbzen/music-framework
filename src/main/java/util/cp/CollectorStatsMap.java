package util.cp;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

/**
 * A TreeMap bound to a base class K and some class T that is a List<K>
 * For example, K : HarmonyChord, T : ChordProgression (which implements List<HarmonyChord>
 * K : Character, T : Word (implements List<Character>)
 * K : Word, T : Sentence
 * A Comparator must be provided so it knows how to order the elements in the TreeMap.
 * @author don_bacon
 *
 * @param <K> a base class
 * @param <T> class that implements List<K>
 */
public class CollectorStatsMap<K, T extends List<K>> extends TreeMap<T, CollectorStats<K, T>> {

	private static final long serialVersionUID = 4801227327750662977L;
	private Map<T, Integer> summaryMap = null;
	protected static final org.apache.log4j.Logger log = Logger.getLogger(CollectorStatsMap.class);

	/**
	 * Creates a CollectorStatsMap with a given Comparator.
	 * @param comparator Comparator to use to order the map, if null natural ordering is used.
	 */
	public CollectorStatsMap(Comparator<? super T> comparator) {
		super(comparator);
	}
	
	public T pickSeed() {
		T seed = null;
		CollectorStats<K,T> cstats = null;
		do {
			seed = pickCandidateSeed();
			cstats = get(seed);
		} while(cstats.isTerminal());
		log.debug("picked seed: '" + seed + "'");
		return seed;
	}
	
	/**
	 * Selects a random T seed
	 * @return
	 */
	public T pickCandidateSeed() {
		T seed = null;
		Set<T> keys = keySet();
		Iterator<T>   keysIt = keys.iterator();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int totalAll = keys.size();
		int randomIndex = random.nextInt(0, totalAll);
		int i = 0;
		while(keysIt.hasNext() && randomIndex >= i++) {
			seed = keysIt.next();
		}
		//log.debug("picked candidate seed: '" + seed + "'");
		return seed;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(T key : this.keySet()) {
			CollectorStats<K, T> cstats = this.get(key);
			sb.append("'" + key.toString() + "'\t" + cstats.getTotalOccurrance());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void display() {
		for(T key : this.keySet()) {
			CollectorStats<K, T> cstats = this.get(key);
			System.out.println("'" + key.toString() + "'\t" + cstats.getTotalOccurrance());
			System.out.print(cstats.toString());
		}
	}

	public Map<T, Integer> getSummaryMap() {
		if(summaryMap == null) {
			summaryMap = new TreeMap<T, Integer>();
		}
		for(T key : this.keySet()) {
			CollectorStats<K, T> cstats = this.get(key);
			summaryMap.put(key, cstats.getTotalOccurrance());
		}
		return summaryMap;
	}
	
	public void displaySummaryMap() {
		getSummaryMap();
		for(T key : summaryMap.keySet()) {
			Integer count = summaryMap.get(key);
			System.out.println("'" + key + "'\t" + count);
		}
	}
}
