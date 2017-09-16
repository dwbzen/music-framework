package music.element.song;

import java.util.Comparator;

public class HarmonyChordComparator implements Comparator<HarmonyChord> {

	@Override
	/**
	 * Compares 2 HarmonyChords. harmonyChord1.compare(harmonyChord2) is defined as
	 *  harmonyChord1.getName().compare(harmonyChord2.getName())
	 * @return compare of respective HarmonyChord names.
	 */
	public int compare(HarmonyChord hc1, HarmonyChord hc2) {
		int result = 0;
		if(hc1 != null && hc2 == null) {
			result = -1;
		}
		else if(hc2 != null && hc1 == null) {
			result = 1;
		}
		else {
			result = hc1.getName().compareTo(hc2.getName());
		}
		return result;
	}

}
