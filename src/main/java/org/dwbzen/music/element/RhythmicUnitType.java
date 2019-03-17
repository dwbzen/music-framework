package org.dwbzen.music.element;


/**
 * <ul>
 * <li>METRIC - one note per duration</li>
 * <li>EXTRAMETRIC - tuplets - 3, 5, or 7 notes per duration</li>
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
