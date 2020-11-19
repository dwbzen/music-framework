package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.ScorePart;
import org.dwbzen.music.element.direction.ScoreDirection;
import org.dwbzen.music.musicxml.DisplayInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Measure is a horizontal arrangement of Note and/or Chord : i.e. a Measurable<br>
 * A new Measure without any parameters has defaults of 480 divisions,<br>
 * time signature of 3/4 (3 beats/measure, quarter note has the beat), key of C - which determines the key signature,<br>
 * moderato Tempo (quarter note = 80 metronome mark),
 * G-cleff in the ususal place, and mf Dynamic.</p>
 * 
 * A Measure cannot exist by itself - it must be part of an IMeasureContainer, a ScorePart for example.<br>
 * Once created, a Measure is built by Consuming instances of Measurable (Notes, Chords).</p>
 * 
 * A Measure can consist of 1 or 2 staves. Most instruments are scored on a single staff.<br>
 * Instruments scored on a Grand Staff like a Piano require 2 staves.<br>
 * A PipeOrgan requires 3 staves - 1 for the pedals, 2 for the manual.<br>
 * Each Measurable has an associated staff number.<br>
 * When converted to musicXML the notes appear in staff order.
 * 
 * @author don_bacon
 *
 */
public class Measure implements IJson, Consumer<Measurable>, BiConsumer<Integer, Measurable> {

	public static final int DEFAULT_DIVISIONS_PER_MEASURE = RhythmScale.defaultUnitsPerMeasure;		// 480
	private static int divisionsPerMeasure = DEFAULT_DIVISIONS_PER_MEASURE;		// default value

	/**
	 * Number of basic units in the measure. Must be >0, default is 480
	 */
	@JsonProperty("divisions")	private int divisions;
	@JsonProperty				private Key key = Key.C_MAJOR;	// sensible default
	/**
	 * Time signature beats per measure
	 */
	@JsonProperty("beats")		private int beats = 4;	// beats per measure. divisions per beat = divisions/beats, 480/4 = 120 for example
	/**
	 * Time signature beat note (1=whole, 2=half, 4 = quaver, 8 = semiquaver etc.)
	 * SO time signature is beats/beatNote: 3/4, 6/8, whatever
	 */
	@JsonProperty("beatNote")	private int beatNote = 4;		// defaults to quarterNote
	@JsonProperty("directions")	private List<ScoreDirection> scoreDirections = new ArrayList<>();
	@JsonProperty("staves")		private int numberOfStaves = 1;		// set to 2 for Grand Staff or as required by the instrument.
	
	@JsonProperty				private Dynamics dynamics = new Dynamics();		// defaults to mf
	@JsonProperty				private Tempo tempo = new Tempo(80);	// defaults to 80 (Moderato)
	/**
	 * true if the tempo changes on this measure (from the previous)
	 */
	@JsonProperty("tempoChange")	private boolean tempoChange = false;
	@JsonProperty("keyChange")		private boolean keyChange = false;
	
	@JsonIgnore	private ScorePart scorePart = null;

	/**
	 * Measure number, starts at 1
	 */
	@JsonProperty("number")		public int number = 1;
	@JsonProperty	private Label label;
	
	@JsonProperty	private List<Label> clefs = new ArrayList<Label>();
	/* 
	 * notes & chords in this measure for this staff, indexed by staff number 1 or 2
	 */
	@JsonProperty	private Map<Integer, List<Measurable>> measureables = new HashMap<>();
	
	/**
	 * Print attributes - margins, breaks etc.
	 */
	@JsonIgnore		private List<DisplayInfo> displayInfo = new ArrayList<>();
	
	@JsonProperty	private Barline barline = null;		// optional Barline to end the measure
	
	
	protected Measure() {
	}
		
	protected Measure(int divisions) {
		this.divisions = divisions;
		clefs.add(new Label(4, "G"));	// G-clef on line 4 of the staff
		// initialize measurables list for 1 staff. Measurables for additional staffs are dynamically added as needed
		measureables.put(1, new ArrayList<>());
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
		key = prev.key;
		scorePart =  prev.getScorePart();
	}
	
	protected Measure(int divisions, Tempo tp, Measure prev) {
		this(divisions);
		this.tempo = tp;
		if(prev != null) {
			Tempo pt = (tp==null) ? prev.getTempo() : tp;
			if(!pt.equals(this.tempo)) {
				tempoChange = true;
			}
			this.number = prev.getNumber() + 1;
			this.key = prev.key;
		}
	}
	
	/**
	 * Creates a field-by-field deep copy of this with these exceptions:<br>
	 * References to ScorePart, Dynamics and Tempo, and the measureNumber are all retained.<br>
	 * scoreDirections and Barline are not copied.<br>
	 * @param measure - the Measure to copy from
	 * @param copyNotes - if true, copy (as in clone) the Measurables List for all staves
	 * @return Measure
	 */
	public static Measure copy(Measure measure, boolean copyNotes) {
		Measure newMeasure = Measure.copy(measure);
		
		for(Label l : measure.getClefs()) {
			newMeasure.clefs.add(new Label(l));
		}
		if(copyNotes) {	// copy notes/chords from all the staves
			for(int staffnum = 1; staffnum <= measure.numberOfStaves; staffnum++) {
				List<Measurable> notes = new ArrayList<>();
				for(Measurable m : measure.getMeasureables(staffnum)) {
					notes.add(m.clone());
				}
				newMeasure.measureables.put(staffnum, notes);
			}
		}
		return newMeasure;
	}
	
	/**
	 * Copies a given Measure to a new one but does not copy the Notes.
	 * @param measure
	 * @return Measure
	 */
	public static Measure copy(Measure measure) {
		Measure newMeasure = createInstance(measure.scorePart);
    	newMeasure.setTempo(measure.tempo);
		newMeasure.setDivisions(measure.divisions);
		newMeasure.setNumber(measure.number);
		newMeasure.setNumberOfStaves(measure.numberOfStaves);
		newMeasure.setKey(measure.key);
		newMeasure.setBeatNote(measure.beatNote);
		newMeasure.setBeats(measure.beats);
		
		newMeasure.label = measure.label != null ? new Label(measure.label) : null;
		return newMeasure;
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
	
	/**
	 * Stream the measurables in staff 1
	 * @return
	 */
	public Stream<Measurable> stream() {
		return measureables.get(1).stream();
	}
	
	/**
	 * Stream the measurables in given staff
	 * @return
	 */
	public Stream<Measurable> stream(int staffNumber) {
		if(!measureables.containsKey(staffNumber)) {
			measureables.put(staffNumber, new ArrayList<>());
		}
		return measureables.get(staffNumber).stream();
	}
	
	/**
	 * Removes the Measurable at a given index on staff 1 and replaces
	 * with a List<Measurable> supplied.
	 * 
	 * @param notes List<Measurable> (Note or Chord) to insert
	 * @param index insertion index
	 * @return the index of the last note added in the measure
	 */
	public int insert(List<? extends Measurable> notes, int index) {
		return insert(notes, index, 1);
	}
	
	/**
	 * Removes the Measurable at a given index on a given staff and replaces
	 * with a List<Measurable> supplied.
	 * 
	 * @param notes List<Measurable> (Note or Chord) to insert
	 * @param index insertion index
	 * @param staffnum the int staff number (1 or 2)
	 * @return the index of the last note added in the measure
	 */
	public int insert(List<? extends Measurable> notes, int index, int staffNumber) {
		if(!measureables.containsKey(staffNumber)) {
			measureables.put(staffNumber, new ArrayList<>());
		}
		int ind = index-1;
		measureables.remove(index);
		for(Measurable note : notes) {
			measureables.get(staffNumber).add(++ind, note);
		}
		return ind;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("Measure " + number + " time signature: " + beats + "/" + beatNote);
		for(int i = 1; i<= numberOfStaves; i++) {
			sb.append("\nstaff " + i);
			for(Measurable m : measureables.get(i)) {
				sb.append("\n " + m.toString());
			}
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
	
	public void addMeasureable(int staffNumber, Measurable m) {
		accept(staffNumber, m);
	}
	
	/**
	 * Consume (accept) each Measureable in the List provided
	 * @param mlist
	 */
	public void addMeasurables(List<Measurable> mlist) {
		mlist.stream().forEach(m -> accept(m));
	}
	
	/**
	 * Get the Measurables on staff 1
	 * @return List<Measurable> on staff 1
	 */
	public List<Measurable> getMeasureables() {
		return measureables.get(1);
	}
	
	/**
	 * Get the Measurables on a given staff
	 * @return List<Measurable> on the designated staff
	 */
	public List<Measurable> getMeasureables(int staffnum) {

		return measureables.get(staffnum);
	}
	
	public int size() {
		return measureables.get(1).size();
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

	public void addScoreDirection(ScoreDirection d) {
		scoreDirections.add(d);
	}
	
	public int getNumberOfStaves() {
		return numberOfStaves;
	}

	public void setNumberOfStaves(int numberOfStaves) {
		this.numberOfStaves = numberOfStaves;
	}

	public List<ScoreDirection> getScoreDirections() {
		return scoreDirections;
	}

	public void setScoreDirections(List<ScoreDirection> scoreDirections) {
		this.scoreDirections = scoreDirections;
	}

	public List<DisplayInfo> getDisplayInfo() {
		return displayInfo;
	}

	public void setDisplayInfo(List<DisplayInfo> displayInfo) {
		this.displayInfo = displayInfo;
	}

	public Barline getBarline() {
		return barline;
	}

	public void setBarline(Barline barline) {
		this.barline = barline;
	}

	@Override
	public void accept(Measurable m) {
		measureables.get(1).add(m);
	}

	@Override
	public void accept(Integer staffNumber, Measurable m) {
		if(!measureables.containsKey(staffNumber)) {
			measureables.put(staffNumber, new ArrayList<>());
		}
		measureables.get(staffNumber).add(m);
	}
}
