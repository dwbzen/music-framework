package org.dwbzen.music.action;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import org.dwbzen.music.element.Interval;
import org.dwbzen.music.element.Interval.Direction;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;

/**
 * ScoreAnalysis consists of two interval maps, Note and Pitch frequencies, and Durations.
 * Results are comma-separated suitable for importing into Excel.
 * 
 * Each map is keyed by the interval (absolute value of #steps separating a note from its predecessor)
 * The intervals map is a map of relative intervals, i.e. within an octave, so the rage is 0 - 11
 * The absoluteIntervals map takes octaves into account.
 * 
 * There are 4 counts for each interval key.
 * count[0] = Direction.Down
 * count[1] = Direction.Up
 * count[3] = no change
 * count[4] = sum of counts[0..3]
 * For example 7,7,12,0,19 relative interval means for a interval of 7 steps,
 * 	there are 7 where the direction is Down, 12 where the direction is Up, an 0 where there is no change.
 * Simiarly for absolute intervals, 17,1,4,0,5 specifies the interval direction for an
 *  absolute interval of 17 steps.
 * 
 * Note and Pitch frequencies are useful in analyzing the note spread of a particular scale transform.
 * Note Frequency - gives the #occurrences of each note in the score. For example:
 * 	A4,9
 *  A5,4
 *  Bb4,7
 * 
 * Pitch frequency - gives the #occurrences of each pitch in the score. For example (scale was Hirajosji Japan, root D)
 *  A,13
 *  Bb,9
 *  D,42
 *  E,16
 *  F,24
 *  
 * Durations - gives the #occurrences of the unique note durations. Useful in analyzing RhythmScales. For example,
 *  { "units" : 1 , "baseUnits" : 1 , "ratio" : { "beats" : 1 , "timeOf" : 1} , "dots" : 0}, 28
 *  { "units" : 12 , "baseUnits" : 8 , "ratio" : { "beats" : 1 , "timeOf" : 1} , "dots" : 1}, 2
 *  { "units" : 16 , "baseUnits" : 16 , "ratio" : { "beats" : 1 , "timeOf" : 1} , "dots" : 0}, 1
 *  { "units" : 2 , "baseUnits" : 2 , "ratio" : { "beats" : 1 , "timeOf" : 1} , "dots" : 0}, 17
 *
 * Derived statistics such as %usage, std. dev. etc. not included as that kind of data
 * can be obtained easily by importing into a spreadsheet.
 * 
 * partName is "Score" if the analysis is for the Score as a whole,
 * or the name of the part from the configuration, as in score.parts.Clarinet.partName=Clarinet for example
 * 
 */
public class ScoreAnalysis {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ScoreAnalysis.class);
	
	/**
	 * map of intervals within an octave (0 - 11)
	 * so diff C0 to E1 would be 4, diff E1 to C0 is -4 (down 4 steps)
	 */
	private Map<Integer,Integer[]> intervals = new TreeMap<Integer, Integer[]>();
	
	/**
	 * map of intervals taking octave differences into account
	 * so diff C0 to E1 would be 12 + 4 = 16
	 */
	private Map<Integer, Integer[]> absoluteIntervals = new TreeMap<Integer, Integer[]>();
	
	private Map<String, Integer> pitchCounts = new TreeMap<String, Integer>();
	
	private Map<String, Integer> noteCounts = new TreeMap<String, Integer>();
	
	private Map<String, Integer> durationCounts = new TreeMap<String, Integer>();
	
	private String partName = null;
	
	public ScoreAnalysis() {
		partName = "Score";
	}
	
	public ScoreAnalysis(String partName) {
		this.partName = partName;
	}

	/**
	 * Copy constructor
	 * @param sa
	 */
	public ScoreAnalysis(ScoreAnalysis sa, String partName) {
		this.partName = partName;
		addAll(sa);
	}
	
	protected void addAll(ScoreAnalysis sa) {
		addAllIntervals(sa.intervals, intervals);
		addAllIntervals(sa.absoluteIntervals, intervals);
		addAllCounts(sa.pitchCounts, pitchCounts);
		addAllCounts(sa.noteCounts, noteCounts);
		addAllCounts(sa.durationCounts, durationCounts);
	}
	
	void addAllIntervals(Map<Integer, Integer[]> other, Map<Integer, Integer[]> intervalMap) {
		for(Integer key : other.keySet()) {
			Integer[] othercounts = other.get(key);
			Integer[] counts = null;
			if(intervalMap.containsKey(key)) {
				counts = intervalMap.get(key);
			}
			else {
				counts = new Integer[4];
				Arrays.fill(counts, 0);
			}
			for(int i=0; i<othercounts.length; i++) {
				counts[i] += othercounts[i];
			}
			intervalMap.put(key, counts);
		}
	}
	
	void addAllCounts(Map<String, Integer> other, Map<String, Integer> countsMap) {
		for(String key : other.keySet()) {
			Integer otherCounts = other.get(key);
			Integer counts = 0;
			if(countsMap.containsKey(key)) {
				counts = countsMap.get(key);
			}
			counts += otherCounts;
			countsMap.put(key, counts);
		}
	}
	
	/**
	 * Change sharps to flats when adding Note
	 * @param note
	 */
	public void addNote(Note note) {
		Pitch p = note.getPitch();
		int alt = p.getAlteration();
		Pitch pitch = (alt > 0) ? p.setEnharmonicEquivalent(alt) : p;

		String noteKey = pitch.toString();			// step + octave as in "C#4"
		String pitchKey = pitch.toString(-1);		// just the step as in "C" or "Eb"
		//System.out.println(pitchKey + "    " + noteKey);
		int pitchCount = 1;
		int noteCount = 1;
		int durCount = 1;
		if(pitchCounts.containsKey(pitchKey)) {
			pitchCount = 1+ pitchCounts.get(pitchKey).intValue();
		}
		pitchCounts.put(pitchKey, Integer.valueOf(pitchCount));
		
		if(noteCounts.containsKey(noteKey)) {
			noteCount = 1+ noteCounts.get(noteKey).intValue();
		}
		noteCounts.put(noteKey, Integer.valueOf(noteCount));
		String durKey = note.getDuration().toJson();
		if(durationCounts.containsKey(durKey)) {
			durCount = 1 + durationCounts.get(durKey);
		}
		durationCounts.put(durKey, durCount);
	}
	
	public void addInterval(Interval pd) {
		Integer[] counts = null;
		Integer iv = pd.getInterval();
		Interval.Direction dir = pd.getDirection();
		if(intervals.containsKey(iv)) {
			counts = intervals.get(iv);
			if(dir.equals(Direction.Down)) {
				counts[0]++;
			}
			else if(dir.equals(Direction.Up)){
				counts[1]++;
			}
			else {
				counts[2]++;
			}
			counts[3]++;
		}
		else {
			counts = new Integer[4];
			if(dir.equals(Direction.Down)) {
				counts[0] = 1;
				counts[1] = 0;
				counts[2] = 0;
			}
			else if(dir.equals(Direction.Up)){
				counts[0] = 0;
				counts[1] = 1;
				counts[2] = 0;
			}
			else {
				counts[0] = 0;
				counts[1] = 0;
				counts[2] = 1;
			}
			counts[3] = 1;
			intervals.put(iv, counts);
		}
		addAbsoluteInterval(pd);
	}

	public void addAbsoluteInterval(Interval pd) {
		Integer[] counts = null;
		int steps = pd.toSteps();
		Integer absSteps = Math.abs(steps);
		if(absoluteIntervals.containsKey(absSteps)) {
			counts = absoluteIntervals.get(absSteps);
			if(steps < 0) {
				counts[0]++;
			}
			else if(steps > 0) {
				counts[1]++;
			}
			else {
				counts[2]++;
			}
			counts[3]++;
		}
		else {
			counts = new Integer[4];
			if(steps < 0) {
				counts[0] = 1;
				counts[1] = 0;
				counts[2] = 0;
			}
			else if(steps > 0){
				counts[0] = 0;
				counts[1] = 1;
				counts[2] = 0;
			}
			else {
				counts[0] = 0;
				counts[1] = 0;
				counts[2] = 1;				
			}
			counts[3] = 1;
			absoluteIntervals.put(absSteps, counts);
		}
	}

	public Map<Integer, Integer[]> getIntervals() {
		return intervals;
	}

	public Map<Integer, Integer[]> getAbsoluteIntervals() {
		return absoluteIntervals;
	}

	public Map<String, Integer> getPitchCounts() {
		return pitchCounts;
	}

	public Map<String, Integer> getNoteCounts() {
		return noteCounts;
	}

	public Map<String, Integer> getDurationCounts() {
		return durationCounts;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}
	
}
