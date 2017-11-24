package music.element;


/**
 * METRIC - one note per duration
 * EXTRAMETRIC - tuplets
 * <p>For background information see <a href="https://en.wikipedia.org/wiki/Rhythm">Rhythm</a> on Wikipedia
 * 
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
