package org.dwbzen.music.element.song;

import org.dwbzen.music.element.Key;

/**
 * A lighter version of Key that has all the same fields, but a limited toJSON for use in Song.
 * Only mode and name are set and serialized: "key" : { "name" : "Bb-Major", "mode" : "MAJOR" }
 * Designation and Signature are left null.
 * 
 * @author don_bacon
 *
 */
public class KeyLite extends Key {

	private static final long serialVersionUID = 8904006604147329449L;

	public KeyLite() { }
	
	public KeyLite(Key other) {
		setName(other.getName());
		setMode(other.getMode());
	}
	
	@Override
	public String toJson() {
		StringBuffer sb = new StringBuffer("\"key\"");
		sb.append(" : { ");
		sb.append("\"name\" : \"" + getName() + "\", ");
		sb.append("\"mode\" : \"" + getMode().toString() + "\" }");
		return sb.toString();
	}

}
