package org.dwbzen.music;

import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import org.dwbzen.music.element.Score;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.Configuration;

/**
 * Creates a Score.
 * 
 * @author don_bacon
 *
 */
public class ScoreFactory implements IScoreFactory, Runnable, Supplier<Score> {
	
	private Map<String, Instrument> instruments;
	private Configuration configuration;
	private Score score = null;
	private int numberOfMeasures;
	private String title = null;
	private String opus = null;
	
	/**
	 * 
	 * @param configuration
	 * @param instruments
	 * @param nMeasures
	 * @param title
	 * @param opus
	 */
	public ScoreFactory(Configuration configuration, Map<String, Instrument> instruments, int nMeasures, String title, String opus) {
		this.configuration = configuration;
		this.instruments = instruments;
		numberOfMeasures = nMeasures;
		this.title = title;
		this.opus = opus;
	}
	
	public ScoreFactory(Configuration configuration, Map<String, Instrument> instruments, int nMeasures) {
		this(configuration, instruments, nMeasures, "", "");
	}

	@Override
	public Score createScore(boolean runFlag) {
		Properties configProperties = configuration.getProperties();
		score = new Score(configuration, title);
		score.setWorkNumber(opus);
		score.addCreator("composer", configProperties.getProperty("creator.composer", "No Composer"));
		score.addCreator("arranger", configProperties.getProperty("creator.arranger", "No Arranger"));
		score.addCreator("lyricist", configProperties.getProperty("creator.lyricist", "No Lyricist"));

		for(String instrumentName : instruments.keySet()) {
			Instrument instrument = instruments.get(instrumentName);
			score.getInstrumentNames().add(instrumentName);
			String partName = configProperties.getProperty(instrumentName + ".partName", instrumentName);
			ScorePart scorePart = new ScorePart(score, partName, instrument);
			/*
			 * if runFlag is true, set the number of measures and run the ScorePart to create the score from data sources
			 * Otherwise just create an empty ScorePart for the instrument.
			 */
			if(runFlag) {
				scorePart.setMaxMeasures(numberOfMeasures);
				scorePart.run();
			}
			score.addPart(scorePart);
		}
		return score;
	}

	@Override
	public void run() {
		score = createScore(true);
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

	public int getNumberOfMeasures() {
		return numberOfMeasures;
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
