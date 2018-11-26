package music.element.song;

import music.action.SongAnalyzer;
import music.action.SongAnalyzer.KeyType;
import music.element.Key;
import music.element.Pitch;
import music.element.Scales;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;
import mathlib.util.INameable;

/**
 * A simplified Harmony Chord. Has the bare minimum of information  - the chord name and a beat
 * Under the hood there's associated HarmonyChords in original and transposed keys.
 * For example, "harmony" : [ { "chord" : "Bb", "beat" : 1 },  { "chord" : "Gm", "beat" : 2 } ]
 * A Harmony of "0" is used to indicate no chord sounded.
 * @author don_bacon
 *
 */
public class Harmony implements IJson, INameable {

	private static final long serialVersionUID = 3702027300515217295L;
	public static String NO_HARMONY_KEY = "NONE";	// name is "0"
	
	@JsonProperty	int beat;
	@JsonProperty	int division = 0;	// The division# after the beat the chord is played
	/**
	 * The name of the chord as it would appear in ChordFormula symbol. For example, "Bb" is Bb Major chord.
	 * A triangle (unicode 0394 Δ ) is sometimes used for major instead of M.
	 * So Fm(M7) would be Fm(Δ7). Unfortunately this is not convenient to enter as pure text.
	 * Lacking an "M" or "m" major is implied. So "Bb" symbol would be "BbM" or "BbΔ"
	 * Other examples, "Cm(M7)" , "C7-13" etc.
	 * Use "0" for silence (rest)
	 * Slash chords like "A7/G" are split up. The name is the chord including the slash (A7/G in this case)
	 * but the bassNote is saved as a Transient Pitch.
	 */
	@JsonProperty("chord")	String name;
	@JsonIgnore	HarmonyChord harmonyChord = null;
	@JsonIgnore	HarmonyChord transposedHarmonyChord = null;		// optional - used by SongAnalyzer, SongCollector
	@JsonIgnore	Key	originalKey = null;				// optional - will be non-null if transposedKey is set
	@JsonIgnore	Key transposedKey = null;			// optional - used by SongAnalyzer, SongCollector
	@JsonIgnore	SongMeasure songMeasure = null;		// parent SongMeasure
	@JsonIgnore	Pitch bassNote;						// for slash chords, but set to the root otherwise.
	@JsonIgnore	boolean isSlash = false;			// true if a slash chord

	public Harmony() {
	}
	
	public Harmony(String name, int beat) {
		setName(name);
		this.beat = beat;
	}
	
	public String getAnalysisKey(SongAnalyzer.KeyType keyType) {
		String key = null;
		if(keyType.equals(KeyType.FORMULA)) {
			key = (harmonyChord != null) ? harmonyChord.getChordFormula().getSymbol() : NO_HARMONY_KEY;
		}
		else if(keyType.equals(KeyType.FORMULA_INTERVAL)) {
			if(harmonyChord != null && originalKey != null) {
				int step = harmonyChord.getRoot().getChromaticScaleDegree(originalKey);
				key = Scales.getScaleDegreeFromChromaticStep(originalKey, step) +  
							"_" + harmonyChord.getChordFormula().getSymbol();
			}
			else {
				key = NO_HARMONY_KEY;
			}
		}
		else if(keyType.equals(KeyType.RELATIVE_HARMONY_CHORD)) {
			key = (transposedHarmonyChord != null) ? transposedHarmonyChord.getName() : NO_HARMONY_KEY;
		}
		else if(keyType.equals(KeyType.ABSOLUTE_HARMONY_CHORD)) {
			key = name;
		}
		return key;
	}
	
	public int getBeat() {
		return beat;
	}

	public void setBeat(int beat) {
		this.beat = beat;
	}

	public String getName() {
		if(this.bassNote == null) {
			setBassNote();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		setBassNote();
	}

	private void setBassNote() {
		if(name.equals("0")) {
			// A rest - doesn't matter what the bass note is
			bassNote = Pitch.A;
		}
		else {
			ChordInfo chordInfo = ChordInfo.parseChordName(name);
			bassNote = new Pitch(chordInfo.getBassNote());
			isSlash = !chordInfo.getRootNote().equals(chordInfo.getBassNote());
		}
	}

	public Pitch getBassNote() {
		if(this.bassNote == null) {
			setBassNote();
		}
		return bassNote;
	}
	
	public HarmonyChord getHarmonyChord() {
		return harmonyChord;
	}

	public void setHarmonyChord(HarmonyChord harmonyChord) {
		this.harmonyChord = harmonyChord;
	}

	public HarmonyChord getTransposedHarmonyChord() {
		return transposedHarmonyChord;
	}

	public void setTransposedHarmonyChord(HarmonyChord transposedHarmonyChord) {
		this.transposedHarmonyChord = transposedHarmonyChord;
	}

	public Key getTransposedKey() {
		return transposedKey;
	}

	public void setTransposedKey(Key transposedKey) {
		this.transposedKey = transposedKey;
	}

	public SongMeasure getSongMeasure() {
		return songMeasure;
	}

	public void setSongMeasure(SongMeasure songMeasure) {
		this.songMeasure = songMeasure;
	}

	public Key getOriginalKey() {
		return originalKey;
	}

	public void setOriginalKey(Key originalKey) {
		this.originalKey = originalKey;
	}
	
	public int getDivision() {
		return division;
	}

	public void setDivision(int division) {
		this.division = division;
	}

	public boolean isSlash() {
		return isSlash;
	}

	public void setSlash(boolean isSlash) {
		this.isSlash = isSlash;
	}

	@Override
	public String toString() {
		return getName();
	}

}
