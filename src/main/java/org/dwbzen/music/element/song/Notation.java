package org.dwbzen.music.element.song;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.Measurable.TieType;
import org.dwbzen.music.element.Measurable.TupletType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provide note type (half, quarter etc. ),  tuplet and tie information.
 * 
 * @author don_bacon
 *
 */
public class Notation implements IJson, Comparable<Notation> {
	
	/**
	 * derived from Duration and time signature: whole, half, quarter, eighth, 16th, 32nd, 64th
	 * TODO - use NoteType enum for noteType and dots
	 */
	@JsonProperty("type")		private String noteType = "";						// will never be null
	@JsonProperty("dots")		private int dots = 0;
	
	@JsonProperty("tieType")	private TieType tieType = TieType.NONE;				// NONE(0), START(1), STOP(2), BOTH(3);
	@JsonProperty("tupletType")	private TupletType tupletType = TupletType.NONE;	// NONE(0), START(1), CONTINUE(2), STOP(3)
	@JsonProperty("tuplet")		private String tuplet = null;		// as in "3/2" etc.

	
	public Notation() {
	}
	
	public Notation(String aNoteType) {
		this.noteType = aNoteType != null ? aNoteType: "" ;
	}

	public String getTuplet() {
		return tuplet;
	}

	public void setTuplet(String tuplet) {
		this.tuplet = tuplet;
	}

	public String getNoteType() {
		return noteType;
	}

	public void setNoteType(String noteType) {
		this.noteType = noteType;
	}

	public TieType getTieType() {
		return tieType;
	}

	public void setTieType(TieType tieType) {
		this.tieType = tieType;
	}

	public TupletType getTupletType() {
		return tupletType;
	}

	public void setTupletType(TupletType tupletType) {
		this.tupletType = tupletType;
	}

	public int getDots() {
		return dots;
	}

	public void setDots(int dots) {
		this.dots = dots;
	}

	@Override
	/**
	 * Compares noteType and dots.
	 */
	public int compareTo(Notation otherNotation) {
		int result = 1;
		if(otherNotation != null) {
			result = noteType.compareTo(otherNotation.noteType);
			if(result == 0) {
				result = dots == otherNotation.dots ? 0 : (dots < otherNotation.dots ? -1 : 1 );
			}
		}
		return result;
	}

}
