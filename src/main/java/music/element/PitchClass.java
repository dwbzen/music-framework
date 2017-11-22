package music.element;

public enum PitchClass {
	UNPITCHED(0), 
	PITCHED(1), 
	DISCRETE_5LINE(5),
	DISCRETE_2LINE(2),
	DISCRETE_1LINE(0);
	
	PitchClass(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
}
