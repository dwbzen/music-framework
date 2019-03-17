package org.dwbzen.music.element.song;

import org.dwbzen.music.element.Measurable.TieType;
import org.dwbzen.music.element.Measurable.TupletType;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;

/**
 * Provide note type (half, quarter etc. ),  tuplet and tie information.
 * 
 * @author don_bacon
 *
 */
public class Notation implements IJson {

	private static final long serialVersionUID = -3719234783577601054L;
	
	/**
	 * derived from Duration and time signature: whole, half, quarter, eighth, 16th, 32nd, 64th
	 */
	@JsonProperty("type")		private String noteType = null;
	@JsonProperty("tieType")	private TieType tieType = null;			// NONE(0), START(1), STOP(2), BOTH(3);
	@JsonProperty("tupletType")	private TupletType tupletType = null;	// NONE(0), START(1), CONTINUE(2), STOP(3)
	@JsonProperty("dots")		private int dots = 0;
	@JsonProperty("tuplet")		private String tuplet = null;		// as in "3/2" etc.

	
	public Notation() {
	}
	
	public Notation(String noteType) {
		this.noteType = noteType;
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

}
