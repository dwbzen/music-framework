package music.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import music.instrument.Instrument;
import music.instrument.MidiInstrument;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

/**
 * The music data portion of a ScorePart
 * @author don_bacon
 *
 */
@Entity("ScorePart")
public class ScorePartEntity implements Serializable {

	private static final long serialVersionUID = -8305108753987313360L;
	@Id		private ObjectId	id = new ObjectId();
	@Property("partName")	private String partName;
	@Property("partNumber")	private int partNumber;
	@Property("partId")		private String partId;
	@Property("scoreKey")   private Key scoreKey = null;	// set from configuration by ScorePart

	
	/**
	 * A linked List of Measures
	 */
	@Reference		private List<Measure> measures = new ArrayList<Measure>();
	@Transient		private Instrument instrument;

	/**
	 * Corresponding midi instrument for this Part - could be null
	 */
	@Transient		private MidiInstrument midiInstrument;
	@Transient		private Score score;	// parent Score

	public ScorePartEntity(Score score, String partName, Instrument instrument) {
		this.partName = partName;
		this.instrument = instrument;
		this.score = score;
	}
	
    public String toString() {
    	StringBuffer sb = new StringBuffer(partName);
    	for(Measure measure: getMeasures()) {
    		sb.append(measure.toString() + "\n");
    	}
    	return sb.toString();
    }


	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	public List<Measure> getMeasures() {
		return measures;
	}

	public void setMeasures(List<Measure> measures) {
		this.measures = measures;
	}

	public MidiInstrument getMidiInstrument() {
		return midiInstrument;
	}

	public void setMidiInstrument(MidiInstrument midiInstrument) {
		this.midiInstrument = midiInstrument;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public Score getScore() {
		return score;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public String getPartId() {
		return partId;
	}

	public void setPartId(String partId) {
		this.partId = partId;
	}

	public Key getScoreKey() {
		return scoreKey;
	}

	public void setScoreKey(Key scoreKey) {
		this.scoreKey = scoreKey;
	}
	
}
