package music.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import music.ScorePart;
import mathlib.util.IJson;

/**
 * A Measure is a horizontal arrangement of Note and/or Chord : i.e. a Measurable
 * A new Measure without any parameters has defaults of 24 divisions,
 * time signature of 3/4 (3 beats/measure, quarter note has the beat),
 * key of C - which determines the key signature,
 * moderato Tempo (quarter note = 90 metronome mark),
 * G-cleff in the ususal place, and mf Dynamic.
 * 
 * A Measure cannot exist by itself - it must be part of an IMeasureContainer,
 * a ScorePart for example.
 * 
 * Once created, a Measure is built by Consuming instances of Measurable (Notes, Chords).
 * 
 * @author don_bacon
 *
 */
public class Measure implements IJson, Consumer<Measurable> {

	private static final long serialVersionUID = -704281345619181452L;
	public static final int DEFAULT_DIVISIONS_PER_MEASURE = 16;
	private static int divisionsPerMeasure = DEFAULT_DIVISIONS_PER_MEASURE;		// default value

	/**
	 * Number of basic units in the measure. Must be >0
	 */
	@JsonProperty("divisions")	private int divisions;
	@JsonProperty				private Key key = Key.C_MAJOR;	// sensible default
	/**
	 * Time signature beats per measure
	 */
	@JsonProperty("beats")		private int beats = 4;	// beats per measure. divisions per beat = divisions/beats, 24/3 = 8 for example
	/**
	 * Time signature beat note (1=whole, 2=half, 4 = quaver, 8 = semiquaver etc.)
	 * SO time signature is beats/beatNote: 3/4, 6/8, whatever
	 */
	@JsonProperty("beatNote")	private int beatNote = 4;		// defaults to quarterNote
	@JsonProperty				private Dynamics dynamics = new Dynamics();		// defaults to mf
	@JsonProperty				private Tempo tempo = new Tempo();	// defaults to 90 (Moderato)
	/**
	 * true if the tempo changes on this measure (from the previous)
	 */
	@JsonProperty("tempoChange")	private boolean tempoChange = false;
	@JsonProperty("keyChange")		private boolean keyChange = false;
	
	/**
	 * Refers the the previous measure if there is one or null if not
	 */
	@JsonIgnore	private Measure lastMeasure = null;
	
	/**
	 * Refers to the next measure if there is one, or null if not
	 */
	@JsonIgnore	private Measure nextMeasure = null;
	@JsonIgnore	private ScorePart scorePart = null;

	/**
	 * Measure number, starts at 1
	 */
	@JsonProperty("number")		private int number = 1;
	@JsonProperty	private Label label;
	
	@JsonProperty	private List<Label> clefs = new ArrayList<Label>();
	@JsonProperty	private List<Measurable> measureables = new ArrayList<Measurable>();	// notes & chords in this measure
	
	
	protected Measure() {
	}
		
	protected Measure(int divisions) {
		this.divisions = divisions;
		clefs.add(new Label(4, "G"));	// G-clef on line 4 of the staff
	}
	
	/**
	 * Creates a new Measure, copying the divisions and tempo from the previous measure
	 * and incrementing the measure number.
	 * @param prev the previous measure.
	 */
	protected Measure(Measure prev) {
		this(prev.getDivisions());
		tempo = prev.getTempo();
		number = prev.getNumber() + 1;
		tempoChange = false;
		prev.setNextMeasure(this);
		key = prev.key;
		scorePart =  prev.getScorePart();
	}
	
	protected Measure(int divisions, Tempo tp, Measure prev) {
		this(divisions);
		this.tempo = tp;
		if(prev != null) {
			this.lastMeasure = prev;
			Tempo pt = (tp==null) ? prev.getTempo() : tp;
			if(!pt.equals(this.tempo)) {
				tempoChange = true;
			}
			this.number = prev.getNumber() + 1;
			prev.setNextMeasure(this);
			this.key = prev.key;
		}
	}
	
	public static Measure createInstance(ScorePart scorePart) {
		Measure m = new Measure(scorePart.getInstrument().getRhythmScale().getRoot());
		m.setScorePart(scorePart);
		m.setKey(scorePart.getScoreKey());
		return m;
	}
	
	public static Measure createInstance(Measure previousMeasure) {
		Measure newMeasure = new Measure(previousMeasure);
		return newMeasure;
	}
	
	public Stream<Measurable> stream() {
		return measureables.stream();
	}
	
	/**
	 * Removes the Measurable at a given index and replaces
	 * with a List<Measurable> supplied.
	 * 
	 * @param notes List<Measurable> (Note or Chord) to insert
	 * @param index insertion index
	 * @return the index of the last note added in the measure
	 */
	public int insert(List<? extends Measurable> notes, int index) {
		int ind = index-1;
		measureables.remove(index);
		for(Measurable note : notes) {
			measureables.add(++ind, note);
		}
		return ind;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("Measure " + number + " time signature: " + beats + "/" + beatNote);
		for(Measurable m : measureables) {
			sb.append("\n " + m.toString());
		}
		return sb.toString();
	}
	
	public int getDivisions() {
		return divisions;
	}

	public void setDivisions(int divisions) {
		this.divisions = divisions;
	}
	
	public int getDivisionPerBeat() {
		return  divisions/beats;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public int getBeats() {
		return beats;
	}

	public void setBeats(int beats) {
		this.beats = beats;
	}

	public int getBeatNote() {
		return beatNote;
	}
	
	public Integer getBeatNoteInteger() {
		return Integer.valueOf(beatNote);
	}

	public void setBeatNote(int beatNote) {
		this.beatNote = beatNote;
	}
	public void setTimeSignature(int[] ts) {
		beats = ts[0];
		beatNote = ts[1];
	}
	public int[] getTimeSignature() {
		int[] ts = new int[2];
		ts[0] = beats;
		ts[1] = beatNote;
		return ts;
	}
	
	public Dynamics getDynamics() {
		return dynamics;
	}

	public void setDynamics(Dynamics dynamics) {
		this.dynamics = dynamics;
	}

	public List<Label> getClefs() {
		return clefs;
	}

	public void setClefs(List<Label> clefs) {
		this.clefs = clefs;
	}

	/**
	 * The clef number is the index + 1 of clefs List
	 * @param clefType
	 * @param staffLine
	 */
	public void addClef(String clefType, int staffLine) {
		Label l = new Label(Integer.valueOf(staffLine), clefType);
		if(!clefs.contains(l)) {
			clefs.add(l);
		}
	}

	public void addMeasureable(Measurable m) {
		accept(m);
	}
	
	/**
	 * Consume (accept) each Measureable in the List provided
	 * @param mlist
	 */
	public void addMeasurables(List<Measurable> mlist) {
		mlist.stream().forEach(m -> accept(m));
	}
	
	public List<Measurable> getMeasureables() {
		return measureables;
	}
	
	public int size() {
		return measureables.size();
	}

	public Tempo getTempo() {
		return tempo;
	}

	public void setTempo(Tempo tempo) {
		this.tempo = tempo;
	}

	public boolean isTempoChange() {
		return tempoChange;
	}

	public void setTempoChange(boolean tempoChange) {
		this.tempoChange = tempoChange;
	}

	public Measure getLastMeasure() {
		return lastMeasure;
	}

	public void setLastMeasure(Measure lastMeasure) {
		this.lastMeasure = lastMeasure;
	}

	public Measure getNextMeasure() {
		return nextMeasure;
	}

	public void setNextMeasure(Measure nextMeasure) {
		this.nextMeasure = nextMeasure;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public ScorePart getScorePart() {
		return scorePart;
	}

	public void setScorePart(ScorePart scorePart) {
		this.scorePart = scorePart;
	}

	public boolean isKeyChange() {
		return keyChange;
	}

	public void setKeyChange(boolean keyChange) {
		this.keyChange = keyChange;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}
	
	public static int getDivisionsPerMeasure() {
		return divisionsPerMeasure;
	}
	public static void setDivisionsPerMeasure(int dpm) {
		divisionsPerMeasure = dpm;
	}

	@Override
	public void accept(Measurable m) {
		measureables.add(m);
	}
}
