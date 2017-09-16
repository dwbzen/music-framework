package music.junit;

import music.ScorePart;
import music.element.Score;
import music.instrument.PianoRH;

public class ScoreTest  {

	
	/**
	 * Usage: Score
	 * 
	 * @param args
	 */
	public void main(String[] args) {
		Score score = new Score("Title");
		ScorePart scorePart = new ScorePart(score, "Grand Piano", new PianoRH());
		score.addPart(scorePart);
		score.addCreator("Composer", "Don Bacon");	
	}
}
