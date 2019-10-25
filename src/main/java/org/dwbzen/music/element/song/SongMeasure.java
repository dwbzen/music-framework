package org.dwbzen.music.element.song;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.TimeSignature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simplified Measure as it might appear in a guitar Fake Book.
 * Supports Key and Key changes, TimeSignature and changes.
 * It has a Melody line and chord changes represented as HarmonyChords
 * The purpose is to make JSON entry easy for subsequent analysis.
 * A SongMeasure that has only chords looks like this:
 *  { "measure" : { "number" : 2, "harmony" : [ { "chord" : "Bb", "beat" : 1 },  { "chord" : "Gm", "beat" : 2 } ] }, 
 * A SongMeasure with Melody example:
 *  { "measure" : { "number" : 2, "melody" : {  "notes" : [ { "pitch" : "D5", "duration" : 48, "notations" : { "type" : "eighth" } }, ... ] } },
 * Note that only the chord symbol appears for each Harmony/HarmonyChord. The complete HarmonyChord is constructed
 * after the SongMeasure instance is created from the symbol and key, the whole idea being
 * to make song entry in JSON as easy as possible by omitting any unnecessary information.
 * A SongMeasure includes a number which is always relative to the enclosing Section (that is, the
 * first measure of each Section is always 1. For simplicity, it is permissible to omit a measure
 * if the Harmony is the same as the previous SongMeasure(s).
 * 
 * @author don_bacon
 *
 */
public class SongMeasure  implements IJson {
	
	@JsonProperty("key")			private KeyLite	key = null;						// optional - include if number == 1 or it changed
	@JsonProperty("timeSignature")	private TimeSignature timeSignature = null;		// optional - include if number == 1 or it changed
	@JsonProperty("number")			private int number = 1;
	@JsonProperty("goto")			private String goTo = null;		// optional direction to go to a particular section after this SongMeasure
	@JsonProperty("harmony")		private List<Harmony> harmony = new ArrayList<Harmony>();
	@JsonProperty("melody")			private Melody melody = null;
	@JsonIgnore						private Section section = null;		// the parent Section of this SongMeasure
	/**
	 * In a Section that repeats n times, there are typically n different endings or trailing measures
	 * In sheet music that would be indicated by an ending Number >= 1 and <= n with a line over the measures in that ending.
	 * The ending property is added to SongMeasures to indicate which ending the measure is associated.
	 * If omitted, each repeat is identical.
	 * A directive to a different section, a "coda" for example is handled this way.
	 * A Section named "bridge" has repeat=2 with next = {"bridge", "coda"}, in the last 3 measures
	 * specify ending:1. First time through it goes back to the beginning of "bridge"
	 * Second time it does't play measures with ending:1 but goes to "coda" when it hits the first instance.
	 */
	@JsonProperty		private int ending = 0;
	/**
	 * If you need to jump to a specific Section. This takes priority over ending
	 * and would cause an immediate jump to measure 1 of the named section.
	 */
	@JsonProperty		private String nextSection = null;
	
	public SongMeasure() {
	}

	public SongMeasure(KeyLite key, TimeSignature ts) {
		this.key = key;
		this.timeSignature = ts;
	}
	
	/**
	 * Copy constructor. Sets number to other.number + 1. Does a deep copy of TimeSignature,
	 * keeps the Key reference.
	 * @param other SongMeasure
	 */
	public SongMeasure(SongMeasure other) {
		this(other, true);
	}
	
	/**
	 * Copy constructor. Sets number to other.getNumber() + 1. Does a deep copy of TimeSignature,
	 * keeps the Key reference.
	 * @param other SongMeasure
	 */
	public SongMeasure(SongMeasure other, boolean copyAll) {
		this.number = other.getNumber() + 1;
		if(copyAll && other.getKey() != null && other.getTimeSignature() != null) {
			TimeSignature ots = other.getTimeSignature();
			this.key = other.key;
			this.timeSignature = new TimeSignature(ots.getBeatNote(), ots.getBeats(), ots.getDivisions());
		}
	}

	public KeyLite getKey() {
		return key;
	}

	public void setKey(KeyLite key) {
		this.key = key;
	}

	public TimeSignature getTimeSignature() {
		return timeSignature;
	}

	public void setTimeSignature(TimeSignature timeSignature) {
		this.timeSignature = timeSignature;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<Harmony> getHarmony() {
		return harmony;
	}
	
	public boolean addHarmony(Harmony h) {
		h.setSongMeasure(this);
		return this.harmony.add(h);
	}

	public Section getSection() {
		return section;
	}
	public void setSection(Section sec) {
		this.section = sec;
	}

	public int getEnding() {
		return ending;
	}

	public void setEnding(int ending) {
		this.ending = ending;
	}

	public void setHarmony(List<Harmony> harmony) {
		this.harmony = harmony;
	}

	public String getNextSection() {
		return nextSection;
	}

	public void setNextSection(String nextSection) {
		this.nextSection = nextSection;
	}

	public Melody getMelody() {
		return melody;
	}

	public void setMelody(Melody melody) {
		this.melody = melody;
	}

	public String getGoTo() {
		return goTo;
	}

	public void setGoTo(String goTo) {
		this.goTo = goTo;
	}

}
