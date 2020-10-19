package org.dwbzen.music.element.direction;

import org.dwbzen.common.util.INameable;

public abstract class DirectionType implements INameable {

	static final long serialVersionUID = 1L;
	private String name = null;
	
	public DirectionType(String aname) {
		name = aname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
		
}
