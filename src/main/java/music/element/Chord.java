package music.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

import util.IJson;

/**
 * A Chord is a vertical arrangement of Notes
 * All the notes in the same voice will have the same duration.
 * A Note that would appear in the same time slice with a different duration
 * would have a different voice.
 * The notes are maintained in a list in ascending order by pitch.
 * Chords can be tied together like notes.
 * By definition, all the notes in each Chord are implicitly tied together.
 * 
 * @author don_bacon
 *
 */
@Entity(value="Chord")
public class Chord extends Measurable implements IJson, Comparable<Chord>, IMeasurableContainer<Note> {

	private static final long serialVersionUID = -573501626988564230L;
	private static Morphia morphia = new Morphia();
	/*
	 * Notes in the Chord sorted in increasing Pitch order
	 */
	@Embedded	private SortedSet<Note> notes = new TreeSet<Note>();
	@Embedded	private Note root = null;
	@Embedded	private Chord tiedTo = null;		// the Chord this is tied to - occurs after this Chord
	@Embedded	private Chord tiedFrom = null;		// the Chord this is tied from - occurs before this Chord
	@Property	private int size = 0;
	@Transient	private Iterator<Note> iterator = null;
	
	public Chord() {
	}
	
	public Chord(List<Note> chordNotes) {
		for(Note note : chordNotes) {
			notes.add(note);
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

	public SortedSet<Note> getNotes() {
		return notes;
	}
	
	/**
	 * Adds a Note to this chord (that already isn't there)
	 * Can't add a rest to a Chord.
	 * @param n Note to add
	 * @return true if added, false otherwise (a duplicate)
	 * @throws IllegalArgumentException if try to add a rest OR duration doesn't match
	 */
	@Override
	public boolean addNote(Note note) {
		if(note == null || note.isRest()) {
			throw new IllegalArgumentException("Note is null or a rest");
		}
		if(duration == null || duration.getDurationUnits() == 0) {
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
	 * If same number of notes in each, add up the pitches of each and compare the results
	 * @param o
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
				psum += p.getStep().value() * (1 + p.getOctave());
				psumo += po.getStep().value() * (1 + po.getOctave());
			}
			cmp = (psum == psumo) ? 0 : (psum < psumo) ? -1 : 1;
		}
		else {
			cmp = s < so ? -1 : 1;
		}
		return cmp;
	}
	
	public boolean equals(Chord o) {
		boolean eq = (compareTo(o)== 0) && getRoot().equals(o);
		return eq;
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
		if(root == null && notes.size()>0) {
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

	public void setNoteDurations(Duration dur) {
		for(Note note : notes) {
			note.setDuration(new Duration(dur));
		}
	}

	
	@Override
	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}
}
