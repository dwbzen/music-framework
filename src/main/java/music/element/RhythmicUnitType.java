package music.element;


/**
 * METRIC - one note per duration
 * EXTRAMETRIC - tuplets
 * @see https://en.wikipedia.org/wiki/Rhythm
 * @author don_bacon
 *
 */
public enum RhythmicUnitType {
	METRIC(0), EXTRAMETRIC(1);
	
	RhythmicUnitType(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
    public String toString() {
    	return (value==0)?"METRIC" : "TUPLET";
    }
}
