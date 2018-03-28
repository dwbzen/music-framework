package music.element;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import mathlib.Point;
import music.element.Duration.BeatUnit;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates a tempo and/or metronome marking
 * In XML this has the form:
 *  <metronome>
    	<beat-unit>quarter</beat-unit>
        <per-minute>85</per-minute>
    </metronome>
 * Tempo marking by range (in bpm) are given as:
 *  Ranges stored don't overlap.
 	Larghissimo – very, very slow (19 BPM and under)
    Grave – slow and solemn (20–40 BPM)
    Lento – slowly (40–45 BPM)
    Largo – broadly (45–50 BPM)
    Larghetto – rather broadly (50–55 BPM)
    Adagio – slow and stately (literally, "at ease") (55–65 BPM)
    Adagietto – rather slow (65–69 BPM)
    Andante moderato – a bit slower than andante (69–72 BPM)
    Andante – at a walking pace (73–77 BPM)
    Andantino – slightly faster than andante (although in some cases it can be taken to mean slightly slower than andante) (78–83 BPM)
    Marcia moderato – moderately, in the manner of a march[4][5] (83–85 BPM)
    Moderato – moderately (86–97 BPM)
    Allegretto – moderately fast (98–109 BPM)
    Allegro – fast, quickly and bright (109–132 BPM)
    Vivace – lively and fast (132–140 BPM)
    Vivacissimo – very fast and lively (140–150 BPM)
    Allegrissimo – very fast (150–167 BPM)
    Presto – extremely fast (168–177 BPM)
    Prestissimo – even faster than Presto (178 BPM and over)
 *
 * @author don_bacon
 *
 */
public final class Tempo implements Serializable, Comparable<Tempo> {

	private static final long serialVersionUID = -2764691813647880595L;

	@JsonProperty("bpm")		private Integer beatsPerMinute = 90;			// Moderato default
	@JsonProperty("beatUnit")	private BeatUnit beatUnit = BeatUnit.QUARTER;	// what kind of note or chord gets 1 beat
	
	private static Map<String, Point<Integer>> tempoMarkings = new HashMap<String, Point<Integer>>();
	
	static {	// some day want to load this from config data
		tempoMarkings.put("Larghissimo", new Point<Integer>(Integer.MIN_VALUE, 19));
		tempoMarkings.put("Grave", new Point<Integer>(20, 40));
		tempoMarkings.put("Lento", new Point<Integer>(41, 45));
		tempoMarkings.put("Largo", new Point<Integer>(46, 50));
		tempoMarkings.put("Larghetto", new Point<Integer>(51, 55));
		tempoMarkings.put("Adagio", new Point<Integer>(56, 64));
		tempoMarkings.put("Adagietto", new Point<Integer>(65, 69));
		tempoMarkings.put("Andante moderato", new Point<Integer>(70, 72));
		tempoMarkings.put("Andante", new Point<Integer>(73, 77));
		tempoMarkings.put("Andantino", new Point<Integer>(78, 83));
		tempoMarkings.put("Marcia moderato", new Point<Integer>(84, 85));
		tempoMarkings.put("Moderato", new Point<Integer>(86, 97));
		tempoMarkings.put("Allegretto", new Point<Integer>(98, 109));
		tempoMarkings.put("Allegro", new Point<Integer>(110, 132));
		tempoMarkings.put("Vivace", new Point<Integer>(133, 140));
		tempoMarkings.put("Vivacissimo", new Point<Integer>(141, 150));
		tempoMarkings.put("Allegrissimo", new Point<Integer>(151, 167));
		tempoMarkings.put("Presto", new Point<Integer>(168, 177));
		tempoMarkings.put("Prestissimo", new Point<Integer>(178, Integer.MAX_VALUE));
	}
	
	/**
	 * 
	 * @param bpm tempo in beats per minute
	 * @return Corresponding tempo marking for the tempo indicated
	 * 
	 */
	public static String getTempoMarking(int bpm) {
		String tempoMarking = null;
		for(String tm :tempoMarkings.keySet()) {
			Point<Integer> point = tempoMarkings.get(tm);
			if(point.getX().intValue()<= bpm && bpm <= point.getY().intValue()) {
				tempoMarking = tm;
				break;
			}
		}
		return tempoMarking;
	}
	
	public static int[] getTempoForTempoMarking(String tm) throws IllegalArgumentException {
		int[] tempo = new int[2];
		if(tempoMarkings.containsKey(tm)) {
			tempo[0] = tempoMarkings.get(tm).getX().intValue();
			tempo[1] = tempoMarkings.get(tm).getY().intValue();
		}
		else {
			throw new IllegalArgumentException("Invalid tempo marking: " + tm);
		}
		return tempo;
	}
	
	/**
	 * Creates a new Tempo with BeatUnit of QUARTER
	 * @param bpm beats per measure
	 */
	public Tempo(int bpm) {
		beatsPerMinute = bpm;
	}
	
	/**
	 * Creates a new Tempo with specified BeatUnit
	 * @param bpm beats per measure
	 */
	public Tempo(int bpm, BeatUnit beatUnit) {
		beatsPerMinute = bpm;
		this.beatUnit = beatUnit;
	}

	/**
	 * Translates how many beats in this tempo for a given duration in seconds
	 * @param seconds
	 * @return
	 */
	public double getBeats(double seconds) {
		return beatsPerMinute*seconds/60;
	}
	
	public Integer getBeatsPerMinute() {
		return beatsPerMinute;
	}

	public BeatUnit getBeatUnit() {
		return beatUnit;
	}

	@Override
	public int compareTo(Tempo o) {
		return beatsPerMinute.compareTo(o.getBeatsPerMinute());
	}
	
	public boolean equals(Tempo o) {
		return beatsPerMinute.equals(o.getBeatsPerMinute()) && beatUnit == o.getBeatUnit();
	}

}
