package music.element.song;

import java.util.Comparator;

public class ChordProgressionComparator implements Comparator<ChordProgression> {

	@Override
	public int compare(ChordProgression cp1, ChordProgression cp2) {
	
		return cp1.toString().compareTo(cp2.toString());
	}

}
