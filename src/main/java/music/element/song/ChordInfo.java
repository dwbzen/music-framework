package music.element.song;

/**
 * Structure that combines chord metadata as Strings:
 * root, bass note (which may be different than the root if slash chord),
 * the symbol (as in "7", "m7", "9-5" etc.) and the chord name
 * which omits the slash portion of the chord. So "D9/C" chord name is just "D9"
 * with a bass note "C".
 * 
 * @author don_bacon
 *
 */
public class ChordInfo {

	private String chordName;
	private String rootNote;
	private String bassNote;
	private String chordSymbol;		// the primary symbol
	private String[] chordSymbols;	// if >1 symbol
	private String[] intervals;		// "M3", "m3", "P5" etc
	
	private static final int CHORD_NAME = 0;
	private static final int ROOT_NOTE = 1;
	private static final int BASS_NOTE = 2;
	private static final int SYMBOL = 3;
	
	public ChordInfo(String chordName, String rootNote, String bassNote, String chordSymbol) {
		this.chordName = chordName;
		this.rootNote = rootNote;
		this.bassNote = bassNote;
		this.chordSymbol = chordSymbol;
	}
	
	/**
	 * In the case of slash chords, the chord name will have the / removed
	 * @param chordName for example, "D7-5/C"
	 * @return ChordInfo
	 */
	public static ChordInfo parseChordName(String chordName) {
		String[] result = new String[4];
		boolean isSlash = false;
		result[CHORD_NAME] = chordName;
		result[ROOT_NOTE] = chordName;
		result[BASS_NOTE] = chordName;
		result[SYMBOL] = chordName;
		if(!chordName.equals("0")) {
			int slash = chordName.indexOf("/");
			int len = chordName.length();
			if(slash > 0 ) {
				isSlash = true;
				result[BASS_NOTE] =  chordName.substring(slash + 1);
				result[CHORD_NAME] = chordName.substring(0, slash);
				len = result[CHORD_NAME].length();
			}
			char possibleAccidental = (len == 1) ? '0' : result[CHORD_NAME].charAt(1);
			if(len == 1 ) {		// "C"
				result[SYMBOL] = "M";	// no symbol is major
				if(isSlash) { result[ROOT_NOTE] = result[CHORD_NAME]; }
			}
			else if(len==2) {
				if(possibleAccidental=='b' || possibleAccidental=='#') {	// "F#" or "Eb"
					result[SYMBOL] = "M";
				}
				else {		// "Cm"
					result[SYMBOL] = "" + possibleAccidental;
					result[ROOT_NOTE] = "" + result[CHORD_NAME].charAt(0);
				}
			}
			else {	// Cm7, Ab7b13 etc.
				if(possibleAccidental=='b' || possibleAccidental=='#') {
					result[ROOT_NOTE] = result[CHORD_NAME].substring(0, 2);
					result[SYMBOL] = result[CHORD_NAME].substring(2);
				}
				else {
					result[ROOT_NOTE] = result[CHORD_NAME].substring(0, 1);
					result[SYMBOL] = result[CHORD_NAME].substring(1);
				}
			}
			if(!isSlash) { result[BASS_NOTE] = result[ROOT_NOTE]; }
		}
		return new ChordInfo(result[CHORD_NAME], result[ROOT_NOTE], result[BASS_NOTE], result[SYMBOL]);
	}
	

	public String getChordName() {
		return chordName;
	}
	public void setChordName(String chordName) {
		this.chordName = chordName;
	}
	public String getRootNote() {
		return rootNote;
	}
	public void setRootNote(String rootNote) {
		this.rootNote = rootNote;
	}
	public String getBassNote() {
		return bassNote;
	}
	public void setBassNote(String bassNote) {
		this.bassNote = bassNote;
	}
	public String getChordSymbol() {
		return chordSymbol;
	}
	public void setChordSymbol(String chordSymbol) {
		this.chordSymbol = chordSymbol;
	}

	public String[] getIntervals() {
		return intervals;
	}

	public void setIntervals(String[] intervals) {
		this.intervals = intervals;
	}

	public String[] getChordSymbols() {
		return chordSymbols;
	}

	public void setChordSymbols(String[] chordSymbols) {
		if(chordSymbols != null && chordSymbols.length>0) {
			this.chordSymbol = chordSymbols[0];
		}
		this.chordSymbols = chordSymbols;
	}
	
	
}
