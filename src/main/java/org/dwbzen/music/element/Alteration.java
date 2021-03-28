package org.dwbzen.music.element;


/**
 * Expresses the alteration to apply to a given pitch <br>
 * and the preference when an enharmonic equivalence is applied:<br>
 * SHARP, FLAT, or NONE.
 * 
 * @author don_bacon
 *
 */
public enum Alteration {
	UP_ONE(1), SHARP(1),
	UP_TWO(2), DOUBLE_SHARP(2),
	DOWN_ONE(-1), FLAT(-1),
	DOWN_TWO(-2), DOUBLE_FLAT(-2),
	NONE(0);
	Alteration(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
}
