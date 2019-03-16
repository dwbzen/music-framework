package music.element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.math.IntegerPair;
import org.dwbzen.common.util.IJson;
import util.Ratio;

public class Duration implements IJson, Comparable<Duration> {

	private static final long serialVersionUID = -2806795943905155955L;
	
	/**
	 * total units including added dots. In MusicXML: <duration>n</duration>
	 */
	@JsonProperty("units")		private int durationUnits = 0;
	
	/**
	 * base units - does not include additional units from added "dots"
	 */
	@JsonProperty("baseUnits")	private int baseUnits = 0;
	
	/**
	 * raw duration value
	 */
	@JsonIgnore	private double rawDuration = 0.0;

	/** 
	 * Beat units are any of the following:
	 * whole, half, quarter, eighth, 16th, 32nd etc.
	 */
	public static enum BeatUnit {
		WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH, THIRTY_SECOND, SIXTY_FOURTH, ONE_TWENTY_EIGHTH
	};
	public static String[] BeatUnitNames = {
		"whole", "half", "quarter", "eighth", "16th", "32nd", "64th", "128th"
	};
	
		
	/**
	 * for specifying tuplets ratio[0] in the time of ratio[1]
	 * Example, triplets would be {3, 2} - 3 notes played in the time of 2
	 */
	@JsonProperty("ratio")	private Ratio ratio = Ratio.ONE_TO_ONE;
	@JsonIgnore				private boolean grace = false;	// grace note or chord
	
	/**
	 * Number of dots qualifying the note for this duration. Can be calculated
	 * from duration units and measure divisions
	 */
	@JsonProperty("dots")		private int dots = 0;
	
	public Duration(int dur) {
		durationUnits = dur;
		baseUnits = dur;
	}
	
	public Duration(int dur, int ddots) {
		setDurationUnits(dur, ddots);
	}
	
	public Duration(double dur) {
		rawDuration = dur;
	}

	/**
	 * Copy constructor
	 * @param d Duration to copy
	 */
	public Duration(Duration d) {
		rawDuration = d.getRawDuration();
		durationUnits = d.getDurationUnits();
		baseUnits = d.getBaseUnits();
		grace = d.isGrace();
		dots = d.getDots();
		setRatio(new Ratio(d.getRatio()));
	}
	
	public int getNumberOfNotes() {
		return ratio.getBeats();
	}
	public int getInTheTimeOfNotes() {
		return ratio.getTimeOf();
	}
	
	public int getDurationUnits() {
		return durationUnits;
	}
	public void setDurationUnits(int duration) {
		durationUnits = duration;
		baseUnits = duration;
	}
	
	/**
	 * Sets durationUnits and dots. durationUnits = units + dots units
	 * Also sets baseUnits (without the dots)
	 * @param units
	 * @param dots
	 */
	public void setDurationUnits(int units, int dots) {
		durationUnits = units;
		baseUnits = units;
		this.dots = dots;
		if(dots > 0) {
			for(int i=1; i<=dots; i++) {
				durationUnits += units / Math.pow(2, i);
			}
		}
	}
	
	public Ratio getRatio() {
		return ratio;
	}
	public void setRatio(Ratio ratio) {
		this.ratio = ratio;
	}
	public void setRatio(IntegerPair pair) {
		ratio = new Ratio(pair.getX(), pair.getY());
	}
	
	public boolean isRatioSame() {
		return ratio.getNumberOfNotes()==ratio.getTimeOf();
	}
	
	public boolean isTuplet() {
		return ratio.getNumberOfNotes()!=ratio.getTimeOf();
	}
	
	public boolean isGrace() {
		return grace;
	}
	public void setGrace(boolean grace) {
		this.grace = grace;
	}

	public double getRawDuration() {
		return rawDuration;
	}

	public void setRawDuration(double d) {
		rawDuration = d;
	}
	
	public int getBaseUnits() {
		return baseUnits;
	}

	public void setBaseUnits(int baseUnits) {
		this.baseUnits = baseUnits;
	}

	public String toString() {
		//String s = ((durationUnits == 0) ? rawDuration + " sec." : durationUnits + " div.") + " dots: " + dots;
		return toJson();
	}
	
	@Override
	public int compareTo(Duration o) {
		int compar = Integer.valueOf(durationUnits).compareTo(Integer.valueOf(o.durationUnits));
		return compar;
	}
	public int getDots() {
		return dots;
	}
	public void setDots(int dots) {
		this.dots = dots;
	}


}
