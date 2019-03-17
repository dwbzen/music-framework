package org.dwbzen.music.instrument.guitar;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dwbzen.music.element.song.HarmonyChord;

/**
 * Encapsulates guitar chord diagram(s) for a given chord.
 * G7 for example has these formations:
 *	 "G7":[
			{"p":"3,2,0,0,0,1","f":"321"},
			{"p":"3,5,3,4,3,3","f":"131211"},
			{"p":"x,10,12,10,12,10","f":"12131;13141"},
			{"p":"x,x,5,7,6,7","f":"1324"},
			{"p":"x,10,9,10,8,x","f":"3241"},
			{"p":"3,x,3,4,3,x","f":"1243"}
		],
 * 
 * A GuitarChord has an associated HarmonyChord, which provides the name and the notes, and a number of chord formations.
 * Each chord formations has positions (on the fret board) and one or more fingerings.
 * Using a standard guitar tab notation, {"p":"3,5,3,4,3,3","f":"131211"} looks like this:
	e||----3-----------------|
	B||----3-----------------|
	G||----4-----------------|
	D||----3-----------------|
	A||----5-----------------|
	E||----3-----------------|
 *
 * "x" indicates omitted string, 0 indicates open string.
 * Note that in this notation, the "p" positions list read left to right as bottom string to top string.
 * Fingering typically isn't shown on a tab and is an optional attribute.
 * 
 * A List<ChordFormation> holds the chord formations
 * 
 * @author don_bacon
 *
 */
public class GuitarChord {

	@JsonProperty("chord")		private HarmonyChord harmonyChord = null;
	@JsonProperty("tuning")		private GuitarTuning guitarTuning = null;
	@JsonProperty("formations")	private List<ChordFormation> chordFormations = new ArrayList<ChordFormation>();
	
	public GuitarChord() {
	}
	
	public GuitarChord(HarmonyChord hc) {
		harmonyChord = hc;
		guitarTuning = GuitarTuning.STANDARD;
	}

	public HarmonyChord getHarmonyChord() {
		return harmonyChord;
	}

	public void setHarmonyChord(HarmonyChord harmonyChord) {
		this.harmonyChord = harmonyChord;
	}

	public GuitarTuning getGuitarTuning() {
		return guitarTuning;
	}

	public void setGuitarTuning(GuitarTuning guitarTuning) {
		this.guitarTuning = guitarTuning;
	}
	
}

