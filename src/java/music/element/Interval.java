package music.element;

import java.io.Serializable;

public class Interval implements Serializable {
	private static final long serialVersionUID = 4723660592073647534L;

	public static enum Direction {
		Up(1), Down(-1), Nill(0);
		Direction(int val)  { this.value = val;}
		private final int value;
	    public int value() { return value; }
	}
	
	private int interval;
	private int octave = 0;
	private Direction direction;
	
	public Interval(int steps) {
		direction = (steps<0) ? Direction.Down : ( (steps == 0) ? Direction.Nill : Direction.Up );
		interval = Math.abs(steps);
		if(interval >= 12) {
			octave = interval / 12;
			interval = interval % 12;
		}
	}

	public int getInterval() {
		return interval;
	}

	public Direction getDirection() {
		return direction;
	}
	
	/**
	 * Equal PitchDifference have same interval (including octave) and direction
	 * @param other
	 * @return
	 */
	public boolean equals(Interval other) {
		return direction.equals(other.getDirection()) && 
								(interval == other.getInterval() &&
								(octave == other.getOctave()));
	}
	
	/**
	 * Same PitchDifference have same interval, direction doesn't count
	 * @param other
	 * @return
	 */
	public boolean same(Interval other) {
		return (interval == other.getInterval());
	}

	public int getOctave() {
		return octave;
	}

	public int toSteps() {
		return (interval + (octave * 12)) * direction.value();
	}
	
	public boolean isZero() {
		return interval == 0 && octave == 0;
	}
	
	public String toString() {
		String s = "{ interval:" + interval + ", octave:" + octave + ", dir:" + direction.value + "}";
		return s;
	}
}
