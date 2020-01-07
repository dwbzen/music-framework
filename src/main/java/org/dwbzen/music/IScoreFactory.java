package org.dwbzen.music;

import org.dwbzen.music.element.Score;

public interface IScoreFactory {
	Score createScore(String title, String opus);
}
