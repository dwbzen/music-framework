package music.element;

/**
 * All the modes can be derived from any diatonic scale by rotation left
 * which essentially changes the root.
 * Diatonic Major scale is equivalent to Ionian mode. 
 * Dorian == MAJOR rotate left 1
 * Phrygian == MAJOR rotate left 2 (Dorian rotate left 1)
 * Lydian == MAJOR rotate left 3  (Phrygian rotate left 1)
 * Mixolydian == MAJOR rotate left 4  (Lydian rotate left 1)
 * Aeolian == MAJOR rotate left 5  (Mixolydian rotate left 1) Equivalent to Natural Minor
 * Locrian == MAJOR rotate left 6  (Aeolian rotate left 1)
 * None - if not applicable
 * @author don_bacon
 *
 */
public enum Mode {
	Ionian(0), Dorian(1), Phrygian(2), Lydian(3), 
	Mixolydian(4), Aeolian(5), Locrian(6), None(13);
	
	Mode(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
}
