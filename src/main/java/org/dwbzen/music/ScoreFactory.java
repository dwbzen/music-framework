package org.dwbzen.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	private ExecutorService executorService = null;
	
	public ScoreFactory(Configuration configuration, Map<String, Instrument> instruments, int nMeasures, String title, String opus) {
		this.configuration = configuration;
		this.instruments = instruments;
		measures = nMeasures;
		this.title = title;
		this.opus = opus;
		executorService = Executors.newFixedThreadPool(Math.min(instruments.size(), 100), r -> {
		    Thread thread = new Thread(r);
		    thread.setDaemon(true);
		    return thread;
		});
	}

	@Override
	public Score createScore() {
		Properties configProperties = configuration.getProperties();
		score = new Score(configuration, title);
		score.setWorkNumber(opus);
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		
		for(String instrumentName : instruments.keySet()) {
			Instrument instrument = instruments.get(instrumentName);
			score.getInstrumentNames().add(instrumentName);
			String partName = configProperties.getProperty("score.parts."+ instrumentName + ".partName", instrumentName);
			ScorePart scorePart = new ScorePart(score, partName, instrument);
			score.addPart(scorePart);
			// set the max# measures to generate
			scorePart.setMaxMeasures(measures);
			
			futures.add(CompletableFuture.runAsync(scorePart, executorService));	
		}
		futures.stream().map(CompletableFuture::join);
		return score;
	}

	@Override
	public void run() {
		score = createScore();
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
