package music.element.song;

import java.util.ArrayList;
import java.util.List;

import music.action.SongAnalyzer;

/**
 * A List<Harmony>. Makes access convenient for song analysis.
 * @author don_bacon
 * @see java.util.ArrayList
 *
 */
public class HarmonyList extends ArrayList<Harmony> {

	private static final long serialVersionUID = -4660183596094095348L;

	public HarmonyList() {
		super();
	}
	
	/**
	 * Adds the entire List<Harmony> from the List<SongMeasure> provided
	 * @param measures List<SongMeasure>
	 * @return true if added successfully
	 * @see java.util.ArrayList.addAll(Collection<? extends E> c)
	 */
	public boolean addAll(List<SongMeasure> measures) {
		boolean ok = true;
		for(SongMeasure measure : measures) {
			ok &= addAll(measure.getHarmony());
		}
		return ok;
	}
	
	/**
	 * 
	 * @param startIndex beginning index to get, inclusive
	 * @param endIndex  end index to get, exclusive
	 * @param transposed if true get the transposed HarmonyChord
	 * @return List<HarmonyChord>
	 */
	public List<HarmonyChord> subset(int startIndex, int endIndex, boolean transposed) {
		List<HarmonyChord> hclist = new ArrayList<HarmonyChord>();
		for(int i=startIndex; i<endIndex; i++) {
			Harmony harmony = this.get(i);
			hclist.add( transposed ? harmony.getTransposedHarmonyChord() : harmony.getHarmonyChord() );
		}
		return hclist;
	}
	
	/**
	 * Concatenates the analysis keys for the designate HarmonyList elements.
	 * Returns a 0-length String if endIndex is out of bounds (i.e. > size() -1 )
	 * @param keyType SongAnalyzer.KeyType
	 * @param startIndex
	 * @param endIndex
	 * @return space-delimited combined key. For example, "G7 C G Dm" 
	 */
	public String getAnalysisKey(SongAnalyzer.KeyType keyType, int startIndex, int endIndex) {
		StringBuffer sb = new StringBuffer();
		if(endIndex < size()-1) {
			for(int i=startIndex; i<=endIndex; i++) {
				sb.append(get(i).getAnalysisKey(keyType));
				sb.append("   ");
			}
		}
		return sb.toString().trim();
	}
}
