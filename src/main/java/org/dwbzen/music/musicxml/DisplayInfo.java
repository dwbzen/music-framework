package org.dwbzen.music.musicxml;

/**
 * Encapsulates the musicXML print element, including staff and system layouts<br>
 * margins and other non-musical elements. Essentially the musicXML Print class.<br>
 * DisplayInfo is associated with Measure.
 * Sample musicXML with default values (except new-system)
 * <pre>
 *     &lt;print new-system="yes">
        &lt;system-layout>
          &lt;system-margins>
            &lt;left-margin>21.00&lt;/left-margin>
            &lt;right-margin>-0.00&lt;/right-margin>
          &lt;/system-margins>
          &lt;system-distance>150.00&lt;/system-distance>
        &lt;/system-layout>
        &lt;staff-layout number="2">
          &lt;staff-distance>65.00&lt;/staff-distance>
        &lt;/staff-layout>
      &lt;/print>
 * </pre>
 *  
 * @author don_bacon
 * @see org.audiveris.proxymusic.Print
 *
 */
public class DisplayInfo {

	private boolean newSystem = false;
	private double system_left_margin = 21.0;
	private double system_right_margin = 0.0;
	private double system_distance = 170.0;
	
	private double staff_distance = 65.0;
	private int staff_number = 2;
	
	public DisplayInfo() {
		
	}

	public boolean isNewSystem() {
		return newSystem;
	}

	public void setNewSystem(boolean newSystem) {
		this.newSystem = newSystem;
	}

	public double getSystem_left_margin() {
		return system_left_margin;
	}

	public void setSystem_left_margin(double system_left_margin) {
		this.system_left_margin = system_left_margin;
	}

	public double getSystem_right_margin() {
		return system_right_margin;
	}

	public void setSystem_right_margin(double system_right_margin) {
		this.system_right_margin = system_right_margin;
	}

	public double getSystem_distance() {
		return system_distance;
	}

	public void setSystem_distance(double system_distance) {
		this.system_distance = system_distance;
	}

	public double getStaff_distance() {
		return staff_distance;
	}

	public void setStaff_distance(double staff_distance) {
		this.staff_distance = staff_distance;
	}

	public int getStaff_number() {
		return staff_number;
	}

	public void setStaff_number(int staff_number) {
		this.staff_number = staff_number;
	}
		
}
