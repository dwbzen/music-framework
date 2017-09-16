package music.transform;

import java.io.Serializable;

import music.element.Score;
import music.element.ScorePartEntity;

/**
 * A Layer is a Score or ScorePartEntity container that can be combined with other Layers
 * to create a score Realization.
 * A Transform is an operation that can be applied to a single Layer.
 * 
 * @author don_bacon
 *
 */
public class Layer implements Serializable {

	private static final long serialVersionUID = -7270662335884644134L;
	private String name;
	private Score score = null;
	private ScorePartEntity scorePartEntity = null;
	private boolean audible = true;
	
	public Layer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Score getScore() {
		return score;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public boolean isAudible() {
		return audible;
	}

	public void setAudible(boolean audible) {
		this.audible = audible;
	}

	public ScorePartEntity getScorePartEntity() {
		return scorePartEntity;
	}

	public void setScorePartEntity(ScorePartEntity scorePartEntity) {
		this.scorePartEntity = scorePartEntity;
	}

}
