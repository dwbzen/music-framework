package music.action;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import music.element.Chord;
import music.element.Interval;
import music.element.Measurable;
import music.element.Measure;
import music.element.Note;
import music.element.Score;
import music.element.ScorePartEntity;

/**
 * Analyzes a complete Score instance.
 * Outputs stats and information in comma-separated (.csv) format
 * The output order is relative intervals (0 to 11), followed by absolute.
 * Creates a ScoreAnalysis for the entire score,
 * and a ScoreAnalysis for each instrument saved as a Map<String, ScoreAnalysis> 
 * TODO add CHORDAL analysis
 * 
 * @author don_bacon
 * @see music.action.ScoreAnalysis
 */
public class ScoreAnalyzer {
	protected static final Logger log = LogManager.getLogger(ScoreAnalyzer.class);
	private Score score;
	private ScoreAnalysis scoreAnalysis =  new ScoreAnalysis();		// Score level analysis
	private Map<String, ScoreAnalysis> instrumentAnalysisMap = new HashMap<String, ScoreAnalysis>();
	private Measurable prev;
	private boolean showDurations = false;
	private boolean showPitchFrequency = true;
	private boolean showNoteFrequency = false;
	private boolean showIntervals = false;
	private boolean showAbsoluteIntervals = false;
	
	public ScoreAnalyzer(Score score){
		this.score = score;
	}
	
	public void analyze() {
		Map<String, ScorePartEntity> parts = score.getParts();
		for(String partName : parts.keySet()) {
			ScoreAnalysis partAnalysis = new ScoreAnalysis();
			ScorePartEntity scorePartEntity = parts.get(partName);
			log.info("Analyzing " + partName);
			prev = null;
			for(Measure measure : scorePartEntity.getMeasures()) {
				analyzeMeasure(partAnalysis, partName, measure);
			}
			scoreAnalysis.addAll(partAnalysis);
			partAnalysis.setPartName(partName);
			instrumentAnalysisMap.put(partName, partAnalysis);
		}
	}

	private void analyzeMeasure(ScoreAnalysis partAnalysis, String partName, Measure measure) {
		Note note = null;
		Note prevNote = null;
		Chord chord = null;
		Chord prevChord = null;
		log.trace("measure: " + measure.getNumber());
		for(Measurable measurable : measure.getMeasureables()) {
			if(measurable instanceof Note) {
				note = (Note)measurable;
			}
			else {
				chord = (Chord)measurable;
			}
			if(prev != null) {
				if(prev instanceof Note) {
					prevNote = (Note)prev;
					if(!prevNote.isTiedTo(note)) {
						Interval diff = prevNote.getInterval(note);
						if(diff.getInterval() == 0) {
							log.trace("prev: " + prevNote + ", note: " + note + diff);
						}
						partAnalysis.addInterval(diff);
					}
				}
				else {
					// TODO: chord
					prevChord = (Chord)prev;
				}
			}
			partAnalysis.addNote(note);
			prev = note;
		}
	}
	
	
	public void displayAnalysis(PrintStream printStream) {
		ScoreAnalysis sa = getScoreAnalysis();
		displayAnalysis(printStream, sa);
		instrumentAnalysisMap.keySet().stream().forEachOrdered(key -> displayAnalysis(printStream, instrumentAnalysisMap.get(key) ));
	}
	
	public void displayAnalysis(PrintStream printStream, ScoreAnalysis sa) {
		Map<Integer, Integer[]> intervals = sa.getIntervals();
		Map<Integer, Integer[]> absoluteIntervals = sa.getAbsoluteIntervals();
		Map<String, Integer> pitchCounts = sa.getPitchCounts();
		Map<String, Integer> noteCounts = sa.getNoteCounts();
		Map<String, Integer> durCounts = sa.getDurationCounts();
		
		printStream.println(sa.getPartName());
		
		if(showIntervals) {
			printStream.println("intervals");
			for(Integer i : intervals.keySet()) {
				Integer[] counts = intervals.get(i);
				printStream.println(i + "," + counts[0] + "," + counts[1] + "," + counts[2] + "," + counts[3]);
			}
		}
		if(showAbsoluteIntervals) {
			printStream.println("absoluteIntervals");
			for(Integer i : absoluteIntervals.keySet()) {
				Integer[] counts = absoluteIntervals.get(i);
				printStream.println(i + "," + counts[0] + "," + counts[1] + "," + counts[2] + "," + counts[3]);
			}
		}
		if(showNoteFrequency) {
			printStream.println("note frequency");
			for(String key: noteCounts.keySet()) {
				printStream.println(key + "," + noteCounts.get(key));
			}
		}
		if(showPitchFrequency) {
			printStream.println("pitch frequency");
			for(String key: pitchCounts.keySet()) {
				printStream.println(key + "," + pitchCounts.get(key));
			}
		}
		if(showDurations) {
			printStream.println("Durations");
			for(String d : durCounts.keySet()) {
				printStream.println(d + ", " + durCounts.get(d));
			}
		}
	}

	public Score getScore() {
		return score;
	}

	public ScoreAnalysis getScoreAnalysis() {
		return scoreAnalysis;
	}

	public boolean isShowDurations() {
		return showDurations;
	}

	public void setShowDurations(boolean showDurations) {
		this.showDurations = showDurations;
	}

	public boolean isShowPitchFrequency() {
		return showPitchFrequency;
	}

	public void setShowPitchFrequency(boolean showPitchFrequency) {
		this.showPitchFrequency = showPitchFrequency;
	}

	public boolean isShowNoteFrequency() {
		return showNoteFrequency;
	}

	public void setShowNoteFrequency(boolean showNoteFrequency) {
		this.showNoteFrequency = showNoteFrequency;
	}

	public boolean isShowIntervals() {
		return showIntervals;
	}

	public void setShowIntervals(boolean showIntervals) {
		this.showIntervals = showIntervals;
	}

	public boolean isShowAbsoluteIntervals() {
		return showAbsoluteIntervals;
	}

	public void setShowAbsoluteIntervals(boolean showAbsoluteIntervals) {
		this.showAbsoluteIntervals = showAbsoluteIntervals;
	}
	
}
