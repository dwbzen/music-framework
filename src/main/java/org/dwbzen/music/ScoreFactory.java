package org.dwbzen.music;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.dwbzen.music.element.Score;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.Configuration;

/**
 * Creates a Score.
 * @author DBacon
 *
 */
public class ScoreFactory implements IScoreFactory, Runnable, Supplier<Score> {
	
	private Map<String, Instrument> instruments;
	private Configuration configuration;
	private Score score = null;
	private int measures;
	private String title = null;
	private String opus = null;
	
	public ScoreFactory(Configuration configuration, Map<String, Instrument> instruments, int nMeasures, String title, String opus) {
		this.configuration = configuration;
		this.instruments = instruments;
		measures = nMeasures;
		this.title = title;
		this.opus = opus;
	}

	@Override
	public Score createScore(String title, String opus) {
		Properties configProperties = configuration.getProperties();
		score = new Score(configuration, title);
		score.setWorkNumber(opus);
		for(String instrumentName : instruments.keySet()) {
			Instrument instrument = instruments.get(instrumentName);
			score.getInstrumentNames().add(instrumentName);
			String partName = configProperties.getProperty("score.parts."+ instrumentName + ".partName", instrumentName);
			ScorePart scorePart = new ScorePart(score, partName, instrument);
			// set the max# measures to generate
			scorePart.setMaxMeasures(measures);
			score.addPart(scorePart);
			CompletableFuture<Void> future = CompletableFuture.runAsync(scorePart);
		}
		return score;
	}

	@Override
	public void run() {
		score = createScore(title, opus);
	}

	public Map<String, Instrument> getInstruments() {
		return instruments;
	}

	public void setInstruments(Map<String, Instrument> instruments) {
		this.instruments = instruments;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public int getMeasures() {
		return measures;
	}

	public void setMeasures(int measures) {
		this.measures = measures;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOpus() {
		return opus;
	}

	public void setOpus(String opus) {
		this.opus = opus;
	}

	public Score getScore() {
		return score;
	}

	@Override
	public Score get() {
		return score;
	}
}
