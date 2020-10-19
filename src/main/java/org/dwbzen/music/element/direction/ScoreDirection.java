package org.dwbzen.music.element.direction;

/**
 * Encapsulates directions that apply to a Measure such as measure/system text, metronome setting<br>
 * 
 * @author don_bacon
 * @see org.audiveris.proxymusic.Direction
 * @see org.audiveris.proxymusic.DirectionType
 */
public class ScoreDirection {
	private int staff = 1;
	private DirectionType directionType = null;
	private String placement = null;		// "above", "below" or not specified
	
	public ScoreDirection(int staff, DirectionType directionType) {
		this.staff = staff;
		this.directionType = directionType;
	}

	public int getStaff() {
		return staff;
	}

	public void setStaff(int staff) {
		this.staff = staff;
	}

	public DirectionType getDirectionType() {
		return directionType;
	}

	public void setDirectionType(DirectionType directionType) {
		this.directionType = directionType;
	}

	public String getPlacement() {
		return placement;
	}

	public void setPlacement(String placement) {
		this.placement = placement;
	}
		
}
