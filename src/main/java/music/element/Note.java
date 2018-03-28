package music.element;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A pitch + Duration. Can also be a rest (no pitch).
 * Not to be confused with  com.audiveris.proxymusic.Note
 * 
 * A Note is a RhythmElement with the addition of a Pitch.
 * 
 * @author don_bacon
 *
 */
public class Note extends Measurable implements Serializable, Comparable<Note>, Cloneable {

	private static final long serialVersionUID = 1774493820041575241L;
	
	@JsonProperty("pitch")	private Pitch pitch = null;		// will be null for a rest
	@JsonProperty("rest")	private boolean rest = true;	// set automatically by constructor from Pitch
	@JsonIgnore				private Note tiedTo = null;		// reference to the note this is tied to - occurs after this note
	@JsonIgnore				private Note tiedFrom = null;	//  reference to the note the note this is tied from - occurs before this note
	@JsonIgnore				private IMeasurableContainer<Note>	container = null;	// reference to container (like a Chord) or null

	public Note(Pitch p, Duration dur) {
		setPitch(new Pitch(p));	// also sets rest
		setDuration(new Duration(dur));
	}
	
	public Note(Pitch p, int dur) {
		setPitch(new Pitch(p));	// also sets rest
		setDuration(new Duration(dur));
	}

	/**
	 * Deep Copy constructor.
	 * Creates a new Note from an existing one.
	 * They will have the same Pitch, Duration, TupletType, voice# and noteType.
	 * It does NOT copy tiedTo, tiedFrom Note references!
	 * @param prevNote a Note instance
	 */
	public Note(Note aNote) {
		super();
		pitch = new Pitch(aNote.getPitch());
		rest = aNote.rest;
		duration = new Duration(aNote.getDuration());
		tupletType = aNote.tupletType;
		noteType = aNote.noteType;
		if(aNote.getContainer() != null) {
			container = aNote.getContainer();
		}
		setPoint(aNote.getPoint());
		setVoice(aNote.getVoice());
	}
	
	/**
	 * Creates a new Note from an existing one and optionally ties the two together.
	 * @param prevNote a Note instance
	 */
	public Note(Note aNote, boolean tieFlag) {
		this(aNote);
		if(tieFlag) {
			aNote.setTiedTo(this);
			setTiedFrom(aNote);
		}
	}
	
	/**
	 * Makes a clone of this including references to tied notes.
	 * 
	 */
	@Override
	public Note clone() {
		return new Note(this, true);
	}
	
	public Pitch getPitch() {
		return pitch;
	}

	public void setPitch(Pitch pitch) {
		this.pitch = pitch;
		setRest(pitch == null);
	}

	public void setPitchTo(Pitch other) {
		if(other != null) {
			this.pitch = new Pitch(other);
		}
	}

	public boolean isRest() {
		return rest;
	}

	private final void setRest(final boolean rest) {
		this.rest = rest;
	}

	public Note getTiedTo() {
		return tiedTo;
	}

	public Note getTiedFrom() {
		return tiedFrom;
	}

	public void setTiedTo(Note newTiedTo) {
		Note oldTiedTo = this.tiedTo;
		this.tiedTo = newTiedTo;
		if(newTiedTo == null) {
			tieType = (tiedFrom == null) ? TieType.NONE : TieType.STOP;
			if(oldTiedTo != null) {
				oldTiedTo.setTiedFrom(null);
			}
		}
		else {	// newTiedTo != null
			tieType = (tiedFrom == null) ? TieType.START : TieType.BOTH;
		}
	}

	public void setTiedFrom(Note newTiedFrom) {
		Note oldTiedFrom = this.tiedFrom;
		this.tiedFrom = newTiedFrom;
		if(newTiedFrom == null) {
			tieType = (tiedTo == null) ? TieType.NONE : TieType.START;
			if(oldTiedFrom != null) {
				oldTiedFrom.setTiedTo(null);
			}
		}
		else {	// newTiedFrom != null
			tieType = (tiedTo == null) ? TieType.STOP : TieType.BOTH;
		}
	}
	
	public void breakAllTies() {
		setTiedFrom(null);
		setTiedTo(null);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("Note: " + pitch.toString() + " duration: " + duration.toString());
		sb.append(" type: " + noteType + " tie: " + tieType);
		if(tiedFrom != null) {
			sb.append(" tiedFrom: " + tiedFrom.getPitch().toString() + " " + tiedFrom.getDuration());
		}
		if(tiedTo != null) {
			sb.append(" tiedTo: " + tiedTo.getPitch().toString() + " " +  tiedTo.getDuration());
		}
		return sb.toString();
	}
	
	@Override
	public int compareTo(Note other) {
		int pc = duration.compareTo(other.getDuration());
		if(pc == 0 && !(rest || other.rest)) {	// same duration, neither one a rest so compare pitches
			pc = pitch.compareTo(other.getPitch());
		}
		return pc;
	}
	
	/**
	 * Notes are equal if they have the same duration,
	 * and if neither is a rest, the pitches are equal
	 * and same grace value.
	 * @param o
	 * @return
	 */
	public boolean equals(Note other) {
		boolean eq = (compareTo(other) == 0);
		if(eq) {	// duration and possibly the pitch are the same
			eq = other.getDuration().isGrace() == other.getDuration().isGrace() && rest == other.rest;
		}
		return eq;
	}
	
	@Override
	protected void setMeasurableType() {
		setType(Measurable.NOTE);
	}
	
	public int difference(Note other, boolean absoluteFlag) {
		return absoluteFlag ? absoluteDifference(other) : difference(other);
	}
	
	/**
	 * Computes this Note - other.Note
	 * If == 0, the step is the same
	 * If < 0, this is below other
	 * else this is above other
	 * Note that this does NOT take octave differences into account.
	 * Use absoluteDifference to include octave difference as well as pitch
	 * 
	 * @param other Note
	 * @return int difference in terms of #chromatic steps (0 to 12)
	 */
	public int difference(Note other) {
		return this.getPitch().difference(other.getPitch());
	}
 
	public int absoluteDifference(Note other) {
		if(other == null) {
			System.err.println(toString());
			throw new IllegalArgumentException("absoluteDifference: other note is null");
		}
		return  this.getPitch().absoluteDifference(other.getPitch());
	}
	
	public Interval getInterval(Note other) {
		return new Interval(absoluteDifference(other));
	}

	/**
	 * Check if this note is tied to the nextNote
	 * That is if this.tiedTo == nextNote.tiedFrom
	 * @param other the Note immediately following this one in a measure
	 * @return true if this is tied to other Note
	 */
	public boolean isTiedTo(Note nextNote) {
		boolean tied = false;
		if(tiedTo != null && nextNote != null) {
			tied =  tiedTo.equals(nextNote);
		}
		return tied;
	}

	public IMeasurableContainer<Note> getContainer() {
		return container;
	}

	public void setContainer(IMeasurableContainer<Note> container) {
		this.container = container;
	}
	
}
