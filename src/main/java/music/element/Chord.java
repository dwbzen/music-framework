package music.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;

/**
 * A Chord is a vertical arrangement of Notes
 * All the notes in the same voice will have the same duration.
 * A Note that would appear in the same time slice with a different duration
 * would have a different voice.
 * The notes are maintained in a SortedSet, ordered by Note.
 * Chords can be tied together like notes.
 * By definition, all the notes in each Chord are implicitly tied together.
 * A Chord must also have a non-null root note. If not specified, it is
 * assigned in the constructors/factory methods.
 * 
 * @author don_bacon
 *
 */
public class Chord extends Measurable implements IJson, Comparable<Chord>, IMeasurableContainer<Note> {

	private static final long serialVersionUID = -573501626988564230L;
	/*
	 * Notes in the Chord sorted in increasing Pitch order
	 */
	@JsonProperty	private SortedSet<Note> notes = new TreeSet<Note>();
	@JsonProperty	private Note root = null;
	@JsonProperty	private Chord tiedTo = null;		// the Chord this is tied to - occurs after this Chord
	@JsonProperty	private Chord tiedFrom = null;		// the Chord this is tied from - occurs before this Chord
	@JsonProperty	private int size = 0;
	@JsonIgnore		private Iterator<Note> iterator = null;
	
	public Chord() {
		
	}
	
	/**
	 * Creates a Chord from a List of Note.
	 * The first entry is set as the Root note.
	 * @param chordNotes
	 */
	public Chord(List<Note> chordNotes) {
		for(Note note : chordNotes) {
			addNote(note);
			if(root == null) {
				root = note;
			}
		}
	}
	
	/**
	 * Creates a Chord from Pitches.
	 * @param name a String name for the chord. Can be null.
	 * @param root	the root Pitch - sets the Duration to 0.
	 * @param pitches List<Pitch> forming the chord. Assigns each a duration of 0.
	 */
	public Chord(String name, Pitch root, List<Pitch> pitches) {
		this.setName(name);
		this.setRoot(new Note(root, 0));
		for(Pitch p:pitches) {
			addNote(new Note(p,0));
		}
	}
	
	public static Chord createChord(Chord aChord, Duration duration) {
		Chord chord = new Chord();
		aChord.notes.forEach(note -> chord.addNote(new Note(note)));
		chord.setNoteType(aChord.getNoteType());
		chord.setRoot(aChord.getRoot());
		return chord;
	}
	
	/**
	 * Creates a Chord from a String[] of pitches.
	 * Pitches can be octave-neutral as in "Eb" or specify an octave as in "Eb4"
	 * The first entry is set as the Root note.
	 * @param chordNotes
	 * @param duration duration to apply, must be >= 0
	 * @return Chord
	 */
	public static Chord createChord(String[] chordNotes, int duration) {
		Chord chord = new Chord();
		Note root = null;
		Duration dur = new Duration(duration);
		if(chordNotes != null && chordNotes.length>0) {
			for(String s : chordNotes) {
				Pitch p = Pitch.fromString(s);
				Note note = new Note(p, dur);
				chord.addNote(note);
				if(root == null) {
					root = note;
					chord.setRoot(root);
				}
			}
			chord.duration = dur;
		}
		return chord;
	}

	public SortedSet<Note> getNotes() {
		return notes;
	}
	
	/**
	 * Adds a Note to this chord (that already isn't there)
	 * 
	 * @param n Note to add
	 * @return true if added, false otherwise (a duplicate)
	 * @throws IllegalArgumentException if try to add a rest OR duration doesn't match
	 */
	@Override
	public boolean addNote(Note note) {
		if(note == null || note.isRest()) {
			throw new IllegalArgumentException("Note is null or a rest");
		}
		if(duration == null) {
			duration = note.getDuration();
		}
		else if(duration.getDurationUnits() != note.getDuration().getDurationUnits()) {
			throw new IllegalArgumentException("Note has wrong duration for chord: " + note.getDuration());
		}
		boolean added = notes.add(note);
		if(added) {
			size++;
			note.setOrdinal(size);
			note.setContainer(this);
		}
		return added;
	}

	/**
	 * Removes a Note from the Chord if it is present.
	 * Handles breaking any ties to/from this note.
	 * 
	 * @param note
	 */
	@Override
	public boolean removeNote(Note note) {
		boolean removed = notes.remove(note);
		if(removed) {
			note.breakAllTies();
		}
		size--;
		return removed;
	}
	
	public List<Note> removeUnisonNotes() {
		List<Note> nlist = new ArrayList<Note>();
		Pitch prevPitch = null;
		/*
		 * Relies on that fact that notes are sorted
		 */
		for(Note note:notes) {
			if(prevPitch == null || !note.getPitch().equals(prevPitch)) {
				nlist.add(note);
				prevPitch = note.getPitch();
			}
		}
		return nlist;
	}
	
	@Override
	public int size() {
		return notes.size();
	}
	
	@Override
	public boolean containsNote(Note n) {
		return containsPitch(n.getPitch());
	}
	
	@Override
	public boolean containsPitch(Pitch pitch) {
		for(Note note : notes) {
			if(note.getPitch().equals(pitch)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a deep copy of the Chord Notes in a new SortedSet<Note>
	 * @return
	 */
	public SortedSet<Note> deepCopyChordNotes() {
		SortedSet<Note> notesCopy = new TreeSet<Note>();
		for(Note note : notes) {
			notesCopy.add(new Note(note));
		}
		return notesCopy;
	}
	
	@Override
	public String toString() {
		Iterator<Note> noteit = notes.iterator();
		StringBuffer sb = new StringBuffer("Chord(" + notes.size() + "): {");
		while(noteit.hasNext()) {
			Note note = noteit.next();
			Pitch pitch = note.getPitch();
			Duration duration = note.getDuration();
			sb.append(" Note[" + note.getOrdinal() + "]: " + pitch.toString() + " duration: " + duration.toString());
		}
		sb.append(" } " );
		if(!tieType.equals(TieType.NONE)) {
			sb.append(" tie: " + tieType);
			if(tiedFrom != null) {	// tiedFrom and tiedTo are Chord instances
				sb.append(" tiedFrom: " + tiedFrom.toString() + " " + tiedFrom.getDuration());
			}
			if(tiedTo != null) {
				sb.append(" tiedTo: " + tiedTo.toString() + " " +  tiedTo.getDuration());
			}
		}
		return sb.toString();
	}
	
	@Override
	/**
	 * Chords are equal if they have the same notes and the same root.
	 * This considers notes that are enharmonic equivalent to be equals in this context (so F# is the same note as Gb).
	 * This also holds for the root pitch in each chord.
	 * If same number of notes in each, add up range step of each and compare the results.
	 * 
	 * @param object
	 * @return
	 */
	public int compareTo(Chord o) {
		int cmp = 0;
		int so = o.size();
		int s = size();
		if(s == so) {
			Iterator<Note> it = notes.iterator();
			Iterator<Note> ito = o.getNotes().iterator();
			int psum = 0;
			int psumo = 0;
			while(it.hasNext()) {
				Pitch p = it.next().getPitch();
				Pitch po = ito.next().getPitch();
				psum += p.getRangeStep();
				psumo += po.getRangeStep();
			}
			cmp = (psum == psumo) ? 0 : (psum < psumo) ? -1 : 1;
		}
		else {
			cmp = s < so ? -1 : 1;
		}
		return cmp;
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof Chord ? (compareTo((Chord)object)== 0) && getRoot().equals(((Chord)object).getRoot()) : false;
	}
	
	/**
	 * Sets the optional root of the chord and also adds it
	 * if not already there.
	 * @param root
	 */
	public void setRoot(Note root) {
		this.root = root;
		notes.add(root);
	}

	/**
	 * If not set, the root is by definition the note with the lowest (smallest) pitch.
	 * @return root Note. Could be null if no notes in the chord yet.
	 */
	public Note getRoot() {
		if(root == null && !notes.isEmpty()) {
			root = notes.first();
		}
		return root;
	}

	public Chord getTiedTo() {
		return tiedTo;
	}

	public void setTiedTo(Chord tiedTo) {
		this.tiedTo = tiedTo;
	}

	public Chord getTiedFrom() {
		return tiedFrom;
	}

	public void setTiedFrom(Chord tiedFrom) {
		this.tiedFrom = tiedFrom;
	}

	@Override
	protected void setMeasurableType() {
		setType(Measurable.CHORD);
	}

	@Override
	public int countPitches(Pitch pitch) {
		int count = 0;
		for(Note note : notes) {
			if(note.getPitch().equals(pitch)) {
				count++;
			}
		}
		return count;
	}

	protected int getSize() {
		return size;
	}

	/**
	 * Sets the Duration on all Notes in the Chord and the Chord itself.
	 * 
	 * @param dur
	 */
	public void setNoteDurations(Duration dur) {
		duration = dur;
		for(Note note : notes) {
			note.setDuration(new Duration(dur));
		}
	}

	@Override
	/**
	 * Sets the tuplet type for the Chord and for each note in the chord.
	 */
	public void setTupletType(TupletType tupletType) {
		this.tupletType = tupletType;
		for(Note note : notes) {
			note.setTupletType(tupletType);
		}
	}

}
