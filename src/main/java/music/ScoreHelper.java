package music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.dwbzen.common.math.Factor;
import org.dwbzen.common.math.MathUtil;
import org.dwbzen.common.math.Partition;
import music.element.Duration;
import music.element.Measure;
import music.element.Note;
import music.element.Tempo;
import music.element.Duration.BeatUnit;
import music.element.Measurable.TieType;

/**
 * Utility and helper methods
 * 
 * @author don_bacon
 *
 */
public final class ScoreHelper {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ScoreHelper.class);

	public final static String WHOLE = "whole";
	public final static String HALF = "half";
	public final static String QUARTER = "quarter";
	public final static String EIGHTH = "eighth";
	public final static String SIXTEENTH = "16th";
	public final static String THIRTY_SECOND = "32nd";
	public final static String SIXTY_FOURTH = "64th";
	
	public static String[] NoteTypes = {
		"whole", "half", "quarter", "eighth", "16th", "32nd", "64th"
	};
	public static Map<Integer, String> NoteTypeMap = new HashMap<Integer, String>();

	static Map<Integer, BeatUnit> beatUnitMap = new HashMap<Integer, BeatUnit>();
	static {
		beatUnitMap.put(Integer.valueOf(1), BeatUnit.WHOLE);
		beatUnitMap.put(Integer.valueOf(2), BeatUnit.HALF);
		beatUnitMap.put(Integer.valueOf(4), BeatUnit.QUARTER);
		beatUnitMap.put(Integer.valueOf(8), BeatUnit.EIGHTH);
		beatUnitMap.put(Integer.valueOf(16), BeatUnit.SIXTEENTH);
		beatUnitMap.put(Integer.valueOf(32), BeatUnit.THIRTY_SECOND);
		beatUnitMap.put(Integer.valueOf(64), BeatUnit.SIXTY_FOURTH);
		beatUnitMap.put(Integer.valueOf(128), BeatUnit.ONE_TWENTY_EIGHTH);
	}
	protected static Map<Integer, List<String>> noteTypeMap = new HashMap<Integer, List<String>>();
	
	static {
		NoteTypeMap.put(1, WHOLE);
		NoteTypeMap.put(2, HALF);
		NoteTypeMap.put(4, QUARTER);
		NoteTypeMap.put(8, EIGHTH);
		NoteTypeMap.put(16, SIXTEENTH);
		NoteTypeMap.put(32, THIRTY_SECOND);
		NoteTypeMap.put(64, SIXTY_FOURTH);	
	}
	
	/**
	 * Converts the note's duration in seconds to a #divisions
	 * based on Measure parameters divisions and tempo
	 * @param note Note
	 * @param measure Measure
	 */
	public static void convertNoteDuration(Note note, Measure measure) {
		Duration duration = note.getDuration();
		double seconds = duration.getRawDuration();
		Tempo tempo = measure.getTempo();
		/*
		 * the tempo determines the #beats per minute
		 * beats is the #beats in this tempo this note will consume
		 * so for example, .8 sec. duration in tempo quarter note = 120
		 * would be 1.6 beats
		 * If there were 24 divisions/measure, in 4/4 time
		 * 1.6 beats would be 1.6 * 6 = 9.6 divisions.
		 * This is rounded to nearest integer so 9
		 * Can't be zero, so if rounding results in 0 units, sets to 1
		 */
		double beatsec = tempo.getBeats(seconds);
		int divisionsPerBeat = measure.getDivisionPerBeat();	// actually divisions per beat note
		double divs = divisionsPerBeat * beatsec;
		int divisions = (int) Math.round(divs);
		int durationUnits = (divisions==0) ? 1 : divisions;
		duration.setDurationUnits(durationUnits);
	}
	
	public static List<Integer> factor(Measure measure, Note note) {
		int noteUnits = note.getDuration().getDurationUnits();
		return factor( measure.getDivisions(), measure.getBeats(), measure.getBeatNote(), noteUnits);
	}
	/**
	 * Factors units to powers of 2 for a given time signature & measure divisions
	 * 
	 * @param divsPerMeasure #divisions per measure
	 * @param beats #beats per measure
	 * @param beatNote note value that has the beat
	 * @param noteUnits units for this note to factor
	 * @return List<Integer> of factors. Elements are powers of 2 (-6 <= n <= 6)
	 * Time signature is beats/beatNote (4/4 or 3/8 for example)
	 * Note values are:  1=whole, 2=half, 4=quarter, 8=eighth, 16=sixteenth, 32=32nd, 64=64th
	 * Constraint - #divisions per beat must be an integer.
	 * Example: noteUnits=19, 4/4 time, 24 divisions/measure = 3.166666, factors as [1, 0, -3, -5]
	 * or 2 + 1 + 1/8 + 1/32 = 3.15625 (close enough!)
	 * Range is 2^6 (64) to 2^-6 (1/64)
	 */
	public static List<Integer> factor(int divsPerMeasure, int beats, int beatNote, int noteUnits) {
		int divsPerBeat = divsPerMeasure/beats;
		double beatNotes = noteUnits / (double)divsPerBeat;
		// get the integer part of that - that's the beat note multiplier
		log.debug("divs/beat: " + divsPerBeat + " beatNotes: " + beatNotes);
		List<Integer> factors = new ArrayList<Integer>();
		double remaining = beatNotes;
		if(remaining >= 1) {
			for(int i=6; i>=0; i--) {
				double fact = Math.pow(2, i);
				if(fact <= remaining) {
					factors.add(i);
					remaining -= fact;
				}
			}
		}
		for(int i=1; i<=6; i++) {
			double fact = Math.pow(2, -i);
			if(fact <= remaining) {
				factors.add(-i);
				remaining -= fact;
			}
		}
		log.debug("remaining:" + remaining);
		return factors;
	}
	
	/**
	 * Nothing fancy - create tied notes from a factors list
	 * @param measure
	 * @param note the note to refactor
	 * Example: [0, -1, -2] (4/4 32 divs/measure) = quarter + 16th + 32nd
	 */
	public static List<Note> refactorToNotes(Measure measure, Note noteToRefactor) {
		int divsPerMeasure = measure.getDivisions();
		int beats =  measure.getBeats();
		int divsPerBeat = divsPerMeasure/beats;
		int beatNote = measure.getBeatNote();
		List<Integer> factors = factor(measure, noteToRefactor);
		List<Note> notes = new ArrayList<Note>();
		Note nextNote = null;
		Note note = noteToRefactor;
		for(int i=0; i<factors.size(); i++) {
			int factor = factors.get(i).intValue();
			int div = (int)(divsPerBeat * Math.pow(2, factor));
			String noteType = getNoteType(factor, beatNote);
			log.debug("div: " + div + " note type; " + noteType);
			if(i==0) {
				note.getDuration().setDurationUnits(div);
				note.getDuration().setDots(0);
				note.setNoteType(noteType);
				if(note.getTiedTo() != null) {
					// untie this
					note.setTiedTo(null);
					if(note.getTieType().equals(TieType.START)) {
						note.setTieType(TieType.NONE);
					}
					else if(note.getTieType().equals(TieType.BOTH)) {
						note.setTieType(TieType.STOP);
					}
				}
				notes.add(note);
			}
			else {
				nextNote = new Note(note, true);	// creates a new Note and ties them
				nextNote.getDuration().setDurationUnits(div);
				nextNote.setNoteType(noteType);
				notes.add(nextNote);
				note = nextNote;
			}
		}
		return notes;
	}
	
	/**
	 * a "dot" adds half the value to a note, ".." adds 1/2 + 1/8 and so forth.
	 * Given a List of factors, this returns the #dots needed.
	 * It's like rounding down. For example, [1, 0, -3, -5] would be 0 dots
	 * factors: [1, -1, -2, -4, -6] would be 2 dots. (missing the -3)
	 * @param factors
	 * @return
	 */
	public static int dots(List<Integer> factors) {
		int ndots = 0;
		int prev = 0;
		for(int i=0; i<factors.size(); i++) {
			if(i==0) {
				prev = factors.get(i);
			}
			else {
				int n = factors.get(i);
				if(n == (prev-1)) {
					ndots++;
					prev = n;
				}
				else {
					break;
				}
			}
		}
		return ndots;
	}
	
	/**
	 * Converts List<Integer> factors to int units, adjusting factors accordingly.
	 * @param factors List<Integer> factors (powers of 2)
	 * @param divsPerMeasure #divisions (units) per measure
	 * @param beats #beats per measure
	 * @return units for this factors
	 */
	public static int roundFactorToUnits(List<Integer> factors, int divsPerMeasure, int beats) {
		double units = 0;
		int divsPerBeat = divsPerMeasure/beats;	// this better be an int or else
		List<Integer> refactors = new ArrayList<Integer>();
		for(Integer n : factors) {
			double d = divsPerBeat * Math.pow(2, n);
			if(d>= 1 && Math.round(d)==(int)d) {
				units += d;
				refactors.add(n);
			}
		}
		if(refactors.size() < factors.size()) {
			factors.clear();
			factors.addAll(refactors);
		}
		return (int)units;
	}
	
	/**
	 * Updates the Note Duration units from Measure attributes and existing Note duration units
	 * Also updates #dots in the Duration
	 * NOTE that the duration is adjusted (rounded down) if needed so it can be
	 * represented by a note + dots.
	 * 
	 * @param measure
	 * @param note
	 * @return note units
	 */
	public static int setNoteDurationFromFactors(Measure measure, Note note) {
		int divsPerMeasure = measure.getDivisions();
		int beats = measure.getBeats();
		int beatNote = measure.getBeatNote();	// 1=whole, 2=half etc.
		int noteUnits = note.getDuration().getDurationUnits();
		int divsPerBeat = divsPerMeasure/beats;
		List<Integer> factors = factor(divsPerMeasure, beats, beatNote, noteUnits);
		int ndots = dots(factors);
		// recompute units from factors & dots
		//
		int nunits = 0;
		for(int i=0; i<=ndots; i++) {
			nunits += divsPerBeat * Math.pow(2, factors.get(i));
		}
		note.getDuration().setDurationUnits(nunits);
		note.getDuration().setDots(ndots);
		if(factors.size() == 0) {
			System.err.println("no factors ");
			return 0;
		}
		String noteType = getNoteType(factors, beatNote);
		note.setNoteType(noteType);
		return nunits;
	}
	
	public static String getNoteType(List<Integer> factors, int beatNote) {
		int pow = factors.get(0);	// the largest power of 2 factor
		int ind = beatUnitMap.get(Integer.valueOf(beatNote)).ordinal() - pow;
		String noteType = (ind >= 0 && ind < NoteTypes.length) ? NoteTypes[ind] : ind + "?";
		return noteType;
	}
	
	public static String getNoteType(int factor, int beatNote) {
		int pow = factor;	// the largest power of 2 factor
		int ind = beatUnitMap.get(Integer.valueOf(beatNote)).ordinal() - pow;
		String noteType = (ind >= 0 && ind < NoteTypes.length) ? NoteTypes[ind] : ind + "?";
		return noteType;
	}
	
	/**
	 * Raw conversion of List<Integer> factors (powers of 2) to units.
	 * @param factors
	 * @param divsPerMeasure
	 * @param beats
	 * @return double units
	 */
	public static double convertFactorToUnits(List<Integer> factors, int divsPerMeasure, int beats) {
		double units = 0;
		int divsPerBeat = divsPerMeasure/beats;	// this better be an int or else
		for(Integer n : factors) {
			double d = divsPerBeat * Math.pow(2, n);
			units += d;
		}
		return units;
	}
	
	/**
	 * Partitions the duration of a note (noteUnits)  in terms of Measure parameters.
	 * A Partition consists of a List<Factor> each factor is a  
	 * List<Integer> of powers of 2 along with #dots (consecutive powers of 2 starting with the largest factor)
	 * Examples, where a measure has 64 divisions in 4/4 time, #beats = 4, beatNote = 4
	 * partition(64, 4, 16, 23) = 
	 * @param divsPerMeasure #divisions (units) per measure
	 * @param beats #beats in the time signature (#beats per measure)
	 * @param beatNote the beat note in the Measure's time signature (1=whole, 2=half,4=quarter, 8=eighth etc.)
	 * @param noteUnits duration of a note in integer units
	 * @return Partition of that noteUnits.
	 */
	public static  Partition partition(int divsPerMeasure, int beats, int beatNote, int noteUnits) {
		List<Integer> factors = MathUtil.factor(divsPerMeasure, beats, beatNote, noteUnits);
		Factor factor = new Factor(factors);
		Partition part = MathUtil.partition(factor);
		return part;
	}
	
	public static void main(String[] args) {

	}

}
