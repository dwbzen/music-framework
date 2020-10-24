package org.dwbzen.music.element;

/**
 * A special barline placed at the start (left) or end (right) of a measure.
 * Valid barline styles are
 * 
 *  <pre>
 *     &lt;enumeration value="regular"/>
 *     &lt;enumeration value="dotted"/>
 *     &lt;enumeration value="dashed"/>
 *     &lt;enumeration value="heavy"/>
 *     &lt;enumeration value="light-light"/>
 *     &lt;enumeration value="light-heavy"/>
 *     &lt;enumeration value="heavy-light"/>
 *     &lt;enumeration value="heavy-heavy"/>
 *     &lt;enumeration value="tick"/>
 *     &lt;enumeration value="short"/>
 *     &lt;enumeration value="none"/>
 * </pre>
 * 
 * @author don_bacon
 * @see org.audiveris.proxymusic.BarStyle
 *
 */
public class Barline {
	
	private String location = null;		// "right", "left" or "middle"
	private String style = null;
	
	/**
	 * Create a new Barline with defaults "right", "light-light"
	 */
	public Barline() {
		this("right", "light-light");
	}
	
	public Barline(String style) {
		this("right", style);
	}
	
	public Barline(String location, String style) {
		this.location = location;
		this.style = style;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
		
}
