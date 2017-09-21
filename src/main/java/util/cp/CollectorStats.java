package util.cp;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 
 * @author Don_Bacon
 *
 */
public class CollectorStats<K, T extends List<K>> {

	private int subsetLength;
	private T subset;				// List of length subsetLength
	private int totalOccurrance;	// total #times subset occurs
	private Map<K, OccurrenceProbability> occurrenceProbabilityMap = new TreeMap<K, OccurrenceProbability>();
	public static final int LOW = 0;
	public static final int HIGH = 1;
	private boolean terminal = false;	// true if this is a terminal state
	
	public CollectorStats() {
	}
	
	public CollectorStats(int sublen) {
		this.subsetLength = sublen;
	}
	
	public CollectorStats(T sub) {
		setSubset(sub);
	}

	public int getSubsetLength() {
		return subsetLength;
	}

	public void setSubsetLength(int subsetLength) {
		this.subsetLength = subsetLength;
	}
	
	public T getSubset() {
		return subset;
	}

	public void setSubset(T sub) {
		this.subset = sub;
		this.subsetLength = sub.size();
	}

	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}

	public Map<K, OccurrenceProbability> getOccurrenceProbabilityMap() {
		return occurrenceProbabilityMap;
	}
	
	public void addOccurrence(K toccur) {
		if(occurrenceProbabilityMap.containsKey(toccur)) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(toccur);
			op.setOccurrence(op.getOccurrence() + 1);
		}
		else {
			occurrenceProbabilityMap.put(toccur, new OccurrenceProbability(1, 1.0));
		}
		recomputeProbabilitie();
	}

	/**
	 * example: occur		range
	 * 			-----		-----
	 * 			  3			  1,3  (1,2,3 selects first entry)
	 * 			  5			  4,8  (4 - 8 selects the second)
	 * 			  1			  9,9  (9 selects the third)
	 * 			  2			10,11   etc.
	 */
	private void recomputeProbabilitie() {
		totalOccurrance = 0;
		Collection<OccurrenceProbability> opcollection = occurrenceProbabilityMap.values();
		for(OccurrenceProbability op : opcollection) {
			totalOccurrance+= op.getOccurrence();
		}
		Set<K> keyset = occurrenceProbabilityMap.keySet(); 
		int[] prevRange = null;
		for(K key : keyset) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(key);
			int occur = op.getOccurrence();
			if(totalOccurrance > 0) {
				op.setProbability(((double)op.getOccurrence()) / ((double)totalOccurrance));
			}
			if(prevRange == null) {
				op.setRange(LOW, 1);
				op.setRange(HIGH, occur);
			}
			else {
				op.setRange(LOW, prevRange[HIGH] + 1);
				op.setRange(HIGH, prevRange[HIGH] + occur);
			}
			prevRange = op.getRange();
		}
	}
	
	public int size() {
		return occurrenceProbabilityMap.size();
	}
	
	public int getTotalOccurrance() {
		return totalOccurrance;
	}

	public void setTotalOccurrance(int totalOccurrance) {
		this.totalOccurrance = totalOccurrance;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(K key : occurrenceProbabilityMap.keySet()) {
			OccurrenceProbability op = occurrenceProbabilityMap.get(key);
			int[] rng = op.getRange();
			sb.append("   '" + key.toString() + "'\t" + op.getOccurrence() + 
					"\t" + rng[0] + "," + rng[1] +
					"\t" + op.getProbability());
			sb.append("\n");
		}
		return sb.toString();
	}

}

