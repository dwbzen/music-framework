package org.dwbzen.util.music;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.dwbzen.util.Configuration;

/**
 * DataSource base class. There is a DataSource concrete instance for each
 * instrument in the score configuration.
 * Configuration parameter selectionMode determines how records are selected.
 * If sequential, data records loaded sequentially, none are skipped. So instruments using
 * the same data set will have identical scoring.
 * If random, from 2 to 20 records are skipped on each selection. So the entire data
 * set must be large enough to accommodate worse case scenario. 
 * random selectionMode should be used when multiple instruments use the same
 * fractal data set.
 * 
 * @author don_bacon
 *
 */
public abstract class DataSource implements IDataSource {

	protected static final Logger log = LogManager.getLogger(DataSource.class);
	protected Configuration configuration = null;
	protected static final String NAME = "name:";	// field name
	protected static final String TYPE = "type:";	// another field name
	protected boolean randomSelection = false;
	protected String[] instrumentNames;
	protected Properties configProperties;
	protected int measures;
	protected int divisionsPerMeasure;
	protected int maxSize;
	protected Stream<String> stream;
	protected Random randomPredicate = null;
	protected String instrumentName;
	protected int skipFactor = 20;

	static final String SEQUENTIAL = "sequential";
	static final String RANDOM = "random";
	
	/**
	 * Sets configuration properties that are common across data sources: instrumentNames, randomSelection
	 * Then invokes configure() in derived class.
	 * @param config
	 */
	public DataSource(Configuration config, String instrumentName) {
		configuration = config;
		this.instrumentName = instrumentName;
		configProperties = config.getProperties();
		instrumentNames = configProperties.getProperty("score.instruments").split(",");
		randomSelection = configProperties.getProperty("selectionMode", SEQUENTIAL).equals(RANDOM);
		measures = Integer.valueOf( configProperties.getProperty("measures", "20")).intValue();
		divisionsPerMeasure = Integer.parseInt(configProperties.getProperty("score.measure.divisions", "16"));
		maxSize = measures * divisionsPerMeasure * 2;
		randomPredicate = new Random(ThreadLocalRandom.current());
		configure();
	}
	
	/**
	 * Configure using the Configuration instance (if not null),
	 * replacing the current Configuration.
	 */
	@Override
	public abstract void configure();
	
	@Override
	public Configuration getConfiguration() {
		return configuration;
	}
	
	@Override
	public void setConfiguration(Configuration config) {
		configuration = config;
	}

	
	public boolean isRandomSelection() {
		return randomSelection;
	}

	public void setRandomSelection(boolean randomSelection) {
		this.randomSelection = randomSelection;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int queryLimit) {
		maxSize = queryLimit;
	}

	/**
	 * Stream data for a particular client such as an Instrument
	 * @return Stream<String>
	 */
	public abstract Stream<String> stream();
	
	
    public Stream<String> getStream() {
    	return stream;
    }
    
    public String getInstrumentName() {
		return instrumentName;
	}

	
	/**
     * Used to filter data points in a Stream for random selection.
     * Does not apply (obviously) to RandomDataSource as that data
     * is generated on the fly.
     * 
     * @author donbacon
     *
     */
    class Random implements Predicate<String>, IntSupplier {
    	ThreadLocalRandom random = null;
    	
    	public Random(ThreadLocalRandom r) {
    		random = r;
    	}
		@Override
		public boolean test(String testString) {
			return random.nextBoolean();
		}
		@Override
		public int getAsInt() {
			// How many records to skip for RANDOM selection
			return random.nextInt(2, skipFactor);
		}
    	
    }
	
}
