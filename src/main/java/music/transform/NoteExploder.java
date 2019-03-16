package music.transform;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.math.Factor;
import org.dwbzen.common.math.IntegerPair;
import org.dwbzen.common.math.Partition;
import music.ScoreHelper;
import music.element.Chord;
import music.element.Duration;
import music.element.Measurable;
import music.element.Measurable.TupletType;
import music.element.Measure;
import music.element.Note;
import music.element.Pitch;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This explodes a single pitch (Note) horizontally into many pitches (ARPEGIO)
 * or vertically into a chord (CHORD) using a formula such that the overall duration remains the same.
 * An exploder is not key- or scale- aware. It just explodes the note.
 * If this is desired, use a ScaleTransformer after the ExplodeTransformer.
 * The explode formula is supplied as an int[] where each element specifies
 * the number of half-steps above (below) the root (which is the pitch of the note
 * being exploded).
 * For example: formula = [0, 1, 2, 1, 0, -1, -2, 0] would explode the single note
 *  C4 say into 8 notes: C4, C#4, D4, C#4, C4, B3, Bb3, C4.
 *  If the duration units of the original note were 16, each exploded note would
 *  have a duration of 2.
 * If duration / #notes is not an integer, a tuplet is created.
 * For example: given the formula {-2, -1, 0, 1, 2} and C4 duration units 8,
 * the explosion would be: Bb3, B3, C4, C#4, D4. Each would have
 * a duration of 8/5 or a specified ratio of {5, 4} - 5 notes in the time of 4 (which would be 2)
 * So an explode formula has 2 components: the List<IntegerPair> that defines
 * the formula intervals, and an IntegerPair that specifies the ratio (default is [1:1]).
 * Each IntegerPair of the formula can specify a range of intervals that is randomly selected.
 * 
 * @author don_bacon
 *
 */
public class NoteExploder extends AbstractExploder {

	private static final long serialVersionUID = -5725725372694711902L;
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ExplodeTransformer.class);
	/*
	 *  if true, breaks ties when forming chords - this value is configured in the ExplodeTransformer
	 */
	@JsonIgnore	private boolean breakChordTies = false;

	
	public NoteExploder(ExploderType exptype, List<IntegerPair> formula) {
		super(exptype, formula, IExploder.ONE_TO_ONE, 0);
	}
	
	public NoteExploder(ExploderType exptype, List<IntegerPair> formula, IntegerPair ratio) {
		super(exptype, formula, ratio, 0);
	}
	
	public NoteExploder(ExploderType exptype, List<IntegerPair> formula, IntegerPair ratio, int freq) {
		super(exptype, formula, ratio, freq);
	}
	
	@Override
	public List<Measurable> explode(Measurable m, Measure measure) {
		Note note = (Note)m;
		List<Measurable> notes = null;
		if(getExploderType().equals(ExploderType.ARPEGIO)) {
			notes = explodeNoteToArpegio(note, measure);
		}
		else if(getExploderType().equals(ExploderType.CHORD)) {
			notes = explodeNoteToChord(note, measure);
		}
		return notes;
	}

	private List<Measurable> explodeNoteToArpegio(Note note, Measure measure) {
		List<Measurable> notes = new ArrayList<Measurable>();
		int size = formula.size();
		Duration duration = note.getDuration();
		Duration newDuration = null;
		Pitch rootPitch = note.getPitch();
		String noteType = null;
		boolean tuplet = false;
		if(ratio.same()) {
			int ndur = duration.getDurationUnits() / size;		// better be divisable
			newDuration = new Duration(ndur);
			noteType = getNoteType(note, newDuration, measure);
		}
		else {
			// a tuplet of some sort as in 3:2 (3 notes in the time of 2) or 5:4
			tuplet = true;
			int ndur = duration.getDurationUnits() / ratio.getY();
			newDuration = new Duration(ndur);
			newDuration.setRatio(ratio);
			/*
			 * get the note type for this duration.
			 * this also sets #dots in newDuration if applicable
			 * so maybe we Don't want to explode this note
			 */
			noteType = getNoteType(note, newDuration, measure);
		}
		if(shouldExplode(note, noteType, newDuration)) {
			for(int i=0; i<formula.size(); i++) {
				IntegerPair pair = formula.get(i);
				int interval = pair.same() ? pair.getX() : getRandom().nextInt(pair.getX(), pair.getY().intValue()+1);
				Pitch newPitch = new Pitch(rootPitch, interval);
				Note newNote = new Note(newPitch, newDuration);
				if(tuplet) {
					if(i==0) {
						newNote.setTupletType(TupletType.START);
					}
					else if(i==formula.size()-1) {
						newNote.setTupletType(TupletType.STOP);
					}
					else {
						newNote.setTupletType(TupletType.CONTINUE);
					}
				}
				newNote.setNoteType(noteType);
				checkRange(newNote);
				if(i==0 && note.getTiedFrom() != null) {	// first note in formula
					if(newNote.getPitch().equals(note.getTiedFrom().getPitch())) {
						newNote.setTiedFrom(note.getTiedFrom());
					}
					else {
						newNote.setTiedFrom(null);
					}
				}
				else if(i==formula.size()-1 && note.getTiedTo() != null) {	// last note in formula
					if(newNote.getPitch().equals(note.getTiedTo().getPitch())) {
						newNote.setTiedTo(note.getTiedTo());
					}
					else {
						newNote.setTiedTo(null);
					}
				}
				notes.add(newNote);
			}
		}
		return notes;
	}
	
	/**
	 * Explodes a Note into a Chord.
	 * NOTE that the Note itself is always included in the Chord regardless of the formula.
	 * Consequently intervals of 0 in the formula are ignored.
	 * @param note
	 * @param measure
	 * @return List<Measurable> which will have 1 Chord element
	 */
	private  List<Measurable> explodeNoteToChord(Note note, Measure measure) {
		List<Measurable> notes = new ArrayList<Measurable>();
		Chord chord = new Chord();
		chord.addNote(note);
		chord.setRoot(note);
		int size = formula.size();
		Duration duration = note.getDuration();
		Pitch rootPitch = note.getPitch();
		String noteType = getNoteType(note, duration, measure);	// won't change as this is adding notes vertically
		Note newNote = null;
		if(shouldExplode(note, noteType, duration)) {
			if(breakChordTies) {
				note.breakAllTies();	// TODO this doesn't quite work
			}
			for(int i=0; i<size; i++) {
				IntegerPair pair = formula.get(i);
				int interval = pair.same() ? pair.getX() : getRandom().nextInt(pair.getX(), pair.getY().intValue()+1);
				if(interval != 0) {
					Pitch newPitch = new Pitch(rootPitch, interval);
					newNote = new Note(newPitch, duration);
					newNote.setNoteType(noteType);
					checkRange(newNote);
					if(!chord.containsPitch(newNote.getPitch())) {
						chord.addNote(newNote);
					}
				}
			}
			notes.add(chord);
		}
		else {
			notes.add(note);
		}
		return notes;
	}
	
	private void checkRange(Note note) {
		Pitch newPitch = note.getPitch();
		if(pitchRange != null) {
			if(newPitch.compareTo(pitchRange.getLow()) < 0 ) {
				newPitch = new Pitch(pitchRange.getLow());
			}
			else if(newPitch.compareTo(pitchRange.getHigh()) > 0 ) {
				newPitch = new Pitch(pitchRange.getHigh());
			}
		}

	}
	
	/**
	 * Gets the note type and if applicable, sets the Dots in the Duration
	 * @param note
	 * @param newDuration
	 * @return String note type
	 */
	public String getNoteType(Note note, Duration newDuration, Measure measure) {
		String noteType = null;
		int divsPerMeasure = measure.getDivisions();	// 32 or 64 for example
		int beatNote = measure.getBeatNote();
		int beats = measure.getBeats();
		int noteUnits = newDuration.getDurationUnits();
		Partition part = ScoreHelper.partition(divsPerMeasure, beats, beatNote, noteUnits);
		List<Factor> factors = part.getPartitions();
		Factor factor = factors.get(0);
		if(factors.size() > 1) {
			log.warn("Can't factor duration: " + newDuration + " in measure " + measure.getNumber());
		}
		noteType = ScoreHelper.getNoteType(factor.getFactors(), beatNote);
		newDuration.setDots(factor.getDots());
		return noteType;
	}
	
	/**
	 * Need to externalize the should explode rules.
	 * For this implementation, the original note type must be eighth or greater 
	 * and NO dots in the new (i.e. exploded) duration (for ARPEGIO type).
	 * or at most 1 dot for CHORD type
	 * @param note
	 * @param noteType
	 * @param newDuration
	 * @return
	 */
	public boolean shouldExplode(Note note, String noteType, Duration newDuration) {
		boolean shouldExplode = noteType.equals("whole") || noteType.equals("half") ||
				noteType.equals("quarter") || noteType.equals("eighth");
		if(getExploderType().equals(ExploderType.ARPEGIO)) {
			shouldExplode = shouldExplode && (newDuration.getDots() == 0);
		}
		else if(getExploderType().equals(ExploderType.CHORD)) {
			shouldExplode = shouldExplode && (newDuration.getDots() <= 1);
		}
		return shouldExplode;
	}

	public boolean isBreakChordTies() {
		return breakChordTies;
	}

	public void setBreakChordTies(boolean breakChordTies) {
		this.breakChordTies = breakChordTies;
	}
	
}
