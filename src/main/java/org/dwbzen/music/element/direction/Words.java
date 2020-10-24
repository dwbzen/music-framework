package org.dwbzen.music.element.direction;

import org.dwbzen.music.element.direction.ScoreDirection.ScoreDirectionType;

/**
 * Used to attach text to a measure and staff.
 * @author don_bacon
 *
 */
public class Words extends DirectionType {

	private static final long serialVersionUID = 1L;
	private String text = null;
	
	public Words() {
		super("words");
		scoreDirectionType = ScoreDirectionType.WORDS;
	}
	
	public Words(String name) {
		super(name);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
