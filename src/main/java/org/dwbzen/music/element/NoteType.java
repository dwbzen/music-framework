package org.dwbzen.music.element;

/**
 * whole, half, quarter, eighth, 16th, 32nd, 64th<br>
 * A single dot can be added to any note >= half and <= 32nd<br>
 * Two dots can be added to any note >= half and <= 16th<br>
 * Three dots can be added to any note >= half and <= eighth
 * 
 * @author don_bacon
 *
 */
public enum NoteType {

	NONE(""),
	WHOLE("whole"),
	HALF("half"), DOTTED_HALF("half",1), DOUBLE_DOTTED_HALF("half",2), TRIPLE_DOTTED_HALF("half",3),
	QUARTER("quarter"), DOTTED_QUARTER("quarter",1), DOUBLE_DOTTED_QUARTER("quarter",2), TRIPLE_DOTTED_QUARTER("quarter",3),
	EIGHTH("eighth"), DOTTED_EIGHTH("eighth",1), DOUBLE_DOTTED_EIGHTH("eighth",2), TRIPLE_DOTTED_EIGHTH("eighth",3), 
	SIXTEENTH("16th"), DOTTED_SIXTEENTH("16th",1), DOUBLE_DOTTED_SIXTEENTH("16th",2),
	THIRTY_SECOND("32nd"), DOTTED_THIRTY_SECOND("32nd",1),
	SIXTY_FOURTH("64th");
	
	private String value;
	private int dots = 0;
	
	private NoteType(String val) {
		this(val,0);
	}
	private NoteType(String val, int ddots) {
		value = val;
		dots = ddots;
	}
	
    public String value() {
        return value;
    }
    public int getDots() {
    	return dots;
    }
    
    /**
     * Shows the dots as period appended to the note type.
     * 
     * @param showDots
     * @return
     */
    public String toString(boolean showDots) {
    	if(dots <= 0) {
    		return value;
    	}
    	else {
    		switch(dots) {
    		case 1:
    			return showDots ? value + "." : "dotted " + value;
			case 2:
				return showDots ? value + ".." : "double-dotted " + value;
			case 3:
				return showDots ? value + "..." : "tripple-dotted " + value;
    		default:
    			return showDots ? value + "-" + dots +  "." : dots + "-dotted" + value;
    		}
    	}
    }
	
    public String toString() {
    	return toString(false);
    }
}
