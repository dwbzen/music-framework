package org.dwbzen.music.element;

/**
 * Yes the spelling is incorrect - not to be confused w/com.audiveris.proxymusic.Clef
 * The octaveShift specifies the #octaves to alter the pitch -1 means transpose DOWN 1 octave.
 * That's because the cleff will have a 8ma designation.
 * 
 * @author don_bacon
 *
 */
public enum Cleff {
    G("G"),
    G8va("G", -1),	/* G-clef 1 octave higher than written */
    G15va("G", -2),	/* G-clef 2 octaves higher than written */
    G8ma("G", 1),
    F("F"),
    F8ma("F", 1),	/* F-clef 1 octave lower than written */
    F15ma("F", 2),	/* F-clef 2 octaves lower than written */
    C("C"),
    TENOR("C"),
    PERCUSSION("percussion"),	/* unpitched 5-line percussion */
    PERCUSSION_2LINE("percussion 2-line"),		/* unpitched 2-line percussion for Cowbell etc. */
    TAB("TAB"),
    NONE("none");

    private String value;
    private int octaveShift;
    
    Cleff(String v) {
    	this(v,0);
    }
    
    Cleff(String v, int octaveShift) {
        this.value = v;
        this.octaveShift = octaveShift;
    }

    public String value() {
        return value;
    }

    public static Cleff fromValue(String v) {
        for (Cleff c: Cleff.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public int getOctaveShift() {
    	return this.octaveShift;
    }
}
