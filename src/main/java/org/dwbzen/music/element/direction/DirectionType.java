package org.dwbzen.music.element.direction;

import org.dwbzen.common.util.INameable;
import org.dwbzen.music.element.direction.ScoreDirection.ScoreDirectionType;

public abstract class DirectionType implements INameable {

	static final long serialVersionUID = 1L;
	private String name = null;
	protected ScoreDirectionType scoreDirectionType = ScoreDirectionType.NONE;
	
	public DirectionType(String aname) {
		name = aname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ScoreDirectionType getScoreDirectionType() {
		return scoreDirectionType;
	}

	public void setScoreDirectionType(ScoreDirectionType scoreDirectionType) {
		this.scoreDirectionType = scoreDirectionType;
	}

}
