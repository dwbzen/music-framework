package music.element;

import java.io.Serializable;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

/**
 * Represents a discrete unit of time as in notation for instruments
 * with no distinct pitch (drum, cymbal etc.)
 * Rhythm Scales can be constructed which are analagous to Pitch scales
 * in ascending time duration order.
 * Vertical stacking of RhythmElements (RhythmChord) is the analog
 * of Pitch chords. For example a pair of Cowbells or Cymbals.
 * 
 * @author don_bacon
 * @See RhythmScale
 */
@Embedded
@Entity(value="RhythmElement")
public class RhythmElement extends Measurable implements Serializable, Comparable<RhythmElement> {

	private static final long serialVersionUID = -6103237755837158646L;
	
	@Property("rest")		private boolean rest = false;
	@Transient	protected RhythmElement tiedTo = null;		// reference to the note this is tied to - occurs after this note
	@Transient	protected RhythmElement tiedFrom = null;	//  reference to the note the note this is tied from - occurs before this note
	@Transient	protected IMeasurableContainer<RhythmElement>	container = null;	// reference to container (like a Chord) or null

	public RhythmElement(Duration dur) {
		setDuration(dur);
	}

	@Override
	public int compareTo(RhythmElement o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setMeasurableType() {
		setType(Measurable.RHYTHM);
	}
	
	public boolean isRest() {
		return rest;
	}

	private void setRest(final boolean rest) {
		this.rest = rest;
	}
	
	public RhythmElement getTiedTo() {
		return tiedTo;
	}

	public RhythmElement getTiedFrom() {
		return tiedFrom;
	}
	
	public void setTiedTo(RhythmElement newTiedTo) {
		RhythmElement oldTiedTo = this.tiedTo;
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

	public void setTiedFrom(RhythmElement newTiedFrom) {
		RhythmElement oldTiedFrom = this.tiedFrom;
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

	public IMeasurableContainer<RhythmElement> getContainer() {
		return container;
	}

	public void setContainer(IMeasurableContainer<RhythmElement> container) {
		this.container = container;
	}

}
