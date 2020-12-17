package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.PitchElement.PitchElementType;
import org.dwbzen.music.element.song.ChordFormula;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Chord is a vertical arrangement of Notes.
 * All the notes in the same voice will have the same duration.<br>
 * The notes are maintained in a SortedSet, ordered by Note (pitch + octave).<br>
 * For more information regarding chords, chord formulas and representing
 * chords in musicXML, see [Chords.md] for more information.
 * 
 * @author don_bacon
 *
 */
public class Chord extends Measurable implements IJson, Comparable<Chord>, IMeasurableContainer<Note> {

	/**
	 * Notes in the Chord sorted in increasing Pitch order
	 */
	@JsonProperty	private SortedSet<Note> notes = new TreeSet<Note>();
	@JsonIgnore		private List<Note> chordNotes = null;	// created when needed
	@JsonProperty	private Note root = null;
	@JsonProperty	private Chord tiedTo = null;		// the Chord this is tied to - occurs after this Chord
	@JsonProperty	private Chord tiedFrom = null;		// the Chord this is tied from - occurs before this Chord
	@JsonProperty	private int size = 0;
	@JsonIgnore		private PitchSet pitchSet = null;			// the unique Pitches of the Notes in the Chord
	@JsonIgnore		private ChordFormula chordFormula = null;	// optional info about the chord if known.
	
	public Chord() {
		pitchSet = new PitchSet();
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
	
	/**
	 * Create a new Chord having the notes in the Chord provided and the given Duration.<br>
	 * The Chord input argument is not altered in any way.
	 * 
	 * @param aChord the Chord having the notes to copy into the new Chord
	 * @param duration the Duration to assign each note in the new Chord
	 * @return Chord
	 */
	public static Chord createChord(Chord aChord, Duration duration) {
		Chord chord = new Chord();
		for(Note note : aChord.notes) {
			Note chordNote = new Note(note);
			chordNote.setDuration(duration);
			chordNote.setNoteType(aChord.getNoteType());
			chord.addNote(chordNote);
		}
		chord.setNoteType(aChord.getNoteType());
		chord.setRoot(aChord.getRoot());
		chord.setStaff(aChord.getStaff());
		chord.size = aChord.size();
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
	
	/**
	 * Create a Chord from a PitchElement
	 * @param pitchElement - a PitchElement, can be a single Pitch or a PitchSet
	 * @param duration
	 * @return new Chord having the pitches and duration passed
	 * @throws IllegalArgumentException
	 */
	public static Chord createChord(PitchElement pitchElement, int duration) {
		if(pitchElement == null ) {
			throw new IllegalArgumentException("PitchElement is null or empty");
		}
		Duration dur = new Duration(duration);
		Chord chord = new Chord();
		Note note = null;
		Note root = null;
		if(pitchElement.getPitchElementType() == PitchElementType.PITCH) {
			note = new Note( ((Pitch)pitchElement), dur);
			chord.addNote(note);
			chord.setRoot(note);
		}
		else {
			PitchSet ps = (PitchSet)pitchElement;
			for(Pitch p : ps.getPitches()) {
				note = new Note(p, dur);
				chord.addNote(note);
				if(root == null) {
					root = note;
					chord.setRoot(root);
				}
			}
		}
		return chord;
	}

	public SortedSet<Note> getNotes() {
		return notes;
	}
	
	/**
	 * Transposes the notes in this Chord up or down number of octaves specified<br>
	 * NOTE: Does not update tiedTo or tiedFrom chords.  TODO
	 * 
	 * @param numberOfOctaves
	 */
	public void octaveTranspose(int numberOfOctaves) {
		for(Note note : notes) {
			int octave = note.getPitch().getOctave();
			note.getPitch().setOctave(octave + numberOfOctaves);
		}
		setPitchSet();		// regenerate the PitchSet
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
			note.setContainer(this);
			getPitchSet().addUniquePitch(note.getPitch());
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
			getPitchSet().remove(note.getPitch());
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
	public void setStaff(int staff) {
		super.setStaff(staff);
		for(Note note : notes) {
			note.setStaff(staff);
		}
	}
	
	@Override
	public void setNoteType(String noteType) {
		super.setNoteType(noteType);
		for(Note note : notes) {
			note.setNoteType(noteType);
		}
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
		return getPitchSet().contains(pitch);
	}

	/**
	 * Returns a deep copy of the Chord Notes in a new SortedSet<Note>
	 * @return SortedSet<Note> 
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
		int n = 1;
		while(noteit.hasNext()) {
			Note note = noteit.next();
			Pitch pitch = note.getPitch();
			Duration duration = note.getDuration();
			sb.append(" Note[" + n + "]: " + pitch.toString() + " duration: " + duration.toString());
			n++;
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
	public int compareTo(Chord other) {
		int cmp = 0;
		int so = other.size();
		int s = size();
		if(s == so) {
			Iterator<Note> it = notes.iterator();
			Iterator<Note> ito = other.getNotes().iterator();
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

	@Override
	/**
	 * Makes a deep copy of this Chord. tie references are maintained but not deep copied.
	 */
	public Chord clone() {
		Chord chord = Chord.createChord(this, getDuration());
		chord.setTiedFrom(tiedFrom);
		chord.setTiedTo(tiedTo);
		return chord;
	}

	public PitchSet getPitchSet() {
		if(pitchSet == null || pitchSet.size()==0) {
			setPitchSet();
		}
		return pitchSet;
	}
	
	private void setPitchSet() {
		pitchSet = new PitchSet();
		for(Note note : notes) {
			pitchSet.addNewPitch(note.getPitch());
		}
	}

	@Override
	/**
	 * Invert the ties in all the chord notes
	 */
	public void invertTies() {
		notes.stream().forEach(note -> note.invertTies());
	}

	public ChordFormula getChordFormula() {
		return chordFormula;
	}

	public void setChordFormula(ChordFormula chordFormula) {
		this.chordFormula = chordFormula;
	}

	public List<Note> getChordNotes() {
		if(chordNotes == null) {
			chordNotes = new ArrayList<>();
			notes.forEach(n -> chordNotes.add(n));
		}
		return chordNotes;
	}


}
