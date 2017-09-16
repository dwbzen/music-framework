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
	
	public ChordInfo(String chordName, String rootNote, String bassNote, String chordSymbol) {
		this.chordName = chordName;
		this.rootNote = rootNote;
		this.bassNote = bassNote;
		this.chordSymbol = chordSymbol;
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
