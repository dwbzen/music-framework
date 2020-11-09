package org.dwbzen.music.element;
import org.dwbzen.common.math.IPoint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Musical structure that can be added to a Measure.
 * This can be a Note or a Chord.
 * If generated from IPoint data, the reference to the IPoint is saved.
 * @author don_bacon
 *
 */
public abstract class Measurable implements Cloneable {
	
	/**
	 * note or chord
	 */
	@JsonProperty	private String type;
	
	/**
	 * derived from Duration.BeatUnitNames: whole, half, quarter etc.
	 */
	@JsonProperty("note-type")	protected String noteType = null;
	/**
	 * in pre-determined units defined by Measure, must be >0
	 * Includes ratio for triplets, and grace note indication
	 */
	@JsonProperty 				protected Duration duration;
	@JsonProperty("voice")		private int voice = 1;
	@JsonProperty("staff")		private int staff = 1;
	@JsonProperty("tieType")	protected TieType tieType = TieType.NONE;	// Chords and Notes can be tied together
	@JsonProperty("tupletType")	protected TupletType tupletType = TupletType.NONE;
	@JsonProperty				private Dynamics dynamics = null;			// optional Dynamics applies to object only
	@JsonProperty("point")		private IPoint point = null;				// optional generating point
	@JsonProperty				private String name = null;		// optional name

	public static enum TieType {
		NONE(0), START(1), STOP(2), BOTH(3), start(1), stop(2), both(3);
		TieType(int val) { this.value = val;}
		private final int value;
		public int value() { return value; }
	}
	public static enum TupletType {
		NONE(0), START(1), CONTINUE(2), STOP(3);
		TupletType(int val)  { this.value = val;}
		private final int value;
		public int value() { return value; }
	}
	
	public static final String CHORD = "chord";
	public static final String NOTE = "note";
	public static final String RHYTHM = "rhythm";
	
	protected Measurable() {
		setMeasurableType();
	}

	protected abstract void setMeasurableType();
	
	public abstract Measurable clone();
	
	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration dur) {
		this.duration = dur;
	}

	public int getVoice() {
		return voice;
	}

	public void setVoice(int voice) {
		this.voice = voice;
	}

	public int getStaff() {
		return staff;
	}

	public void setStaff(int staff) {
		this.staff = staff;
	}

	public TieType getTieType() {
		return tieType;
	}

	public void setTieType(TieType tieType) {
		this.tieType = tieType;
	}

	public Dynamics getDynamics() {
		return dynamics;
	}

	public void setDynamics(Dynamics dynamics) {
		this.dynamics = dynamics;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNoteType() {
		return noteType;
	}

	public void setNoteType(String noteType) {
		this.noteType = noteType;
	}

	public TupletType getTupletType() {
		return tupletType;
	}

	public void setTupletType(TupletType tupletType) {
		this.tupletType = tupletType;
	}

	public IPoint getPoint() {
		return point;
	}

	public void setPoint(IPoint point) {
		this.point = point;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

}
