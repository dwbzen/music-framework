package music.element;
import mathlib.IPoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Musical structure that can be added to a Measure.
 * This can be a Note or a Chord.
 * If generated from IPoint data, the reference to the IPoint is saved.
 * @author don_bacon
 *
 */
public abstract class Measurable {
	
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
	@JsonProperty("tieType")	protected TieType tieType = TieType.NONE;	// Chords and Notes can be tied together
	@JsonProperty("tupletType")	protected TupletType tupletType = TupletType.NONE;
	@JsonProperty				private Dynamics dynamics = null;			// optional Dynamics applies to object only
	@JsonProperty("point")		private IPoint point = null;				// optional generating point
	@JsonIgnore					protected Integer seq_id = 0;		// a global sequence id starting at 1 (0 = unassigned)
	@JsonProperty				private String name = null;		// optional name
	/**
	 * starting at 1 indicates the order this Measurable added to some container
	 */
	@JsonProperty("ordinal")	protected int ordinal = 0;
	/**
	 * Reference to container if any - a Chord for example
	 */
	

	private static Integer nextSeqId = 1;

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
		this.seq_id = getNextSeqId();
		setMeasurableType();
	}

	protected abstract void setMeasurableType();
	
	protected static Integer getNextSeqId() {
		synchronized(Measurable.class) {
			return nextSeqId++;
		}
	}
	
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

	public Integer getSeq_id() {
		return seq_id;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

}
